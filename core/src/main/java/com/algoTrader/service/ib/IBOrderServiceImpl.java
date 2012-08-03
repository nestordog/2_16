package com.algoTrader.service.ib;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.trade.LimitOrderI;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.entity.trade.OrderQuantityValidationException;
import com.algoTrader.entity.trade.SimpleOrder;
import com.algoTrader.entity.trade.StopOrderI;
import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.enumeration.Side;
import com.algoTrader.util.MyLogger;
import com.ib.client.Contract;

public class IBOrderServiceImpl extends IBOrderServiceBase {

    private static final long serialVersionUID = -7426452967133280762L;

    private static Logger logger = MyLogger.getLogger(IBOrderServiceImpl.class.getName());

    private static IBClient client;

    private @Value("${ib.faEnabled}") boolean faEnabled;
    private @Value("${ib.faAccount}") String faAccount;
    private @Value("${ib.faGroup}") String faGroup;
    private @Value("${ib.faOpenMethod}") String faOpenMethod;
    private @Value("${ib.faCloseMethod}") String faCloseMethod;

    @Override
    protected void handleInit() throws Exception {

        client = getIBClientFactory().getDefaultClient();
    }

    @Override
    protected void handleValidateExternalOrder(SimpleOrder order) throws Exception {

        // validate quantity by allocations (if fa is enabled and no account has been specified)
        if (this.faEnabled && (order.getAccount() == null || "".equals(order.getAccount()))) {
            long quantity = getAccountService().getQuantityByAllocation(order.getStrategy().getName(), order.getQuantity());
            if (quantity != order.getQuantity()) {
                OrderQuantityValidationException ex = new OrderQuantityValidationException();
                ex.setMaxQuantity(quantity);
                throw ex;
            }
        }
    }

    @Override
    protected void handleSendExternalOrder(SimpleOrder order) throws Exception {

        int orderNumber = IBIdGenerator.getInstance().getNextOrderId();
        order.setNumber(orderNumber);
        sendOrModifyOrder(order);
    }

    @Override
    protected void handleModifyExternalOrder(SimpleOrder order) throws Exception {

        sendOrModifyOrder(order);
    }

    @Override
    protected void handleCancelExternalOrder(SimpleOrder order) throws Exception {

        if (client.getMessageHandler().getState().getValue() < ConnectionState.LOGGED_ON.getValue()) {
            logger.error("transaction cannot be executed, because IB is not logged on");
            return;
        }

        client.cancelOrder(order.getNumber());

        logger.info("requested order cancellation for order: " + order);
    }

    /**
     * helper method to be used in both sendorder and modifyorder.
     * @throws Exception
     */
    private void sendOrModifyOrder(Order order) throws Exception {

        if (client.getMessageHandler().getState().getValue() < ConnectionState.LOGGED_ON.getValue()) {
            logger.error("transaction cannot be executed, because IB is not logged on");
            return;
        }

        Contract contract = IBUtil.getContract(order.getSecurity());

        com.ib.client.Order ibOrder = new com.ib.client.Order();
        ibOrder.m_action = order.getSide().toString();
        ibOrder.m_orderType = IBUtil.getIBOrderType(order);
        ibOrder.m_transmit = true;

        // handle a potentially defined account
        if (order.getAccount() != null && !"".equals(order.getAccount())) {

            ibOrder.m_totalQuantity = (int) order.getQuantity();

            ibOrder.m_account = order.getAccount();

        // handling for financial advisor accounts
        } else if (this.faEnabled) {

            if (this.faGroup != null && !"".equals(this.faGroup)) {

                long existingQuantity = 0;
                for (Position position : order.getSecurity().getPositions()) {
                    existingQuantity += position.getQuantity();
                }

                // evaluate weather the transaction is opening or closing
                boolean opening = false;
                if (existingQuantity > 0 && Side.SELL.equals(order.getSide())) {
                    opening = false;
                } else if (existingQuantity <= 0 && Side.SELL.equals(order.getSide())) {
                    opening = true;
                } else if (existingQuantity < 0 && Side.BUY.equals(order.getSide())) {
                    opening = false;
                } else if (existingQuantity >= 0 && Side.BUY.equals(order.getSide())) {
                    opening = true;
                }

                ibOrder.m_faGroup = this.faGroup;

                if (opening) {

                    // open by specifying the actual quantity
                    ibOrder.m_faMethod = this.faOpenMethod;
                    ibOrder.m_totalQuantity = (int) order.getQuantity();

                } else {

                    // reduce by percentage
                    ibOrder.m_faMethod = this.faCloseMethod;
                    ibOrder.m_faPercentage = "-" + Math.abs(order.getQuantity() * 100 / (existingQuantity - order.getQuantity()));
                }

            } else {

                ibOrder.m_totalQuantity = (int) order.getQuantity();

                ibOrder.m_faProfile = order.getStrategy().getName().toUpperCase();

            }

        } else {

            ibOrder.m_totalQuantity = (int) order.getQuantity();

            // if fa is disabled, it is still possible to work with an IB FA setup if a single client account is specified
            if (this.faAccount != null) {
                ibOrder.m_account = this.faAccount;
            }
        }

        //set the limit price if order is a limit order or stop limit order
        if (order instanceof LimitOrderI) {
            ibOrder.m_lmtPrice = ((LimitOrderI) order).getLimit().doubleValue();
        }

        //set the stop price if order is a stop order or stop limit order
        if (order instanceof StopOrderI) {
            ibOrder.m_auxPrice = ((StopOrderI) order).getStop().doubleValue();
        }

        // progapate the order to all corresponding esper engines
        propagateOrder(order);

        // place the order through IBClient
        client.placeOrder(order.getNumber(), contract, ibOrder);

        logger.info("placed or modified order: " + order);
    }
}
