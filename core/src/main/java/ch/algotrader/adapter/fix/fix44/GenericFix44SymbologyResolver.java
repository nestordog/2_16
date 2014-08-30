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

import java.text.SimpleDateFormat;
import java.util.Date;

import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.adapter.fix.FixUtil;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.OptionType;
import quickfix.field.CFICode;
import quickfix.field.ContractMultiplier;
import quickfix.field.Currency;
import quickfix.field.MaturityDate;
import quickfix.field.MaturityMonthYear;
import quickfix.field.SecurityType;
import quickfix.field.StrikePrice;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

/**
 * Generic FIX/4.4 symbology resolver implementation.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class GenericFix44SymbologyResolver implements Fix44SymbologyResolver {

    private final SimpleDateFormat monthFormat;
    private final SimpleDateFormat dayFormat;

    public GenericFix44SymbologyResolver() {
        this.monthFormat = new SimpleDateFormat("yyyyMM");
        this.dayFormat = new SimpleDateFormat("yyyyMMdd");
    }

    protected String formatYM(final Date date) {
        if (date == null) {
            return null;
        }
        synchronized (this.monthFormat) {
            return this.monthFormat.format(date);
        }
    }

    protected String formatYMD(final Date date) {
        if (date == null) {
            return null;
        }
        synchronized (this.dayFormat) {
            return this.dayFormat.format(date);
        }
    }

    @Override
    public void resolve(final NewOrderSingle message, final Security security, final Broker broker) throws FixApplicationException {

        message.set(FixUtil.getFixSymbol(security, broker));

        // populate security information
        if (security instanceof Option) {

            Option option = (Option) security;

            message.set(new SecurityType(SecurityType.OPTION));
            message.set(new Currency(option.getSecurityFamilyInitialized().getCurrency().toString()));
            message.set(new CFICode("O" + (OptionType.PUT.equals(option.getType()) ? "P" : "C")));
            message.set(new StrikePrice(option.getStrike().doubleValue()));
            message.set(new ContractMultiplier(option.getSecurityFamilyInitialized().getContractSize()));
            message.set(new MaturityMonthYear(formatYM(option.getExpiration())));
            message.set(new MaturityDate(formatYMD(option.getExpiration())));

        } else if (security instanceof Future) {

            Future future = (Future) security;

            message.set(new SecurityType(SecurityType.FUTURE));
            message.set(new Currency(future.getSecurityFamilyInitialized().getCurrency().toString()));
            message.set(new MaturityMonthYear(formatYM(future.getExpiration())));
            message.set(new MaturityDate(formatYMD(future.getExpiration())));

        } else if (security instanceof Forex) {

            message.set(new SecurityType(SecurityType.CASH));
            message.set(new Currency(security.getSecurityFamilyInitialized().getCurrency().getValue()));

        } else if (security instanceof Stock) {

            Stock stock = (Stock) security;

            message.set(new SecurityType(SecurityType.COMMON_STOCK));
            message.set(new Currency(stock.getSecurityFamilyInitialized().getCurrency().toString()));
        }
    }

    @Override
    public void resolve(final OrderCancelReplaceRequest message, final Security security, final Broker broker) throws FixApplicationException {

        message.set(FixUtil.getFixSymbol(security, broker));

        // populate security information
        if (security instanceof Option) {

            Option option = (Option) security;

            message.set(new SecurityType(SecurityType.OPTION));
            message.set(new CFICode("O" + (OptionType.PUT.equals(option.getType()) ? "P" : "C")));
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
    }

    @Override
    public void resolve(final OrderCancelRequest message, final Security security, final Broker broker) throws FixApplicationException {

        message.set(FixUtil.getFixSymbol(security, broker));

        // populate security information
        if (security instanceof Option) {

            Option option = (Option) security;

            message.set(new SecurityType(SecurityType.OPTION));
            message.set(new CFICode()); // todo
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
    }

}
