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
package ch.algotrader.adapter.fxcm;

import java.util.Date;

import quickfix.field.Account;
import quickfix.field.ClOrdID;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;
import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.adapter.fix.FixUtil;
import ch.algotrader.adapter.fix.fix44.Fix44OrderMessageFactory;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.util.PriceUtil;

/**
 *  FXCM order message factory.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class FXCMFixOrderMessageFactory implements Fix44OrderMessageFactory {

    protected TimeInForce resolveTimeInForce(final SimpleOrder order) throws FixApplicationException {

        TIF tif = order.getTif();
        if (tif == null) {

            return null;
        }
        switch (tif) {
            case DAY:
                return new TimeInForce(TimeInForce.DAY);
            case GTC:
                return new TimeInForce(TimeInForce.GOOD_TILL_CANCEL);
            case GTD:
                return new TimeInForce(TimeInForce.GOOD_TILL_DATE);
            case IOC:
                return new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL);
            case FOK:
                return new TimeInForce(TimeInForce.FILL_OR_KILL);
            default:
                throw new FixApplicationException("Time in force " + tif + " not supported by FXCM");
        }
    }

    @Override
    public NewOrderSingle createNewOrderMessage(final SimpleOrder order, final String clOrdID) throws FixApplicationException {

        NewOrderSingle message = new NewOrderSingle();
        message.set(new ClOrdID(clOrdID));

        ch.algotrader.entity.Account account = order.getAccount();
        if (account != null && account.getExtAccount() != null) {
            message.set(new Account(account.getExtAccount()));
        }

        message.set(new TransactTime(new Date()));
        message.set(new Symbol(FXCMUtil.getFXCMSymbol(order.getSecurity())));
        message.set(FixUtil.getFixSide(order.getSide()));
        message.set(new OrderQty(order.getQuantity()));

        if (order instanceof MarketOrder) {

            message.set(new OrdType(OrdType.MARKET));

        } else if (order instanceof LimitOrder) {

            message.set(new OrdType(OrdType.LIMIT));
            LimitOrder limitOrder = (LimitOrder) order;
            message.set(new Price(PriceUtil.denormalizePrice(order, limitOrder.getLimit())));

        } else if (order instanceof StopOrder) {

            message.set(new OrdType(OrdType.STOP));
            StopOrder stopOrder = (StopOrder) order;
            message.set(new StopPx(PriceUtil.denormalizePrice(order, stopOrder.getStop())));

        } else if (order instanceof StopLimitOrder) {

            StopLimitOrder stopLimitOrder = (StopLimitOrder) order;
            message.set(new OrdType(OrdType.STOP_LIMIT));
            message.set(new Price(PriceUtil.denormalizePrice(order, stopLimitOrder.getLimit())));
            message.set(new StopPx(PriceUtil.denormalizePrice(order, stopLimitOrder.getStop())));

        } else {

            throw new FixApplicationException("Order type " + order.getClass().getName() + " is not supported by FXCM");
        }

        if (order.getTif() != null) {

            message.set(resolveTimeInForce(order));
        }

        return message;
    }

    @Override
    public OrderCancelReplaceRequest createModifyOrderMessage(final SimpleOrder order, final String clOrdID) throws FixApplicationException {

        // get origClOrdID and assign a new clOrdID
        String origClOrdID = order.getIntId();

        OrderCancelReplaceRequest message = new OrderCancelReplaceRequest();
        message.set(new ClOrdID(clOrdID));
        message.set(new OrigClOrdID(origClOrdID));

        ch.algotrader.entity.Account account = order.getAccount();
        if (account != null && account.getExtAccount() != null) {
            message.set(new Account(account.getExtAccount()));
        }

        message.set(new TransactTime(new Date()));
        message.set(new Symbol(FXCMUtil.getFXCMSymbol(order.getSecurity())));
        message.set(FixUtil.getFixSide(order.getSide()));
        message.set(new OrderQty(order.getQuantity()));

        if (order instanceof LimitOrder) {

            message.set(new OrdType(OrdType.LIMIT));
            LimitOrder limitOrder = (LimitOrder) order;
            message.set(new Price(PriceUtil.denormalizePrice(order, limitOrder.getLimit())));

        } else if (order instanceof StopOrder) {

            message.set(new OrdType(OrdType.STOP));
            StopOrder stopOrder = (StopOrder) order;
            message.set(new StopPx(PriceUtil.denormalizePrice(order, stopOrder.getStop())));

        } else if (order instanceof StopLimitOrder) {

            StopLimitOrder stopLimitOrder = (StopLimitOrder) order;
            message.set(new OrdType(OrdType.STOP_LIMIT));
            message.set(new Price(PriceUtil.denormalizePrice(order, stopLimitOrder.getLimit())));
            message.set(new StopPx(PriceUtil.denormalizePrice(order, stopLimitOrder.getStop())));

        } else {

            throw new FixApplicationException("Order modification of type " + order.getClass().getName() + " is not supported by FXCM");
        }

        if (order.getTif() != null) {

            message.set(resolveTimeInForce(order));
        }

        return message;
    }

    @Override
    public OrderCancelRequest createOrderCancelMessage(final SimpleOrder order, final String clOrdID) throws FixApplicationException {

        // get origClOrdID and assign a new clOrdID
        String origClOrdID = order.getIntId();

        OrderCancelRequest message = new OrderCancelRequest();
        message.set(new ClOrdID(clOrdID));
        message.set(new OrigClOrdID(origClOrdID));

        ch.algotrader.entity.Account account = order.getAccount();
        if (account != null && account.getExtAccount() != null) {
            message.set(new Account(account.getExtAccount()));
        }

        message.set(new TransactTime(new Date()));
        message.set(new Symbol(FXCMUtil.getFXCMSymbol(order.getSecurity())));
        message.set(FixUtil.getFixSide(order.getSide()));
        message.set(new OrderQty(order.getQuantity()));

        return message;
    }

}
