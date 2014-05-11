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
package ch.algotrader.adapter.lmax;

import java.util.Date;

import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.adapter.fix.FixUtil;
import ch.algotrader.adapter.fix.fix44.Fix44OrderMessageFactory;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.enumeration.TIF;
import quickfix.field.ClOrdID;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.StopPx;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

/**
 *  LMAX order message factory.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class LMAXFixOrderMessageFactory implements Fix44OrderMessageFactory {

    private static final double MULTPLIER = 10000.0;

    protected TimeInForce resolveTimeInForce(final SimpleOrder order) throws FixApplicationException {

        TIF tif = order.getTif();
        if (tif == null) {

            return null;
        }
        if (order instanceof MarketOrder) {

            if (tif != TIF.IOC && tif != TIF.FOK) {

                throw new FixApplicationException("Time in force '" + tif + "' is not supported by LMAX for market orders");
            }
        } else if (order instanceof LimitOrder) {

            if (tif != TIF.DAY && tif != TIF.GTC && tif != TIF.IOC && tif != TIF.FOK) {

                throw new FixApplicationException("Time in force '" + tif + "' is not supported by LMAX for limit orders");
            }
        } else if (order instanceof StopOrder) {

            if (tif != TIF.DAY && tif != TIF.GTC) {

                throw new FixApplicationException("Time in force '" + tif + "' is not supported by LMAX for stop orders");
            }
        }

        return FixUtil.getTimeInForce(tif);
    }

    protected SecurityID resolveSecurityID(final Security security) throws FixApplicationException {

        String lmaxid = security.getLmaxid();
        if (lmaxid == null) {
            throw new FixApplicationException(security + " is not supported by LMAX");
        }
        return new SecurityID(lmaxid);
    }

    protected OrderQty resolveOrderQty(final SimpleOrder order) {

        long quantity = order.getQuantity();
        if (order.getSecurity() instanceof Forex) {
            if ((quantity % 1000) != 0) {
                throw new FixApplicationException("FX orders on LMAX need to be multiples of 1000");
            } else {
                return new OrderQty(quantity / MULTPLIER);
            }
        } else {
            throw new FixApplicationException("LMAX interface currently only supports FX orders");
        }
    }

    @Override
    public NewOrderSingle createNewOrderMessage(final SimpleOrder order, final String clOrdID) throws FixApplicationException {

        NewOrderSingle message = new NewOrderSingle();
        message.set(new ClOrdID(clOrdID));
        message.set(new TransactTime(new Date()));

        message.set(resolveSecurityID(order.getSecurityInitialized()));
        message.set(new SecurityIDSource("8"));

        message.set(FixUtil.getFixSide(order.getSide()));
        message.set(resolveOrderQty(order));

        if (order instanceof MarketOrder) {

            message.set(new OrdType(OrdType.MARKET));

        } else if (order instanceof LimitOrder) {

            message.set(new OrdType(OrdType.LIMIT));
            message.set(new Price(((LimitOrder) order).getLimit().doubleValue()));

        } else if (order instanceof StopOrder) {

            message.set(new OrdType(OrdType.STOP));
            message.set(new StopPx(((StopOrder) order).getStop().doubleValue()));

        } else {

            throw new FixApplicationException("Order type " + order.getClass().getName() + " is not supported by LMAX");
        }

        if (order.getTif() != null) {

            message.set(resolveTimeInForce(order));
        }

        return message;
    }

    @Override
    public OrderCancelReplaceRequest createModifyOrderMessage(final SimpleOrder order, final String clOrdID) throws FixApplicationException {

        if (!(order instanceof LimitOrder)) {

            throw new FixApplicationException("Order modification of type " + order.getClass().getName() + " is not supported by LMAX");
        }

        // get origClOrdID and assign a new clOrdID
        String origClOrdID = order.getIntId();

        OrderCancelReplaceRequest message = new OrderCancelReplaceRequest();

        message.set(new ClOrdID(clOrdID));
        message.set(new OrigClOrdID(origClOrdID));
        message.set(new TransactTime(new Date()));

        message.set(resolveSecurityID(order.getSecurityInitialized()));
        message.set(new SecurityIDSource("8"));

        message.set(FixUtil.getFixSide(order.getSide()));
        message.set(resolveOrderQty(order));

        message.set(new OrdType(OrdType.LIMIT));
        message.set(new Price(((LimitOrder) order).getLimit().doubleValue()));

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
        message.set(new TransactTime(new Date()));

        message.set(resolveSecurityID(order.getSecurityInitialized()));
        message.set(new SecurityIDSource("8"));

        message.set(FixUtil.getFixSide(order.getSide()));
        message.set(resolveOrderQty(order));

        return message;
    }

}
