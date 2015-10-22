/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.adapter.rt;

import java.util.Date;

import ch.algotrader.adapter.fix.fix44.Fix44SymbologyResolver;
import ch.algotrader.adapter.fix.fix44.GenericFix44OrderMessageFactory;
import ch.algotrader.entity.trade.SimpleOrder;
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
 */
public class RTFixOrderMessageFactory extends GenericFix44OrderMessageFactory {


    public RTFixOrderMessageFactory(final Fix44SymbologyResolver symbologyResolver) {
        super(symbologyResolver);
    }

    @Override
    public NewOrderSingle createNewOrderMessage(final SimpleOrder order, final String clOrdID) {

        NewOrderSingle newOrder = super.createNewOrderMessage(order, clOrdID);

        newOrder.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC));
        newOrder.set(new LocateReqd(true));

        return newOrder;
    }

    @Override
    public OrderCancelReplaceRequest createModifyOrderMessage(final SimpleOrder order, final String clOrdID) {

        OrderCancelReplaceRequest replaceRequest = super.createModifyOrderMessage(order, clOrdID);

        replaceRequest.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC));
        replaceRequest.set(new LocateReqd(true));

        return replaceRequest;
    }

    @Override
    public OrderCancelRequest createOrderCancelMessage(final SimpleOrder order, final String clOrdID) {

        OrderCancelRequest cancelRequest = super.createOrderCancelMessage(order, clOrdID);

        cancelRequest.set(new TransactTime(new Date()));

        return cancelRequest;
    }

}
