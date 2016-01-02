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
package ch.algotrader.adapter.ib;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.Validate;

import com.ib.client.Contract;
import com.ib.client.Execution;

import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.Index;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
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
import ch.algotrader.util.DateTimeLegacy;

/**
 * Utility class providing conversion methods for IB specific types.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class IBUtil {

    private static final DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter executionFormat = DateTimeFormatter.ofPattern("yyyyMMdd  HH:mm:ss");
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.#######");

    public static Contract getContract(Security security) {

        SecurityFamily securityFamily = security.getSecurityFamily();
        Validate.notNull(securityFamily.getExchange(), "securityFamily.exchange");

        Contract contract = new Contract();
        contract.m_exchange = securityFamily.getExchange().getIbCode();

        // use Conid if available
        if (security.getConid() != null) {

            contract.m_conId = Integer.parseInt(security.getConid());

        } else {

            contract.m_currency = securityFamily.getCurrency().toString();

            if (security instanceof Option) {

                Validate.notNull(securityFamily.getSymbolRoot(Broker.IB.name()), "securityFamily.baseSymbol");

                Option option = (Option) security;

                contract.m_secType = "OPT";
                contract.m_symbol = securityFamily.getSymbolRoot(Broker.IB.name());
                contract.m_primaryExch = securityFamily.getExchange().getIbCode();
                contract.m_strike = option.getStrike().doubleValue();
                contract.m_right = option.getOptionType().toString();
                contract.m_multiplier = decimalFormat.format(securityFamily.getContractSize(Broker.IB.name()));
                contract.m_expiry = dayFormat.format(DateTimeLegacy.toLocalDate(option.getExpiration()));

            } else if (security instanceof Future) {

                Validate.notNull(securityFamily.getSymbolRoot(Broker.IB.name()), "securityFamily.baseSymbol");

                Future future = (Future) security;

                contract.m_secType = "FUT";
                contract.m_symbol = securityFamily.getSymbolRoot(Broker.IB.name());
                contract.m_multiplier = decimalFormat.format(securityFamily.getContractSize(Broker.IB.name()));
                contract.m_expiry = future.getMonthYear();

            } else if (security instanceof Forex) {

                contract.m_secType = "CASH";
                contract.m_symbol = ((Forex) security).getBaseCurrency().name();

            } else if (security instanceof Stock) {

                Validate.notNull(security.getSymbol(), "securityFamily.symbol");

                contract.m_secType = "STK";
                contract.m_symbol = security.getSymbol();
                contract.m_primaryExch = securityFamily.getExchange().getIbCode();

            } else if (security instanceof Index) {

                Validate.notNull(security.getSymbol(), "securityFamily.symbol");

                contract.m_secType = "IND";
                contract.m_symbol = security.getSymbol();

            } else {

                throw new IllegalArgumentException("unsupported security type " + ClassUtils.getShortClassName(security.getClass()));
            }
        }

        return contract;
    }

    public static String getIBOrderType(Order order) {

        if (order instanceof MarketOrder) {
            if (order.getTif() != null && TIF.ATC.equals(order.getTif())) {
                return "MOC";
            } else {
                return "MKT";
            }
        } else if (order instanceof LimitOrder) {
            if (order.getTif() != null && TIF.ATC.equals(order.getTif())) {
                return "LOC";
            } else {
                return "LMT";
            }
        } else if (order instanceof StopOrder) {
            return "STP";
        } else if (order instanceof StopLimitOrder) {
            return "STP LMT";
        } else {
            throw new IllegalArgumentException("unsupported order type " + order.getClass().getName());
        }
    }

    public static String getIBSide(Side side) {

        if (side.equals(Side.BUY)) {
            return "BUY";
        } else if (side.equals(Side.SELL)) {
            return "SELL";
        } else if (side.equals(Side.SELL_SHORT)) {
            return "SSHORT";
        } else {
            throw new IllegalArgumentException("unknow side " + side);
        }
    }

    public static Date getExecutionDateTime(Execution execution) {

        try {
            return DateTimeLegacy.parseAsLocalDateTime(execution.m_time, executionFormat);
        } catch (DateTimeParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date getLastDateTime(String input) {

        return new Date(Long.parseLong(input + "000"));
    }

    public static Side getSide(Execution execution) {

        String sideString = execution.m_side;
        if ("BOT".equals(sideString)) {
            return Side.BUY;
        } else if ("SLD".equals(sideString)) {
            return Side.SELL;
        } else {
            throw new IllegalArgumentException("unknow side " + sideString);
        }
    }

    public static Status getStatus(String status, int filled) {

        if ("Submitted".equals(status) ||
                "PreSubmitted".equals(status) ||
                "PendingSubmit".equals(status) ||
                "PendingCancel".equals(status)) {
            if (filled == 0) {
                return Status.SUBMITTED;
            } else {
                return Status.PARTIALLY_EXECUTED;
            }
        } else if ("Filled".equals(status)) {
            return Status.EXECUTED;
        } else if ("ApiCancelled".equals(status) ||
                "Cancelled".equals(status)) {
            return Status.CANCELED;
        } else if ("Inactive".equals(status)) {
            return Status.REJECTED;
        } else {
            throw new IllegalArgumentException("unknown orderStatus " + status);
        }
    }
}
