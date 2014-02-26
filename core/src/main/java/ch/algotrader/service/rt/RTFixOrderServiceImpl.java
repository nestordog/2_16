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
package ch.algotrader.service.rt;

import java.util.Date;

import quickfix.field.Account;
import quickfix.field.ExecInst;
import quickfix.field.HandlInst;
import quickfix.field.LocateReqd;
import quickfix.field.TransactTime;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class RTFixOrderServiceImpl extends RTFixOrderServiceBase {

    private static final long serialVersionUID = 1030392480992545177L;

    @Override
    protected void handleSendOrder(final SimpleOrder order, final NewOrderSingle newOrder) throws Exception {

        newOrder.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC));
        newOrder.set(new LocateReqd(true));

        if (order.isDirect()) {
            newOrder.set(new ExecInst(String.valueOf(ExecInst.HELD)));
        } else {
            newOrder.set(new ExecInst(String.valueOf(ExecInst.NOT_HELD)));
        }

        // handling for accounts
        if (order.getAccount().getExtAccount() != null) {
            newOrder.set(new Account(order.getAccount().getExtAccount()));
        }
    }

    @Override
    protected void handleModifyOrder(final SimpleOrder order, final OrderCancelReplaceRequest replaceRequest) throws Exception {

        replaceRequest.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC));
        replaceRequest.set(new LocateReqd(true));

        if (order.isDirect()) {
            replaceRequest.set(new ExecInst(String.valueOf(ExecInst.HELD)));
        } else {
            replaceRequest.set(new ExecInst(String.valueOf(ExecInst.NOT_HELD)));
        }

        // handling for accounts
        if (order.getAccount().getExtAccount() != null) {
            replaceRequest.set(new Account(order.getAccount().getExtAccount()));
        }
    }

    @Override
    protected void handleCancelOrder(final SimpleOrder order, final OrderCancelRequest cancelRequest) throws Exception {

        // handling for accounts
        if (order.getAccount().getExtAccount() != null) {
            cancelRequest.set(new Account(order.getAccount().getExtAccount()));
        }

        cancelRequest.set(new TransactTime(new Date()));
    }

    @Override
    protected OrderServiceType handleGetOrderServiceType() throws Exception {

        return OrderServiceType.RT_FIX;
    }
}
