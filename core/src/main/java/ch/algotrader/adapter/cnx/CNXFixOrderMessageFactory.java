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
package ch.algotrader.adapter.cnx;

import java.util.Date;

import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.adapter.fix.FixUtil;
import ch.algotrader.adapter.fix.fix44.Fix44OrderMessageFactory;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.entity.trade.StopOrderI;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TIF;
import quickfix.IntField;
import quickfix.field.ClOrdID;
import quickfix.field.Currency;
import quickfix.field.ExpireTime;
import quickfix.field.HandlInst;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.Product;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

/**
 *  Currenex order message factory.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class CNXFixOrderMessageFactory implements Fix44OrderMessageFactory {

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
            case IOC:
                if (order instanceof StopOrderI) {

                    throw new FixApplicationException(tif + " cannot be used with stop-loss orders");
                }
                return new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL);
            case FOK:
                if (order instanceof StopOrderI) {

                    throw new FixApplicationException(tif + " cannot be used with stop-loss orders");
                }
                return new TimeInForce(TimeInForce.FILL_OR_KILL);
            case GTD:
                return new TimeInForce(TimeInForce.GOOD_TILL_DATE);
            default:
                throw new FixApplicationException("Time in force " + tif + " not supported");
        }
    }

    @Override
    public NewOrderSingle createNewOrderMessage(final SimpleOrder order, final String clOrdID) throws FixApplicationException {

        Security security = order.getSecurityInitialized();
        if (!(security instanceof Forex)) {

            throw new FixApplicationException("Currenex supports forex orders only");
        }
        Forex forex = (Forex) security;

        NewOrderSingle message = new NewOrderSingle();
        message.set(new ClOrdID(clOrdID));
        message.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE));
        message.set(new Symbol(CNXUtil.getCNXSymbol(forex)));
        message.set(new Product(Product.CURRENCY));
        message.set(FixUtil.getFixSide(order.getSide()));
        message.set(new TransactTime(new Date()));
        message.set(new OrderQty(order.getQuantity()));
        message.set(new Currency(forex.getBaseCurrency().name()));

        if (order instanceof MarketOrder) {

            message.set(new OrdType(OrdType.FOREX_MARKET));

        } else if (order instanceof LimitOrder) {

            LimitOrder limitOrder = (LimitOrder) order;
            message.set(new OrdType(OrdType.FOREX_LIMIT));
            message.set(new Price((limitOrder.getLimit().doubleValue())));

        } else if (order instanceof StopOrder) {

            StopOrder stopOrder = (StopOrder) order;
            message.set(new OrdType(OrdType.STOP));
            message.set(new StopPx(stopOrder.getStop().doubleValue()));

        } else if (order instanceof StopLimitOrder) {

            StopLimitOrder stopLimitOrder = (StopLimitOrder) order;
            message.set(new OrdType(OrdType.STOP_LIMIT));
            message.set(new Price((stopLimitOrder.getLimit().doubleValue())));
            message.set(new StopPx(stopLimitOrder.getStop().doubleValue()));

        } else {

            throw new FixApplicationException("Order type " + order.getClass().getName() + " is not supported by Currenex");
        }

        if (order instanceof StopOrderI) {

            int stopSide = order.getSide() == Side.BUY ? 1 : 2; // 1 - Bid; 2 - Offer
            message.setField(new IntField(7534, stopSide));
        }

        TIF tif = order.getTif();
        if (tif != null) {

            message.set(resolveTimeInForce(order));

            if (tif == TIF.GTD) {

                Date tifDateTime = order.getTifDateTime();
                if (tifDateTime == null) {

                    throw new FixApplicationException("Good till date is not set");
                }
                message.set(new ExpireTime(tifDateTime));

            }
        }

        return message;
    }

    @Override
    public OrderCancelReplaceRequest createModifyOrderMessage(final SimpleOrder order, final String clOrdID) throws FixApplicationException {

        Security security = order.getSecurityInitialized();
        if (!(security instanceof Forex)) {

            throw new FixApplicationException("Currenex supports forex orders only");
        }
        Forex forex = (Forex) security;

        String origClOrdID = order.getIntId();
        String extId = order.getExtId();

        OrderCancelReplaceRequest message = new OrderCancelReplaceRequest();
        message.set(new ClOrdID(clOrdID));
        message.set(new OrigClOrdID(origClOrdID));
        message.set(new OrderID(extId));
        message.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE));
        message.set(new Symbol(CNXUtil.getCNXSymbol(forex)));
        message.set(new Product(Product.CURRENCY));
        message.set(FixUtil.getFixSide(order.getSide()));
        message.set(new TransactTime(new Date()));
        message.set(new OrderQty(order.getQuantity()));
        message.set(new Currency(forex.getBaseCurrency().name()));

        if (order instanceof MarketOrder) {

            message.set(new OrdType(OrdType.FOREX_MARKET));

        } else if (order instanceof LimitOrder) {

            LimitOrder limitOrder = (LimitOrder) order;
            message.set(new OrdType(OrdType.FOREX_LIMIT));
            message.set(new Price((limitOrder.getLimit().doubleValue())));

        } else if (order instanceof StopOrder) {

            StopOrder stopOrder = (StopOrder) order;
            message.set(new OrdType(OrdType.STOP));
            message.set(new StopPx(stopOrder.getStop().doubleValue()));

        } else if (order instanceof StopLimitOrder) {

            StopLimitOrder stopLimitOrder = (StopLimitOrder) order;
            message.set(new OrdType(OrdType.STOP_LIMIT));
            message.set(new Price((stopLimitOrder.getLimit().doubleValue())));
            message.set(new StopPx(stopLimitOrder.getStop().doubleValue()));

        } else {

            throw new FixApplicationException("Order type " + order.getClass().getName() + " is not supported by Currenex");
        }

        TIF tif = order.getTif();
        if (tif != null && tif == TIF.GTD) {

            Date tifDateTime = order.getTifDateTime();
            if (tifDateTime != null) {

                message.set(new ExpireTime(tifDateTime));
            }
        }

        return message;
    }

    @Override
    public OrderCancelRequest createOrderCancelMessage(final SimpleOrder order, final String clOrdID) throws FixApplicationException {

        Security security = order.getSecurityInitialized();
        if (!(security instanceof Forex)) {

            throw new FixApplicationException("Currenex supports forex orders only");
        }
        Forex forex = (Forex) security;

        String origClOrdID = order.getIntId();
        String extId = order.getExtId();

        OrderCancelRequest message = new OrderCancelRequest();
        message.set(new ClOrdID(clOrdID));
        message.set(new OrigClOrdID(origClOrdID));
        message.set(new OrderID(extId));
        message.set(new Symbol(CNXUtil.getCNXSymbol(forex)));
        message.set(new Product(Product.CURRENCY));
        message.set(FixUtil.getFixSide(order.getSide()));
        message.set(new TransactTime(new Date()));

        if (order instanceof MarketOrder) {

            message.setField(new OrdType(OrdType.FOREX_MARKET));

        } else if (order instanceof LimitOrder) {

            message.setField(new OrdType(OrdType.FOREX_LIMIT));

        } else if (order instanceof StopOrder) {

            message.setField(new OrdType(OrdType.STOP));

        } else if (order instanceof StopLimitOrder) {

            message.setField(new OrdType(OrdType.STOP_LIMIT));

        } else {

            throw new FixApplicationException("Order type " + order.getClass().getName() + " is not supported by Currenex");
        }

        return message;
    }

}
