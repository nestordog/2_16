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
package ch.algotrader.adapter.rt;

import java.util.Date;

import ch.algotrader.adapter.fix.fix44.GenericFix44OrderMessageFactory;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.SimpleOrder;
import quickfix.field.ExecInst;
import quickfix.field.HandlInst;
import quickfix.field.LocateReqd;
import quickfix.field.TransactTime;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

/**
 * RealTick order message factory.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class RTFixOrderMessageFactory extends GenericFix44OrderMessageFactory {


    @Override
    public NewOrderSingle createNewOrderMessage(final SimpleOrder order, final String clOrdID) {

        NewOrderSingle newOrder = super.createNewOrderMessage(order, clOrdID);

        newOrder.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC));
        newOrder.set(new LocateReqd(true));
        newOrder.set(new ExecInst(String.valueOf(order.isDirect() ? ExecInst.HELD : ExecInst.NOT_HELD)));

        // handling for accounts
        Account account = order.getAccountInitialized();
        if (account.getExtAccount() != null) {
            newOrder.set(new quickfix.field.Account(account.getExtAccount()));
        }

        return newOrder;
    }

    @Override
    public OrderCancelReplaceRequest createModifyOrderMessage(final SimpleOrder order, final String clOrdID) {

        OrderCancelReplaceRequest replaceRequest = super.createModifyOrderMessage(order, clOrdID);

        replaceRequest.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC));
        replaceRequest.set(new LocateReqd(true));
        replaceRequest.set(new ExecInst(String.valueOf(order.isDirect() ? ExecInst.HELD : ExecInst.NOT_HELD)));

        // handling for accounts
        Account account = order.getAccountInitialized();
        if (account.getExtAccount() != null) {
            replaceRequest.set(new quickfix.field.Account(account.getExtAccount()));
        }

        return replaceRequest;
    }

    @Override
    public OrderCancelRequest createOrderCancelMessage(final SimpleOrder order, final String clOrdID) {

        OrderCancelRequest cancelRequest = super.createOrderCancelMessage(order, clOrdID);

        // handling for accounts
        Account account = order.getAccountInitialized();
        if (account.getExtAccount() != null) {
            cancelRequest.set(new quickfix.field.Account(account.getExtAccount()));
        }

        cancelRequest.set(new TransactTime(new Date()));

        return cancelRequest;
    }

}
