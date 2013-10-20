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

import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.TIF;
import quickfix.field.ExpireTime;
import quickfix.field.HandlInst;
import quickfix.field.OrderID;
import quickfix.field.TargetSubID;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import java.util.Date;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class RTFixOrderServiceImpl extends RTFixOrderServiceBase {

    private static final long serialVersionUID = 1030392480992545177L;

    private final String targetSubId;

    public RTFixOrderServiceImpl(final String targetSubId) {
        super();
        this.targetSubId = targetSubId;
    }

    public RTFixOrderServiceImpl() {
        this("DEMO");
    }

    @Override
    protected void handleSendOrder(
            final SimpleOrder order, final NewOrderSingle newOrder) throws Exception {
        if (this.targetSubId != null) {
            newOrder.getHeader().setField(new TargetSubID(this.targetSubId));
        }
        if (order.getTif() != null) {
            newOrder.set(getTimeInForce(order.getTif()));
            if (order.getTif() == TIF.GTD && order.getTifDate() != null) {
                newOrder.set(new ExpireTime(order.getTifDate()));
            }
        }
        newOrder.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC));
    }

    @Override
    protected void handleModifyOrder(
            final SimpleOrder order, final OrderCancelReplaceRequest replaceRequest) throws Exception {
        if (this.targetSubId != null) {
            replaceRequest.getHeader().setField(new TargetSubID(this.targetSubId));
        }
        replaceRequest.set(new OrderID(order.getExtId()));
        if (order.getTif() != null) {
            replaceRequest.set(getTimeInForce(order.getTif()));
            if (order.getTif() == TIF.GTD && order.getTifDate() != null) {
                replaceRequest.set(new ExpireTime(order.getTifDate()));
            }
        }
        replaceRequest.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC));
        replaceRequest.set(new TransactTime(new Date()));
    }

    @Override
    protected void handleCancelOrder(
            final SimpleOrder order, final OrderCancelRequest cancelRequest) throws Exception {
        if (this.targetSubId != null) {
            cancelRequest.getHeader().setField(new TargetSubID(this.targetSubId));
        }
        cancelRequest.set(new OrderID(order.getExtId()));
        cancelRequest.set(new TransactTime(new Date()));
    }

    private TimeInForce getTimeInForce(final TIF tif) {
        switch (tif) {
        case DAY:
            return new TimeInForce(TimeInForce.DAY);
        case GTC:
            return new TimeInForce(TimeInForce.GOOD_TILL_CANCEL);
        case GTD:
            return new TimeInForce(TimeInForce.GOOD_TILL_DATE);
        case OPG:
            return new TimeInForce(TimeInForce.AT_THE_OPENING);
        }
        return null;
    }

}
