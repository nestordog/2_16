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
package ch.algotrader.adapter.tt;

import java.time.LocalDate;

import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.OptionType;

/**
 * Trading Technologies security definition.
 */
public class TTSecurityDefVO {

    private final String symbol;
    private final String id;
    private final String type;
    private final String exchange;
    private final String description;
    private final Currency currency;
    private final LocalDate maturityDate;
    private final LocalDate expiryDate;
    private final OptionType optionType;
    private final Double strikePrice;

    public TTSecurityDefVO(
            final String symbol, final String id, final String type, final String exchange, final String description,
            final Currency currency, final LocalDate maturityDate, final LocalDate expiryDate,
            final OptionType optionType, final Double strikePrice) {
        this.symbol = symbol;
        this.id = id;
        this.type = type;
        this.exchange = exchange;
        this.description = description;
        this.currency = currency;
        this.maturityDate = maturityDate;
        this.expiryDate = expiryDate;
        this.optionType = optionType;
        this.strikePrice = strikePrice;
    }

    public TTSecurityDefVO(
            final String symbol, final String id, final String type, final String exchange, final String description,
            final Currency currency) {
        this(symbol, id, type, description, exchange, currency, null, null, null, null);
    }

    public TTSecurityDefVO(
            final String symbol, final String id, final String type, final String exchange, final String description,
            final Currency currency, final LocalDate maturityDate, final LocalDate expiryDate) {
        this(symbol, id, type, exchange, description, currency, maturityDate, expiryDate, null, null);
    }

    public String getSymbol() {
        return symbol;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getExchange() {
        return exchange;
    }

    public String getDescription() {
        return description;
    }

    public Currency getCurrency() {
        return currency;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public Double getStrikePrice() {
        return strikePrice;
    }

    @Override
    public String toString() {
        return "{" +
                "symbol='" + symbol + '\'' +
                ", securityId='" + id + '\'' +
                ", type='" + type + '\'' +
                ", exchange='" + exchange + '\'' +
                ", description='" + description + '\'' +
                ", currency=" + currency +
                ", maturityDate=" + maturityDate +
                ", expiryDate=" + expiryDate +
                ", optionType='" + optionType + '\'' +
                ", strikePrice=" + strikePrice +
                '}';
    }

}
