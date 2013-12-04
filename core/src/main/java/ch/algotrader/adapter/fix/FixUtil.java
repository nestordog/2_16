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
package ch.algotrader.adapter.fix;

import quickfix.field.CumQty;
import quickfix.field.ExecType;
import quickfix.field.OrdType;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TIF;

/**
 * Utility class providing conversion methods for Fix specific types.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FixUtil {

    public static Side getSide(quickfix.field.Side side) {

        if (side.getValue() == quickfix.field.Side.BUY) {
            return Side.BUY;
        } else if (side.getValue() == quickfix.field.Side.SELL) {
            return Side.SELL;
        } else if (side.getValue() == quickfix.field.Side.SELL_SHORT) {
            return Side.SELL_SHORT;
        } else {
            throw new IllegalArgumentException("unknow side " + side);
        }
    }

    public static Status getStatus(ExecType execType, CumQty cumQty) {

        if (execType.getValue() == ExecType.NEW) {
            return Status.SUBMITTED;
        } else if (execType.getValue() == ExecType.PARTIAL_FILL) {
            return Status.PARTIALLY_EXECUTED;
        } else if (execType.getValue() == ExecType.FILL) {
            return Status.EXECUTED;
        } else if (execType.getValue() == ExecType.CANCELED || execType.getValue() == ExecType.REJECTED
                || execType.getValue() == ExecType.DONE_FOR_DAY || execType.getValue() == ExecType.EXPIRED) {
            return Status.CANCELED;
        } else if (execType.getValue() == ExecType.REPLACE) {
            if (cumQty.getValue() == 0) {
                return Status.SUBMITTED;
            } else {
                return Status.PARTIALLY_EXECUTED;
            }
        } else {
            throw new IllegalArgumentException("unknown execType " + execType.getValue());
        }
    }

    public static Symbol getFixSymbol(Security security, Broker broker) {

        if (security instanceof Option) {
            return new Symbol(security.getSecurityFamily().getBaseSymbol(broker));
        } else if (security instanceof Future) {
            return new Symbol(security.getSecurityFamily().getBaseSymbol(broker));
        } else if (security instanceof Forex) {
            return new Symbol(((Forex) security).getBaseCurrency().getValue());
        } else if (security instanceof Stock) {
            return new Symbol(security.getSymbol());
        } else {
            throw new UnsupportedOperationException("unsupported security type " + security.getClass());
        }
    }

    public static quickfix.field.Side getFixSide(Side side) {

        if (side.equals(Side.BUY)) {
            return new quickfix.field.Side(quickfix.field.Side.BUY);
        } else if (side.equals(Side.SELL)) {
            return new quickfix.field.Side(quickfix.field.Side.SELL);
        } else if (side.equals(Side.SELL_SHORT)) {
            return new quickfix.field.Side(quickfix.field.Side.SELL_SHORT);
        } else {
            throw new IllegalArgumentException("unknow side " + side);
        }
    }

    public static OrdType getFixOrderType(Order order) {

        if (order instanceof MarketOrder) {
            return new OrdType(OrdType.MARKET);
        } else if (order instanceof LimitOrder) {
            return new OrdType(OrdType.LIMIT);
        } else if (order instanceof StopOrder) {
            return new OrdType(OrdType.STOP);
        } else if (order instanceof StopLimitOrder) {
            return new OrdType(OrdType.STOP_LIMIT);
        } else {
            throw new IllegalArgumentException("unsupported order type " + order.getClass().getName());
        }
    }

    public static TimeInForce getTimeInForce(final TIF tif) {

        switch (tif) {
            case DAY:
                return new TimeInForce(TimeInForce.DAY);
            case GTC:
                return new TimeInForce(TimeInForce.GOOD_TILL_CANCEL);
            case GTD:
                return new TimeInForce(TimeInForce.GOOD_TILL_DATE);
            case OPG:
                return new TimeInForce(TimeInForce.AT_THE_OPENING);
            default:
                throw new IllegalArgumentException("unknown timeInForce " + tif);
        }
    }
}
