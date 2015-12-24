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
package ch.algotrader.entity;

import java.math.BigDecimal;
import java.util.Date;

import ch.algotrader.entity.marketData.MarketDataEventI;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Direction;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.vo.CurrencyAmountVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class PositionImpl extends Position {

    private static final long serialVersionUID = -2679980079043322328L;

    /**
     * used by hibernate hql expression to instantiate a virtual position
     */
    public PositionImpl() {
        super();
    }

    /**
     * used by hibernate hql expression to instantiate a virtual position
     */
    public PositionImpl(long quantity, Strategy strategy, Security security) {
        super();
        setQuantity(quantity);
        setStrategy(strategy);
        setSecurity(security);
    }

    /**
     * used by hibernate hql expression to instantiate a virtual position
     */
    public PositionImpl(long quantity, Security security) {
        super();
        setQuantity(quantity);
        setSecurity(security);
    }

    /**
     * used by hibernate hql expression to instantiate a virtual position
     */
    public PositionImpl(long quantity, BigDecimal cost, Security security) {
        super();
        setQuantity(quantity);
        setCost(cost);
        setSecurity(security);
    }

    @Override
    public Direction getDirection() {

        if (getQuantity() < 0) {
            return Direction.SHORT;
        } else if (getQuantity() > 0) {
            return Direction.LONG;
        } else {
            return Direction.FLAT;
        }
    }

    @Override
    public BigDecimal getMarketPrice(MarketDataEventI marketDataEvent) {

        if (isOpen()) {

            if (marketDataEvent != null) {
                return marketDataEvent.getMarketValue(getDirection());
            } else {
                return null;
            }
        } else {
            return RoundUtil.getBigDecimal(0.0);
        }
    }

    @Override
    public BigDecimal getMarketValue(MarketDataEventI marketDataEvent) {

        if (isOpen()) {

            return RoundUtil.getBigDecimal(getQuantity() * getSecurity().getSecurityFamily().getContractSize() * getMarketPrice(marketDataEvent).doubleValue());
        } else {
            return RoundUtil.getBigDecimal(0.0);
        }
    }

    @Override
    public double getAveragePriceDouble() {

        return getCost().doubleValue() / getQuantity() / getSecurity().getSecurityFamily().getContractSize();
    }

    @Override
    public BigDecimal getAveragePrice() {

        return RoundUtil.getBigDecimal(getAveragePriceDouble(), getSecurity().getSecurityFamily().getScale());
    }

    @Override
    public BigDecimal getUnrealizedPL(MarketDataEventI marketDataEvent) {

        return getMarketValue(marketDataEvent).subtract(getCost());
    }

    @Override
    public BigDecimal getExposure(MarketDataEventI marketDataEvent, MarketDataEventI underlyingMarketDataEvent, Date currentTime) {

        return RoundUtil.getBigDecimal(getMarketValue(marketDataEvent).doubleValue() * getSecurity().getLeverage(marketDataEvent, underlyingMarketDataEvent, currentTime));
    }

    @Override
    public CurrencyAmountVO getAttribution(MarketDataEventI marketDataEvent) {

        double amount = 0;
        Currency currency = null;
        SecurityFamily securityFamily = getSecurity().getSecurityFamily();

        // Forex are attributed in their baseCurrency
        if (getSecurity() instanceof Forex) {

            currency = ((Forex) getSecurity()).getBaseCurrency();
            amount = getQuantity() * securityFamily.getContractSize();

            // Futures on Forex are attributed in the base currently for their underlying baseCurrency
        } else if (getSecurity() instanceof Future && getSecurity().getUnderlying() != null && getSecurity().getUnderlying() instanceof Forex) {

            Forex forex = (Forex) getSecurity().getUnderlying();
            currency = forex.getBaseCurrency();
            amount = getQuantity() * securityFamily.getContractSize();

            // everything else is attributed in their currency
        } else {
            currency = securityFamily.getCurrency();
            amount = getMarketValue(marketDataEvent).doubleValue();
        }

        CurrencyAmountVO currencyAmount = new CurrencyAmountVO();
        currencyAmount.setCurrency(currency);
        currencyAmount.setAmount(RoundUtil.getBigDecimal(amount, securityFamily.getScale()));

        return currencyAmount;
    }

    @Override
    public boolean isOpen() {

        return getQuantity() != 0;
    }

    @Override
    public boolean isCashPosition() {

        return getSecurity() instanceof Forex;
    }

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();
        buffer.append(getQuantity());
        buffer.append(",");
        buffer.append(getSecurity());
        buffer.append(",");
        buffer.append(getStrategy());
        return buffer.toString();
    }

}
