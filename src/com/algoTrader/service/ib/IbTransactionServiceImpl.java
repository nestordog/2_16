package com.algoTrader.service.ib;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Order;
import com.algoTrader.entity.PartialOrder;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.enumeration.OrderStatus;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.service.TickServiceException;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.RawTickVO;
import com.ib.client.Contract;
import com.ib.client.Execution;

public class IbTransactionServiceImpl extends IbTransactionServiceBase {

    private static Logger logger = MyLogger.getLogger(IbTransactionServiceImpl.class.getName());

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");

    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");
    private static boolean ibEnabled = "IB".equals(ConfigurationUtil.getBaseConfig().getString("marketChannel"));
    private static boolean transactionServiceEnabled = ConfigurationUtil.getBaseConfig().getBoolean("ib.transactionServiceEnabled");

    private static String[] spreadPositions = ConfigurationUtil.getBaseConfig().getStringArray("spreadPositions");

    private static String group = ConfigurationUtil.getBaseConfig().getString("ib.group");
    private static String openMethod = ConfigurationUtil.getBaseConfig().getString("ib.openMethod");
    private static String closeMethod = ConfigurationUtil.getBaseConfig().getString("ib.closeMethod");

    private static int transactionTimeout = ConfigurationUtil.getBaseConfig().getInt("ib.transactionTimeout");
    private static int retrievalTimeout = ConfigurationUtil.getBaseConfig().getInt("ib.retrievalTimeout");

    private DefaultClientSocket client;
    private DefaultWrapper wrapper;
    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();

    private Map<Integer, PartialOrder> partialOrdersMap;
    private Map<Integer, Boolean> executedMap;
    private Map<Integer, Boolean> deletedMap;

    private static int clientId = 3;

    protected void handleInit() {

        if (!ibEnabled || simulation || !transactionServiceEnabled)
            return;

        this.wrapper = new DefaultWrapper(clientId) {

            @Override
            public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {

                logger.debug("orderId: " + orderId +
                        " orderStatus: " + status +
                        " filled: " + filled +
                        " remaining: " + remaining +
                        " avgFillPrice: " + avgFillPrice +
                        " permId: " + permId +
                        " lastFillPrice: " + lastFillPrice +
                        " whyHeld: " + whyHeld);

                IbTransactionServiceImpl.this.lock.lock();
                try {

                    PartialOrder partialOrder = IbTransactionServiceImpl.this.partialOrdersMap.get(orderId);

                    if (partialOrder == null) {
                        logger.error("orderId " + orderId + " was not found");
                        return;
                    }

                    partialOrder.setExecutedQuantity(filled);

                    if ("Submitted".equals(status) || "PendingSubmit".equals(status)) {

                        if (filled == 0) {
                            partialOrder.setStatus(OrderStatus.SUBMITTED);
                        } else {
                            partialOrder.setStatus(OrderStatus.PARTIALLY_EXECUTED);
                        }

                    } else if ("Filled".equals(status)) {

                        partialOrder.setStatus(OrderStatus.EXECUTED);

                        IbTransactionServiceImpl.this.executedMap.put(orderId, true);
                        IbTransactionServiceImpl.this.condition.signalAll();

                    } else if ("Cancelled".equals(status)) {

                        partialOrder.setStatus(OrderStatus.CANCELED);

                        IbTransactionServiceImpl.this.deletedMap.put(orderId, true);
                        IbTransactionServiceImpl.this.condition.signalAll();
                    }

                } finally {
                    IbTransactionServiceImpl.this.lock.unlock();
                }
            }

            public void nextValidId(int orderId) {

                logger.debug("nextValidId: " + orderId);

                RequestIdManager.getInstance().initializeOrderId(orderId);
            }

            public void execDetails(int requestId, Contract contract, Execution execution) {

                logger.debug("orderId: " + execution.m_orderId +
                        " execId: " + execution.m_execId +
                        " time: " + execution.m_time +
                        " acctNumber: " + execution.m_acctNumber +
                        " shares: " + execution.m_shares +
                        " price: " + execution.m_price +
                        " permId: " + execution.m_permId +
                        " cumQty: " + execution.m_cumQty +
                        " avgPrice: " + execution.m_avgPrice);

                IbTransactionServiceImpl.this.lock.lock();
                try {

                    // if the execution does not represent a internal transfer create a transaction
                    if (!execution.m_execId.startsWith("F") && !execution.m_execId.startsWith("U")) {

                        PartialOrder partialOrder = IbTransactionServiceImpl.this.partialOrdersMap.get(execution.m_orderId);
                        Order order = partialOrder.getParentOrder();

                        Date dateTime = format.parse(execution.m_time);
                        String number = execution.m_execId;
                        int executedQuantity = Math.abs(execution.m_shares);
                        int signedExecutedQuantity = TransactionType.SELL.equals(order.getTransactionType()) ? -executedQuantity : executedQuantity;
                        BigDecimal price = RoundUtil.getBigDecimal(execution.m_price);

                        Transaction transaction = new TransactionImpl();
                        transaction.setDateTime(dateTime);
                        transaction.setNumber(number);
                        transaction.setQuantity(signedExecutedQuantity);
                        transaction.setPrice(price);

                        if (TransactionType.SELL.equals(order.getTransactionType()) || TransactionType.BUY.equals(order.getTransactionType())) {
                            double commission = order.getSecurity().getSecurityFamily().getCommission().doubleValue();
                            transaction.setCommission(RoundUtil.getBigDecimal(executedQuantity * commission));
                        } else {
                            transaction.setCommission(new BigDecimal(0));
                        }

                        partialOrder.addTransaction(transaction);

                        logger.info("executed " + execution.m_shares +
                                " of " + partialOrder.getParentOrder().getRequestedQuantity() +
                                " at spreadPosition " + partialOrder.getSpreadPosition());
                    }

                } catch (ParseException e) {
                    logger.error("illegal time format ", e);
                } finally {
                    IbTransactionServiceImpl.this.lock.unlock();
                }
            }

            public void connectionClosed() {

                super.connectionClosed();

                connect();
            }

            public void error(int id, int code, String errorMsg) {

                String message = "client: " + IbTransactionServiceImpl.clientId + " id: " + id + " code: " + code + " " + errorMsg.replaceAll("\n", " ");

                if (code == 202) {

                    // Order cancelled
                    // do nothing, since we cancelled the order ourself
                    logger.debug(message);

                } else if (code == 201) {

                    // Order rejected - reason:To late to replace order
                    // do nothing, we modified the price too late
                    logger.debug(message);

                } else {
                    super.error(id, code, errorMsg);

                    IbTransactionServiceImpl.this.lock.lock();
                    try {

                        PartialOrder partialOrder = IbTransactionServiceImpl.this.partialOrdersMap.get(id);

                        if (partialOrder != null) {
                            partialOrder.setStatus(OrderStatus.CANCELED);

                            IbTransactionServiceImpl.this.deletedMap.put(id, true);
                            IbTransactionServiceImpl.this.condition.signalAll();
                        }

                    } finally {
                        IbTransactionServiceImpl.this.lock.unlock();
                    }
                }
            }
        };

        this.client = new DefaultClientSocket(this.wrapper);

        connect();
    }

    protected void handleConnect() {

        if (!ibEnabled || simulation || !transactionServiceEnabled)
            return;

        this.partialOrdersMap = new HashMap<Integer, PartialOrder>();
        this.executedMap = new HashMap<Integer, Boolean>();
        this.deletedMap = new HashMap<Integer, Boolean>();

        this.client.connect(clientId);
    }

    protected ConnectionState handleGetConnectionState() {

        if (this.wrapper == null) {
            return ConnectionState.DISCONNECTED;
        } else {
            return this.wrapper.getState();
        }
    }

    protected void handleExecuteExternalTransaction(Order order) throws Exception {

        if (!this.wrapper.getState().equals(ConnectionState.READY)) {
            logger.error("transaction cannot be executed, because IB is not connected");
            return;
        }

        getPartialOrder(order);

        Tick tick = null;
        for (String spreadPosition : spreadPositions) {

            if (spreadPosition == spreadPositions[0]) {

                tick = getValidTick(order);
            }

            PartialOrder partialOrder = order.getCurrentPartialOrder();
            partialOrder.setSpreadPosition(Double.parseDouble(spreadPosition));

            placeOrModifyPartialOrder(partialOrder, tick);

            if (OrderStatus.OPEN.equals(partialOrder.getStatus())) {

                // nothing went through, so try next higher spreadPosition
                continue;

            } else if (OrderStatus.PARTIALLY_EXECUTED.equals(partialOrder.getStatus())) {

                // try to cancel, if successfull reset the partialOrder
                // otherwise the order must have been executed in the meantime
                if (cancelPartialOrder(partialOrder)) {

                    // cancel sucessfull so reset the order
                    getPartialOrder(order);
                    continue;

                } else {

                    // order did EXECUTE after beeing cancelled, so we are done!
                    break;
                }

            } else if (OrderStatus.EXECUTED.equals(partialOrder.getStatus())) {

                // we are done!
                break;

            } else if (OrderStatus.CANCELED.equals(partialOrder.getStatus())) {

                // there must have been a problem submitting the error so abort
                // the loop
                break;
            }
        }

        cancelRemainingOrder(order);
    }

    private Tick getValidTick(Order order) throws TransformerException, ParseException, InterruptedException {

        Security security = order.getSecurity();
        TransactionType transactionType = order.getTransactionType();
        long requestedQuantity = order.getRequestedQuantity();

        // only validate price and volum the first time, because our
        // orders will show up in the orderbook as well
        Tick tick;
        while (true) {

            RawTickVO rawTick = getIbTickService().retrieveTick(order.getSecurity());
            tick = getIbTickService().completeRawTick(rawTick);

            // validity check (volume and bid/ask spread)
            try {
                tick.validate();
                break;

            } catch (TickServiceException e) {

                logger.warn(e.getMessage());

                // wait a little then try again
                Thread.sleep(retrievalTimeout);
            }
        }

        // validity check (available volume)
        if (TransactionType.BUY.equals(transactionType) && tick.getVolAsk() < requestedQuantity) {
            logger.warn("available volume (" + tick.getVolAsk() + ") is smaler than requested quantity (" + requestedQuantity + ") for a order on " + security.getSymbol());
        } else if (TransactionType.SELL.equals(transactionType) && tick.getVolBid() < requestedQuantity) {
            logger.warn("available volume (" + tick.getVolBid() + ") is smaler than requested quantity (" + requestedQuantity + ") for a order on " + security.getSymbol());
        }

        return tick;
    }

    private void getPartialOrder(Order order) {

        PartialOrder partialOrder = order.createPartialOrder();

        partialOrder.setOrderId(RequestIdManager.getInstance().getNextOrderId());

        this.partialOrdersMap.put(partialOrder.getOrderId(), partialOrder);
    }

    private void placeOrModifyPartialOrder(PartialOrder partialOrder, Tick tick) {

        this.executedMap.put(partialOrder.getOrderId(), false);

        Contract contract = IbUtil.getContract(partialOrder.getParentOrder().getSecurity());

        com.ib.client.Order ibOrder = new com.ib.client.Order();
        ibOrder.m_action = partialOrder.getParentOrder().getTransactionType().toString();
        ibOrder.m_totalQuantity = (int) partialOrder.getRequestedQuantity();
        ibOrder.m_orderType = "LMT";
        ibOrder.m_lmtPrice = getPrice(partialOrder.getParentOrder(), partialOrder.getSpreadPosition(), tick.getBid().doubleValue(), tick.getAsk().doubleValue());
        ibOrder.m_faGroup = group;

        if (TransactionType.SELL.equals(partialOrder.getParentOrder().getTransactionType())) {
            ibOrder.m_faMethod = openMethod;
        }
        if (TransactionType.BUY.equals(partialOrder.getParentOrder().getTransactionType())) {
            ibOrder.m_faMethod = closeMethod;
            ibOrder.m_faPercentage = "-100";
        }

        this.client.placeOrder(partialOrder.getOrderId(), contract, ibOrder);

        logger.debug("orderId: " + partialOrder.getOrderId() + " placeOrder for quantity: " + partialOrder.getRequestedQuantity() + " limit: " + ibOrder.m_lmtPrice + " spreadPosition: "
                + partialOrder.getSpreadPosition());

        this.lock.lock();

        try {
            while (!this.executedMap.get(partialOrder.getOrderId())) {
                if (!this.condition.await(transactionTimeout, TimeUnit.MILLISECONDS))
                    break;
            }

        } catch (InterruptedException e) {
            logger.error("problem placing order", e);
        } finally {
            this.lock.unlock();
        }
    }

    private boolean cancelPartialOrder(PartialOrder partialOrder) {

        this.deletedMap.put(partialOrder.getOrderId(), false);

        this.lock.lock();

        try {

            this.client.cancelOrder(partialOrder.getOrderId());

            logger.debug("orderId: " + partialOrder.getOrderId() + " cancelOrder");

            while (!this.deletedMap.get(partialOrder.getOrderId()) && !this.executedMap.get(partialOrder.getOrderId())) {
                this.condition.await();
            }

            if (OrderStatus.CANCELED.equals(partialOrder.getStatus())) {
                logger.debug("orderId: " + partialOrder.getOrderId() + " has been canceled");
                return true;

            } else if (OrderStatus.EXECUTED.equals(partialOrder.getStatus())) {
                logger.debug("orderId: " + partialOrder.getOrderId() + " has been executed after trying to cancel");
                return false;

            } else {
                throw new IbTickServiceException("orderId: " + partialOrder.getOrderId() + " unappropriate order status: " + partialOrder.getStatus());
            }

        } catch (InterruptedException e) {
            throw new IbTickServiceException("problem canceling order", e);
        } finally {
            this.lock.unlock();
        }
    }

    private void cancelRemainingOrder(Order order) {

        // if order did not execute fully, cancel the rest
        OrderStatus status = order.getCurrentPartialOrder().getStatus();
        if (OrderStatus.SUBMITTED.equals(status) || OrderStatus.PARTIALLY_EXECUTED.equals(status)) {

            cancelPartialOrder(order.getCurrentPartialOrder());
            order.setStatus(OrderStatus.CANCELED);

            logger.warn("order on: " + order.getSecurity().getSymbol() + " did not execute fully, requestedQuantity: " + order.getRequestedQuantity() + " executedQuantity: "
                    + order.getPartialOrderExecutedQuantity());
        }
    }
}
