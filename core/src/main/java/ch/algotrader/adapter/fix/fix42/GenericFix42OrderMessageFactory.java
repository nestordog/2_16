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
package ch.algotrader.adapter.fix.fix42;

import java.text.SimpleDateFormat;
import java.util.Date;

import ch.algotrader.adapter.fix.FixUtil;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.trade.LimitOrderI;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.StopOrderI;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.enumeration.TIF;
import quickfix.field.ClOrdID;
import quickfix.field.ContractMultiplier;
import quickfix.field.Currency;
import quickfix.field.ExpireTime;
import quickfix.field.MaturityDay;
import quickfix.field.MaturityMonthYear;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.PutOrCall;
import quickfix.field.SecurityType;
import quickfix.field.StopPx;
import quickfix.field.StrikePrice;
import quickfix.field.TransactTime;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

/**
 * Generic FIX/4.2 order message factory implementation.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class GenericFix42OrderMessageFactory implements Fix42OrderMessageFactory {

    private final SimpleDateFormat monthFormat;
    private final SimpleDateFormat dayFormat;

    public GenericFix42OrderMessageFactory() {
        this.monthFormat = new SimpleDateFormat("yyyyMM");
        this.dayFormat = new SimpleDateFormat("dd");
    }

    protected String formatYM(final Date date) {
        if (date == null) {
            return null;
        }
        synchronized (this.monthFormat) {
            return this.monthFormat.format(date);
        }
    }

    protected String formatD(final Date date) {
        if (date == null) {
            return null;
        }
        synchronized (this.dayFormat) {
            return this.dayFormat.format(date);
        }
    }

    @Override
    public NewOrderSingle createNewOrderMessage(final SimpleOrder order, final String clOrdID) {

        NewOrderSingle message = new NewOrderSingle();

        // common info
        message.set(new TransactTime(new Date()));
        message.set(new ClOrdID(String.valueOf(clOrdID)));

        Security security = order.getSecurityInitialized();
        Broker broker = order.getAccountInitialized().getBroker();

        message.set(FixUtil.getFixSymbol(security, broker));
        message.set(FixUtil.getFixSide(order.getSide()));
        message.set(new OrderQty(order.getQuantity()));
        message.set(FixUtil.getFixOrderType(order));

        // populate security information
        if (security instanceof Option) {

            Option option = (Option) security;

            message.set(new SecurityType(SecurityType.OPTION));
            message.set(new Currency(option.getSecurityFamily().getCurrency().toString()));
            message.set(new PutOrCall(OptionType.PUT.equals(option.getType()) ? PutOrCall.PUT : PutOrCall.CALL));
            message.set(new StrikePrice(option.getStrike().doubleValue()));
            message.set(new ContractMultiplier(option.getSecurityFamily().getContractSize()));
            message.set(new MaturityMonthYear(formatYM(option.getExpiration())));
            message.set(new MaturityDay(formatD(option.getExpiration())));

        } else if (security instanceof Future) {

            Future future = (Future) security;

            message.set(new SecurityType(SecurityType.FUTURE));
            message.set(new Currency(future.getSecurityFamily().getCurrency().toString()));
            message.set(new MaturityMonthYear(formatYM(future.getExpiration())));
            message.set(new MaturityDay(formatD(future.getExpiration())));

        } else if (security instanceof Forex) {

            message.set(new SecurityType(SecurityType.CASH));
            message.set(new Currency(security.getSecurityFamily().getCurrency().getValue()));

        } else if (security instanceof Stock) {

            Stock stock = (Stock) security;

            message.set(new SecurityType(SecurityType.COMMON_STOCK));
            message.set(new Currency(stock.getSecurityFamily().getCurrency().toString()));
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

        return message;
    }

    @Override
    public OrderCancelReplaceRequest createModifyOrderMessage(final SimpleOrder order, final String clOrdID) {

        String origClOrdID = order.getIntId();

        OrderCancelReplaceRequest message = new OrderCancelReplaceRequest();

        // common info
        message.set(new ClOrdID(clOrdID));
        message.set(new OrigClOrdID(origClOrdID));

        Security security = order.getSecurityInitialized();
        Broker broker = order.getAccountInitialized().getBroker();

        message.set(FixUtil.getFixSymbol(security, broker));
        message.set(FixUtil.getFixSide(order.getSide()));
        message.set(new OrderQty(order.getQuantity()));
        message.set(FixUtil.getFixOrderType(order));

        // populate security information
        if (security instanceof Option) {

            Option option = (Option) security;

            message.set(new SecurityType(SecurityType.OPTION));
            message.set(new PutOrCall(OptionType.PUT.equals(option.getType()) ? PutOrCall.PUT : PutOrCall.CALL));
            message.set(new StrikePrice(option.getStrike().doubleValue()));
            message.set(new MaturityMonthYear(formatYM(option.getExpiration())));

        } else if (security instanceof Future) {

            Future future = (Future) security;

            message.set(new SecurityType(SecurityType.FUTURE));
            message.set(new MaturityMonthYear(formatYM(future.getExpiration())));

        } else if (security instanceof Forex) {

            message.set(new SecurityType(SecurityType.CASH));

        } else if (security instanceof Stock) {
            message.set(new SecurityType(SecurityType.COMMON_STOCK));
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

        return message;
    }

    @Override
    public OrderCancelRequest createOrderCancelMessage(final SimpleOrder order, final String clOrdID) {

        String origClOrdID = order.getIntId();

        OrderCancelRequest message = new OrderCancelRequest();

        // common info
        message.set(new ClOrdID(clOrdID));
        message.set(new OrigClOrdID(origClOrdID));

        Security security = order.getSecurityInitialized();
        Broker broker = order.getAccountInitialized().getBroker();

        message.set(FixUtil.getFixSymbol(security, broker));
        message.set(FixUtil.getFixSide(order.getSide()));
        message.set(new OrderQty(order.getQuantity()));

        // populate security information
        if (security instanceof Option) {

            Option option = (Option) security;

            message.set(new SecurityType(SecurityType.OPTION));
            message.set(new PutOrCall(OptionType.PUT.equals(option.getType()) ? PutOrCall.PUT : PutOrCall.CALL));
            message.set(new StrikePrice(option.getStrike().doubleValue()));
            message.set(new MaturityMonthYear(formatYM(option.getExpiration())));

        } else if (security instanceof Future) {

            Future future = (Future) security;

            message.set(new SecurityType(SecurityType.FUTURE));
            message.set(new MaturityMonthYear(formatYM(future.getExpiration())));

        } else if (security instanceof Forex) {

            message.set(new SecurityType(SecurityType.CASH));

        } else if (security instanceof Stock) {
            message.set(new SecurityType(SecurityType.COMMON_STOCK));
        }

        return message;
    }

}
