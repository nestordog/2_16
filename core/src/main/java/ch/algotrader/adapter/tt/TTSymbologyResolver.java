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
package ch.algotrader.adapter.tt;

import java.time.format.DateTimeFormatter;
import java.util.Date;

import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.adapter.fix.fix42.Fix42SymbologyResolver;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.util.DateTimeLegacy;
import quickfix.field.MaturityMonthYear;
import quickfix.field.PutOrCall;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityID;
import quickfix.field.SecurityType;
import quickfix.field.StrikePrice;
import quickfix.field.Symbol;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

/**
 * Trading Technologies symbology resolver implementation.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TTSymbologyResolver implements Fix42SymbologyResolver {

    private final DateTimeFormatter monthFormat;

    public TTSymbologyResolver() {
        this.monthFormat = DateTimeFormatter.ofPattern("yyyyMM");
    }

    protected String formatYM(final Date date) {
        if (date == null) {
            return null;
        }
        return this.monthFormat.format(DateTimeLegacy.toLocalDate(date));
    }

    @Override
    public void resolve(final NewOrderSingle message, final Security security, final String broker) throws FixApplicationException {

        SecurityFamily securityFamily = security.getSecurityFamily();
        Exchange exchange = securityFamily.getExchange();
        String exchangeCode = exchange.getCode();
        String symbolRoot = securityFamily.getSymbolRoot(Broker.TT.name());
        if (security.getTtid() != null) {
            message.set(new SecurityID(security.getTtid()));
        }

        if (security instanceof Option) {

            Option option = (Option) security;

            message.set(new SecurityType(SecurityType.OPTION));
            message.set(new SecurityExchange(exchangeCode));
            message.set(new Symbol(symbolRoot));
            message.set(new MaturityMonthYear(formatYM(option.getExpiration())));

            message.set(new PutOrCall(OptionType.PUT.equals(option.getType()) ? PutOrCall.PUT : PutOrCall.CALL));
            message.set(new StrikePrice(option.getStrike().doubleValue()));

        } else if (security instanceof Future) {

            Future future = (Future) security;

            message.set(new SecurityType(SecurityType.FUTURE));
            message.set(new SecurityExchange(exchangeCode));
            message.set(new Symbol(symbolRoot));
            message.set(new MaturityMonthYear(formatYM(future.getExpiration())));

        } else {

            throw new FixApplicationException("TT interface does not support security class " + security.getClass());
        }
    }

    @Override
    public void resolve(final OrderCancelReplaceRequest message, final Security security, final String broker) throws FixApplicationException {

        SecurityFamily securityFamily = security.getSecurityFamily();
        Exchange exchange = securityFamily.getExchange();
        String exchangeCode = exchange.getCode();
        String symbolRoot = securityFamily.getSymbolRoot(Broker.TT.name());
        if (security.getTtid() != null) {
            message.set(new SecurityID(security.getTtid()));
        }

        if (security instanceof Option) {

            Option option = (Option) security;

            message.set(new SecurityType(SecurityType.OPTION));
            message.set(new SecurityExchange(exchangeCode));
            message.set(new Symbol(symbolRoot));
            message.set(new MaturityMonthYear(formatYM(option.getExpiration())));

            message.set(new PutOrCall(OptionType.PUT.equals(option.getType()) ? PutOrCall.PUT : PutOrCall.CALL));
            message.set(new StrikePrice(option.getStrike().doubleValue()));

        } else if (security instanceof Future) {

            Future future = (Future) security;

            message.set(new SecurityType(SecurityType.FUTURE));
            message.set(new SecurityExchange(exchangeCode));
            message.set(new Symbol(symbolRoot));
            message.set(new MaturityMonthYear(formatYM(future.getExpiration())));

        } else {

            throw new FixApplicationException("TT interface does not support security class " + security.getClass());
        }
    }

    @Override
    public void resolve(final OrderCancelRequest message, final Security security, final String broker) throws FixApplicationException {
    }

}
