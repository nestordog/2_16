/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service.ib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import ch.algotrader.adapter.ib.IBUtil;
import ch.algotrader.entity.trade.LimitOrderI;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.StopOrderI;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.util.MyLogger;

import com.ib.client.Contract;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeOrderServiceImpl extends IBNativeOrderServiceBase {

    private static Logger logger = MyLogger.getLogger(IBNativeOrderServiceImpl.class.getName());
    private static DateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    private static boolean firstOrder = true;

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

        // Because of an IB bug only one order can be submitted at a time when
        // first connecting to IB, so wait 100ms after the first order

        logger.info("before place");

        if (firstOrder) {

            synchronized (this) {
                internalSendOrder(order);
                Thread.sleep(200);
                firstOrder = false;
            }

        } else {

            internalSendOrder(order);
        }
    }

    private synchronized void internalSendOrder(SimpleOrder order) throws Exception {

        String intId = getIBIdGenerator().getNextOrderId();
        order.setIntId(intId);
        sendOrModifyOrder(order);
    }

    @Override
    protected void handleModifyOrder(SimpleOrder order) throws Exception {

        sendOrModifyOrder(order);
    }

    @Override
    protected void handleCancelOrder(SimpleOrder order) throws Exception {

        if (!getIBSession().getLifecycle().isLoggedOn()) {
            logger.error("order cannot be cancelled, because IB is not logged on");
            return;
        }

        getIBSession().cancelOrder(Integer.parseInt(order.getIntId()));

        logger.info("requested order cancellation for order: " + order);
    }

    /**
     * helper method to be used in both sendorder and modifyorder.
     * @throws Exception
     */
    private void sendOrModifyOrder(Order order) throws Exception {

        if (!getIBSession().getLifecycle().isLoggedOn()) {
            logger.error("order cannot be sent / modified, because IB is not logged on");
            return;
        }

        Contract contract = IBUtil.getContract(order.getSecurityInitialized());

        com.ib.client.Order ibOrder = new com.ib.client.Order();
        ibOrder.m_totalQuantity = (int) order.getQuantity();
        ibOrder.m_action = IBUtil.getIBSide(order.getSide());
        ibOrder.m_orderType = IBUtil.getIBOrderType(order);
        ibOrder.m_transmit = true;

        // handle a potentially defined account
        if (order.getAccount().getExtAccount() != null) {

            ibOrder.m_account = order.getAccount().getExtAccount();

        // handling for financial advisor account groups
        } else if (order.getAccount().getExtAccountGroup() != null) {

            ibOrder.m_faGroup = order.getAccount().getExtAccountGroup();
            ibOrder.m_faMethod = this.getIBConfig().getFaMethod();

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

        // set Time-In-Force (ATC are set as order types LOC and MOC)
        if (order.getTif() != null && !TIF.ATC.equals(order.getTif())) {
            ibOrder.m_tif = order.getTif().getValue();

            // set the TIF-Date
            if (order.getTifDateTime() != null) {
                ibOrder.m_goodTillDate = format.format(order.getTifDateTime());
            }
        }

        // progapate the order to all corresponding esper engines
        getOrderService().propagateOrder(order);

        // place the order through IBSession
        getIBSession().placeOrder(Integer.parseInt(order.getIntId()), contract, ibOrder);

        logger.info("placed or modified order: " + order);
    }

    @Override
    protected OrderServiceType handleGetOrderServiceType() throws Exception {
        return OrderServiceType.IB_NATIVE;
    }
}
