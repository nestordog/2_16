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
package ch.algotrader.adapter.fix.fix44;

import java.util.Date;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.fix.FixUtil;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.trade.LimitOrderI;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.StopOrderI;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.TIF;
import quickfix.field.ClOrdID;
import quickfix.field.ExpireTime;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.StopPx;
import quickfix.field.TransactTime;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

/**
 * Generic FIX/4.4 order message factory implementation.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class GenericFix44OrderMessageFactory implements Fix44OrderMessageFactory {

    private final Fix44SymbologyResolver symbologyResolver;

    public GenericFix44OrderMessageFactory(final Fix44SymbologyResolver symbologyResolver) {
        Validate.notNull(symbologyResolver, "FIX symbology resolver is null");
        this.symbologyResolver = symbologyResolver;
    }

    @Override
    public NewOrderSingle createNewOrderMessage(final SimpleOrder order, final String clOrdID) {

        NewOrderSingle message = new NewOrderSingle();
        Security security = order.getSecurityInitialized();
        Account account = order.getAccountInitialized();
        Broker broker = account.getBroker();

        // common info
        message.set(new ClOrdID(clOrdID));
        message.set(new TransactTime(new Date()));

        symbologyResolver.resolve(message, security, broker);

        message.set(FixUtil.getFixSide(order.getSide()));
        message.set(new OrderQty(order.getQuantity()));
        message.set(FixUtil.getFixOrderType(order));

        // handling for accounts
        if (account.getExtAccount() != null) {
            message.set(new quickfix.field.Account(account.getExtAccount()));
        }

        //set the limit price if order is a limit order or stop limit order
        if (order instanceof LimitOrderI) {
            message.set(new Price(((LimitOrderI) order).getLimit().doubleValue()));
        }

        //set the stop price if order is a stop order or stop limit order
        if (order instanceof StopOrderI) {
            message.set(new StopPx(((StopOrderI) order).getStop().doubleValue()));
        }

        // set TIF
        if (order.getTif() != null) {
            message.set(FixUtil.getTimeInForce(order.getTif()));
            if (order.getTif() == TIF.GTD && order.getTifDateTime() != null) {
                message.set(new ExpireTime(order.getTifDateTime()));
            }
        }

        FixUtil.copyOrderProperties(message, order.getOrderPropertiesInitialized());

        return message;
    }

    @Override
    public OrderCancelReplaceRequest createModifyOrderMessage(final SimpleOrder order, final String clOrdID) {

        // get origClOrdID and assign a new clOrdID
        String origClOrdID = order.getIntId();

        OrderCancelReplaceRequest message = new OrderCancelReplaceRequest();
        Security security = order.getSecurityInitialized();
        Account account = order.getAccountInitialized();
        Broker broker = account.getBroker();

        // common info
        message.set(new ClOrdID(clOrdID));
        message.set(new OrigClOrdID(origClOrdID));
        message.set(new TransactTime(new Date()));

        symbologyResolver.resolve(message, security, broker);

        message.set(FixUtil.getFixSide(order.getSide()));
        message.set(new OrderQty(order.getQuantity()));
        message.set(FixUtil.getFixOrderType(order));

        // handling for accounts
        if (account.getExtAccount() != null) {
            message.set(new quickfix.field.Account(account.getExtAccount()));
        }

        //set the limit price if order is a limit order or stop limit order
        if (order instanceof LimitOrderI) {
            message.set(new Price(((LimitOrderI) order).getLimit().doubleValue()));
        }

        //set the stop price if order is a stop order or stop limit order
        if (order instanceof StopOrderI) {
            message.set(new StopPx(((StopOrderI) order).getStop().doubleValue()));
        }

        // set TIF
        if (order.getTif() != null) {
            message.set(FixUtil.getTimeInForce(order.getTif()));
            if (order.getTif() == TIF.GTD && order.getTifDateTime() != null) {
                message.set(new ExpireTime(order.getTifDateTime()));
            }
        }

        FixUtil.copyOrderProperties(message, order.getOrderPropertiesInitialized());

        return message;
    }

    @Override
    public OrderCancelRequest createOrderCancelMessage(final SimpleOrder order, final String clOrdID) {

        // get origClOrdID and assign a new clOrdID
        String origClOrdID = order.getIntId();

        OrderCancelRequest message = new OrderCancelRequest();
        Security security = order.getSecurityInitialized();
        Account account = order.getAccountInitialized();
        Broker broker = account.getBroker();

        // common info
        message.set(new ClOrdID(clOrdID));
        message.set(new OrigClOrdID(origClOrdID));
        message.set(new TransactTime(new Date()));

        symbologyResolver.resolve(message, security, broker);

        message.set(FixUtil.getFixSide(order.getSide()));
        message.set(new OrderQty(order.getQuantity()));

        // handling for accounts
        if (account.getExtAccount() != null) {
            message.set(new quickfix.field.Account(account.getExtAccount()));
        }

        return message;
    }

}
