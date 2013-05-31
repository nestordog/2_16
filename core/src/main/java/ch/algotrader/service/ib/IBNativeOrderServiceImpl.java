/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service.ib;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.adapter.ib.IBClient;
import ch.algotrader.adapter.ib.IBIdGenerator;
import ch.algotrader.adapter.ib.IBUtil;
import ch.algotrader.util.MyLogger;

import ch.algotrader.entity.trade.LimitOrderI;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.StopOrderI;
import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.service.ib.IBNativeOrderServiceBase;
import com.ib.client.Contract;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeOrderServiceImpl extends IBNativeOrderServiceBase {

    private static final long serialVersionUID = -7426452967133280762L;

    private static Logger logger = MyLogger.getLogger(IBNativeOrderServiceImpl.class.getName());

    private static IBClient client;

    private @Value("${ib.faMethod}") String faMethod;

    @Override
    protected void handleInit() throws Exception {

        client = getIBClientFactory().getDefaultClient();
    }

    @Override
    protected void handleValidateOrder(SimpleOrder order) throws Exception {

        // validate quantity by allocations (if fa is enabled and no account has been specified)
        //        if (this.faEnabled && (order.getAccount() == null || "".equals(order.getAccount()))) {
        //            long quantity = getAccountService().getQuantityByAllocation(order.getStrategy().getName(), order.getQuantity());
        //            if (quantity != order.getQuantity()) {
        //                OrderQuantityValidationException ex = new OrderQuantityValidationException();
        //                ex.setMaxQuantity(quantity);
        //                throw ex;
        //            }
        //        }
    }

    @Override
    protected void handleSendOrder(SimpleOrder order) throws Exception {

        String intId = IBIdGenerator.getInstance().getNextOrderId();
        order.setIntId(intId);
        sendOrModifyOrder(order);
    }

    @Override
    protected void handleModifyOrder(SimpleOrder order) throws Exception {

        sendOrModifyOrder(order);
    }

    @Override
    protected void handleCancelOrder(SimpleOrder order) throws Exception {

        if (client == null || client.getMessageHandler().getState().getValue() < ConnectionState.LOGGED_ON.getValue()) {
            logger.error("order cannot be cancelled, because IB is not logged on");
            return;
        }

        // progapate the order (even though nothing actually changed) to be able to identify missing replies
        getOrderService().propagateOrder(order);

        client.cancelOrder(Integer.parseInt(order.getIntId()));

        logger.info("requested order cancellation for order: " + order);
    }

    /**
     * helper method to be used in both sendorder and modifyorder.
     * @throws Exception
     */
    private void sendOrModifyOrder(Order order) throws Exception {

        if (client == null || client.getMessageHandler().getState().getValue() < ConnectionState.LOGGED_ON.getValue()) {
            logger.error("order cannot be sent / modified, because IB is not logged on");
            return;
        }

        Contract contract = IBUtil.getContract(order.getSecurityInitialized());

        com.ib.client.Order ibOrder = new com.ib.client.Order();
        ibOrder.m_totalQuantity = (int) order.getQuantity();
        ibOrder.m_action = order.getSide().toString();
        ibOrder.m_orderType = IBUtil.getIBOrderType(order);
        ibOrder.m_transmit = true;

        // handle a potentially defined account
        if (order.getAccount().getExtAccount() != null) {

            ibOrder.m_account = order.getAccount().getExtAccount();

        // handling for financial advisor account groups
        } else if (order.getAccount().getExtAccountGroup() != null) {

            ibOrder.m_faGroup = order.getAccount().getExtAccountGroup();
            ibOrder.m_faMethod = this.faMethod;

            //            long existingQuantity = 0;
            //            for (Position position : order.getSecurity().getPositions()) {
            //                existingQuantity += position.getQuantity();
            //            }
            //
            //            // evaluate weather the transaction is opening or closing
            //            boolean opening = false;
            //            if (existingQuantity > 0 && Side.SELL.equals(order.getSide())) {
            //                opening = false;
            //            } else if (existingQuantity <= 0 && Side.SELL.equals(order.getSide())) {
            //                opening = true;
            //            } else if (existingQuantity < 0 && Side.BUY.equals(order.getSide())) {
            //                opening = false;
            //            } else if (existingQuantity >= 0 && Side.BUY.equals(order.getSide())) {
            //                opening = true;
            //            }
            //
            //            ibOrder.m_faGroup = order.getAccount().getExtAccountGroup();
            //
            //            if (opening) {
            //
            //                // open by specifying the actual quantity
            //                ibOrder.m_faMethod = this.faOpenMethod;
            //
            //            } else {
            //
            //                // reduce by percentage
            //                ibOrder.m_faMethod = "PctChange";
            //                ibOrder.m_totalQuantity = 0; // bacause the order is percent based
            //                ibOrder.m_faPercentage = "-" + Math.abs(order.getQuantity() * 100 / (existingQuantity - order.getQuantity()));
            //            }

        // handling for financial advisor allocation profiles
        } else if (order.getAccount().getExtAllocationProfile() != null) {

            ibOrder.m_faProfile = order.getAccount().getExtAllocationProfile();
        }

        // add clearing information
        if (order.getAccount().getExtClearingAccount() != null) {
            ibOrder.m_clearingAccount = order.getAccount().getExtClearingAccount();
            ibOrder.m_clearingIntent = "Away";
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
        getOrderService().propagateOrder(order);

        // place the order through IBClient
        client.placeOrder(Integer.parseInt(order.getIntId()), contract, ibOrder);

        logger.info("placed or modified order: " + order);
    }
}
