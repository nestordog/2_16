package com.algoTrader.service.ib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.algoTrader.entity.Order;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.enumeration.OrderStatus;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;
import com.ib.client.AnyWrapper;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.Execution;
import com.ib.client.OrderState;

public class IbTransactionServiceImpl extends IbTransactionServiceBase implements InitializingBean {

    private static Logger logger = MyLogger.getLogger(IbTransactionServiceImpl.class.getName());

    private static boolean ibEnabled = "IB".equals(PropertiesUtil.getProperty("marketChannel"));
    private static String[] bidAskSpreadPositions = PropertiesUtil.getProperty("bidAskSpreadPositions").split("\\s");

    private static int port = PropertiesUtil.getIntProperty("ib.port");
    private static String account = PropertiesUtil.getProperty("ib.account");
    private static int confirmationTimeout = PropertiesUtil.getIntProperty("ib.confirmationTimeout");

    private EClientSocket client;
    private Lock lock = new ReentrantLock();
    private Condition placeOrderCondition = this.lock.newCondition();
    private Condition deleteOrderCondition = this.lock.newCondition();

    private Map<Integer, Order> orderMap = new HashMap<Integer, Order>();
    private Map<Integer, Boolean> executedMap = new HashMap<Integer, Boolean>();
    private Map<Integer, Boolean> deletedMap = new HashMap<Integer, Boolean>();

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd  hh:mm:ss");
    private static int clientId = 0;

    public void afterPropertiesSet() throws Exception {

        init();
    }

    protected void handleInit() {

        if (!ibEnabled)
            return;

        AnyWrapper wrapper = new DefaultWrapper() {

            @Override
            public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {

                logger.debug("orderId: " + orderId + " orderStatus: " + status + ", filled: " + filled);

                IbTransactionServiceImpl.this.lock.lock();
                try {

                    Order order = IbTransactionServiceImpl.this.orderMap.get(orderId);

                    order.setExecutedQuantity(filled);

                    if ((filled > 0) && ("Submitted".equals(status) || "PendingSubmit".equals(status))) {

                        order.setStatus(OrderStatus.PARTIALLY_EXECUTED);

                    } else if ("Filled".equals(status) && (order.getCommission() != 0)) {
                        order.setStatus(OrderStatus.EXECUTED);

                        IbTransactionServiceImpl.this.executedMap.put(orderId, true);
                        IbTransactionServiceImpl.this.placeOrderCondition.signalAll();

                    } else if ("Cancelled".equals(status)) {

                        IbTransactionServiceImpl.this.deletedMap.put(orderId, true);
                        IbTransactionServiceImpl.this.deleteOrderCondition.signalAll();
                    }

                } finally {
                    IbTransactionServiceImpl.this.lock.unlock();
                }
            }

            public void openOrder(int orderId, Contract contract, com.ib.client.Order iBOrder, OrderState orderState) {

                double commission = orderState.m_commission == Double.MAX_VALUE ? 0 : orderState.m_commission;
                logger.debug("orderId: " + orderId + " openOrder: commission: " + commission);

                IbTransactionServiceImpl.this.lock.lock();
                try {

                    Order order = IbTransactionServiceImpl.this.orderMap.get(orderId);
                    order.setCommission(commission);

                } finally {
                    IbTransactionServiceImpl.this.lock.unlock();
                }
            }

            public void nextValidId(int orderId) {

                logger.debug("nextValidId: " + orderId);

                RequestIdManager.getInstance().initializeOrderId(orderId);
            }

            @SuppressWarnings("unchecked")
            public void execDetails(int requestId, Contract contract, Execution execution) {

                logger.debug("orderId: " + execution.m_orderId + " execDetails, price: " + execution.m_price + ", quantity: " + execution.m_shares);

                IbTransactionServiceImpl.this.lock.lock();
                try {

                    Order order = IbTransactionServiceImpl.this.orderMap.get(execution.m_orderId);

                    Transaction transaction = new TransactionImpl();
                    transaction.setDateTime(format.parse(execution.m_time));
                    transaction.setNumber(String.valueOf(execution.m_permId));
                    transaction.setQuantity(execution.m_shares);
                    transaction.setPrice(RoundUtil.getBigDecimal(execution.m_price));

                    order.getTransactions().add(transaction);

                } catch (ParseException e) {
                    logger.error("illegal time format ", e);
                } finally {
                    IbTransactionServiceImpl.this.lock.unlock();
                }
            }

            public void error(int id, int code, String errorMsg) {

                if (code == 202) {
                    // do nothing, since we probably cancelled the order ourself
                } else {
                    super.error(id, code, errorMsg);
                }
            }
        };

        this.client = new EClientSocket(wrapper);
        this.client.eConnect(null, port, clientId);
    }

    protected void handleExecuteExternalTransaction(Order order) throws Exception {

        order.setNumber(RequestIdManager.getInstance().getNextOrderId());
        this.orderMap.put(order.getNumber(), order);

        Tick tick = null;
        for (String bidAskSpreadPosition : bidAskSpreadPositions) {

            if (bidAskSpreadPosition.equals(bidAskSpreadPositions[0])) {

                // only validate price and volum the first time, because our
                // orders will show up in the orderbook as well
                tick = getIbTickService().retrieveTick(order.getSecurity());
                validateTick(order, tick);
            }

            placeOrModifyOrder(order, Double.valueOf(bidAskSpreadPosition), tick);

            if (OrderStatus.OPEN.equals(order.getStatus())) {

                // nothing went through, so try next higher bidAskSpreadPosition
                continue;

            } else if (OrderStatus.PARTIALLY_EXECUTED.equals(order.getStatus())) {

                distributeCommissions(order);

                cancelRemainingOrder(order);

                resetOrder(order);

                continue;

            } else if (OrderStatus.EXECUTED.equals(order.getStatus())) {

                // we are done!
                distributeCommissions(order);

                break;
            }
        }

        // if order did not execute fully, cancel the rest
        if (!OrderStatus.EXECUTED.equals(order.getStatus())) {

            cancelRemainingOrder(order);

            logger.warn("orderid: " + order.getNumber() + " did not execute fully, requestedQuantity: " + order.getRequestedQuantity() + " executedQuantity: " + order.getExecutedQuantity());
        }
    }

    private void placeOrModifyOrder(Order order, double bidAskSpreadPosition, Tick tick) {

        this.executedMap.put(order.getNumber(), false);

        Contract contract = IbUtil.getContract(order.getSecurity());

        com.ib.client.Order ibOrder = new com.ib.client.Order();
        ibOrder.m_action = order.getTransactionType().getValue();
        ibOrder.m_totalQuantity = (int) order.getRequestedQuantity();
        ibOrder.m_orderType = "LMT";
        ibOrder.m_lmtPrice = getPrice(order, bidAskSpreadPosition, tick.getBid().doubleValue(), tick.getAsk().doubleValue());
        ibOrder.m_account = account;

        this.lock.lock();

        try {

            this.client.placeOrder(order.getNumber(), contract, ibOrder);

            logger.debug("orderId: " + order.getNumber() + " placeOrder, quantity: " + order.getRequestedQuantity() + ", limit: " + ibOrder.m_lmtPrice + ", bidAskSpreadPosition: "
                    + bidAskSpreadPosition);

            while (!this.executedMap.get(order.getNumber())) {
                if (!this.placeOrderCondition.await(confirmationTimeout, TimeUnit.SECONDS))
                    break;
            }

        } catch (InterruptedException e) {
            logger.error("problem placing order", e);
        } finally {
            this.lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private void distributeCommissions(Order order) {

        if (order.getExecutedQuantity() > 0) {
            Collection<Transaction> transactions = order.getTransactions();
            for (Transaction transaction : transactions) {
                double transactionCommission = order.getCommission() / order.getExecutedQuantity() * transaction.getQuantity();
                transaction.setCommission(RoundUtil.getBigDecimal(transactionCommission));
            }
        }
    }

    private void cancelRemainingOrder(Order order) {

        this.deletedMap.put(order.getNumber(), false);

        this.lock.lock();

        try {

            this.client.cancelOrder(order.getNumber());

            logger.debug("orderId: " + order.getNumber() + " cancelOrder");

            while (!this.deletedMap.get(order.getNumber())) {
                this.deleteOrderCondition.await();
            }

        } catch (InterruptedException e) {
            logger.error("problem canceling order", e);
        } finally {
            this.lock.unlock();
        }
    }

    private void resetOrder(Order order) {

        // only part of the order has gone through, so reduce the
        // requested numberOfContracts by this number and keep going
        order.setRequestedQuantity(-Math.abs(order.getExecutedQuantity()));

        // increase the orderNumber, since we are submitting an new order
        order.setNumber(RequestIdManager.getInstance().getNextOrderId());

        order.setExecutedQuantity(0);
        order.setCommission(0);

        this.orderMap.put(order.getNumber(), order);
    }

    private void validateTick(Order order, Tick tick) throws TransformerException, ParseException {

        Security security = order.getSecurity();
        TransactionType transactionType = order.getTransactionType();
        long requestedQuantity = order.getRequestedQuantity();

        // validity check (volume and bid/ask spread)
        tick.validate();

        // validity check (available volume)
        if (TransactionType.BUY.equals(transactionType) && tick.getVolAsk() < requestedQuantity) {
            logger.warn("available volume (" + tick.getVolAsk() + ") is smaler than requested quantity (" + requestedQuantity + ") for a order on " + security.getIsin());
        } else if (TransactionType.SELL.equals(transactionType) && tick.getVolBid() < requestedQuantity) {
            logger.warn("available volume (" + tick.getVolBid() + ") is smaler than requested quantity (" + requestedQuantity + ") for a order on " + security.getIsin());
        }
    }
}
