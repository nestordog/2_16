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
package ch.algotrader.adapter.ftx;

import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.adapter.fix.FixUtil;
import ch.algotrader.adapter.fix.fix44.GenericFix44OrderMessageFactory;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.enumeration.TIF;
import quickfix.field.CumQty;
import quickfix.field.HandlInst;
import quickfix.field.OrdType;
import quickfix.field.TimeInForce;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

/**
 *  Fortex order message factory.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class FTXFixOrderMessageFactory extends GenericFix44OrderMessageFactory {

    public FTXFixOrderMessageFactory() {
        super(new FTXSymbologyResolver());
    }

    @Override
    protected TimeInForce resolveTimeInForce(final TIF tif) {
        switch (tif) {
            case GTC:
                return new TimeInForce(TimeInForce.GOOD_TILL_CANCEL);
            case IOC:
                return new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL);
            case FOK:
                return new TimeInForce(TimeInForce.FILL_OR_KILL);
            default:
                throw new FixApplicationException("Unsupported time-in-force " + tif);
        }
    }

    @Override
    public NewOrderSingle createNewOrderMessage(final SimpleOrder order, final String clOrdID) {
        if (order instanceof StopLimitOrder) {
            throw new FixApplicationException("Forex does not support STOP_LIMIT orders");
        }
        final NewOrderSingle message = super.createNewOrderMessage(order, clOrdID);
        message.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE));
        if (!message.isSetTimeInForce()) {
            message.set(new TimeInForce(TimeInForce.GOOD_TILL_CANCEL));
        }
        return message;
    }

    @Override
    public OrderCancelReplaceRequest createModifyOrderMessage(final SimpleOrder order, final String clOrdID) {
        final OrderCancelReplaceRequest message = super.createModifyOrderMessage(order, clOrdID);
        message.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE));
        if (!message.isSetTimeInForce()) {
            message.set(new TimeInForce(TimeInForce.GOOD_TILL_CANCEL));
        }
        return message;
    }

    public OrderCancelReplaceRequest createModifyOrderMessage(final SimpleOrder order, final String clOrdID, final long filledQty) {
        final OrderCancelReplaceRequest message = createModifyOrderMessage(order, clOrdID);
        message.setDouble(CumQty.FIELD, filledQty);
        return message;
    }

    @Override
    public OrderCancelRequest createOrderCancelMessage(SimpleOrder order, String clOrdID) {
        final OrderCancelRequest message = super.createOrderCancelMessage(order, clOrdID);
        final OrdType fixOrderType = FixUtil.getFixOrderType(order);
        message.setField(fixOrderType);
        return message;

    }
}
