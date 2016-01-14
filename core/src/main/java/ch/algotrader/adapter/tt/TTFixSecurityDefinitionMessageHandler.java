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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.BrokerAdapterException;
import ch.algotrader.adapter.fix.fix42.AbstractFix42MessageHandler;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.OptionType;
import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.SessionID;
import quickfix.field.EventDate;
import quickfix.field.EventType;
import quickfix.field.NoEvents;
import quickfix.field.SecurityType;
import quickfix.fix42.SecurityDefinition;

/**
 * Trading Technologies specific FIX market data handler.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TTFixSecurityDefinitionMessageHandler extends AbstractFix42MessageHandler {

    private static final Logger LOGGER = LogManager.getLogger(TTFixSecurityDefinitionMessageHandler.class);

    private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final TTPendingRequests pendingRequests;

    public TTFixSecurityDefinitionMessageHandler(final TTPendingRequests pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public void onMessage(final SecurityDefinition securityDefinition, final SessionID sessionID) throws FieldNotFound {

        String requestId = securityDefinition.getSecurityReqID().getValue();
        TTPendingRequest<TTSecurityDefVO> pendingRequest = this.pendingRequests.getSecurityDefinitionRequest(requestId);
        if (pendingRequest == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Unexpected requestId: {}", requestId);
            }
            return;
        }
        try {
            TTSecurityDefVO vo = parse(securityDefinition);
            pendingRequest.add(vo);

            int totalNum = securityDefinition.getTotalNumSecurities().getValue();
            if (pendingRequest.getResultList().size() >= totalNum) {
                pendingRequest.completed();
                this.pendingRequests.removeSecurityDefinitionRequest(requestId);
            }
        } catch (FieldNotFound ex) {
            pendingRequest.fail(new BrokerAdapterException("Unexpected SecurityDefinition format", ex));
            throw ex;
        } catch (BrokerAdapterException ex) {
            pendingRequest.fail(ex);
            throw ex;
        }
    }

    TTSecurityDefVO parse(final SecurityDefinition securityDefinition) throws BrokerAdapterException, FieldNotFound {

        String symbol = securityDefinition.getSymbol().getValue();
        String altSymbol = securityDefinition.isSetField(10455) ? securityDefinition.getString(10455) : null;
        String securityId = securityDefinition.getSecurityID().getValue();
        String securityType = securityDefinition.getSecurityType().getValue();
        String securityExchange = securityDefinition.getSecurityExchange().getValue();
        String desc = securityDefinition.isSetSecurityDesc() ? securityDefinition.getSecurityDesc().getValue() : null;
        String ccyString = securityDefinition.isSetCurrency() ? securityDefinition.getCurrency().getValue() : null;
        Currency currency = null;
        if (ccyString != null) {
            try {
                currency = Currency.valueOf(ccyString);
            } catch (IllegalArgumentException ex) {
            }
        }
        switch (securityType) {
            case SecurityType.FUTURE:
                return new TTSecurityDefVO(symbol, altSymbol, securityId, securityType, securityExchange, desc, currency,
                        parseMaturityDate(securityDefinition), parseExpiryDate(securityDefinition));
            case SecurityType.OPTION:
                return new TTSecurityDefVO(symbol, altSymbol, securityId, securityType, securityExchange, desc, currency,
                        parseMaturityDate(securityDefinition), parseExpiryDate(securityDefinition),
                        parseOptionType(securityDefinition), securityDefinition.getStrikePrice().getValue());
            default:
                return new TTSecurityDefVO(symbol, altSymbol, securityId, securityType, securityExchange, desc, currency);
        }
    }

    LocalDate parseMaturityDate(final SecurityDefinition securityDefinition) throws BrokerAdapterException, FieldNotFound {
        String s = securityDefinition.getMaturityMonthYear().getValue();
        if (s.isEmpty() || s.equals("0")) {
            return null;
        }
        try {
            TemporalAccessor parsed = YEAR_MONTH_FORMAT.parse(s);
            int year = parsed.get(ChronoField.YEAR);
            int month = parsed.get(ChronoField.MONTH_OF_YEAR);
            int day;
            if (securityDefinition.isSetMaturityDay()) {
                String ss = securityDefinition.getMaturityDay().getValue();
                try {
                    day = Integer.parseInt(ss);
                } catch (NumberFormatException ex) {
                    throw new BrokerAdapterException("Invalid maturity day: " + ss);
                }
            } else {
                day = 1;
            }
            return LocalDate.of(year, month, day);
        } catch (DateTimeParseException ex) {
            throw new BrokerAdapterException("Invalid maturity month/year: " + s);
        }
    }

    LocalDate parseExpiryDate(final SecurityDefinition securityDefinition) throws BrokerAdapterException, FieldNotFound {
        for (Group group : securityDefinition.getGroups(NoEvents.FIELD)) {
            int type = group.getInt(EventType.FIELD);
            if (type == 5) { // EXPIRY_DATE
                String s = group.getString(EventDate.FIELD);
                try {
                    return DATE_FORMAT.parse(s, LocalDate::from);
                } catch (DateTimeParseException ex) {
                    throw new BrokerAdapterException("Invalid expiry date: " + s);
                }
            }
        }
        return null;
    }

    OptionType parseOptionType(final SecurityDefinition securityDefinition) throws BrokerAdapterException, FieldNotFound {
        int i = securityDefinition.getPutOrCall().getValue();
        switch (i) {
            case 0:
                return OptionType.PUT;
            case 1:
                return OptionType.CALL;
            default:
                throw new BrokerAdapterException("Unexpected put or call value: " + i);
        }
    }

}
