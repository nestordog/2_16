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
package ch.algotrader.entity;

import ch.algotrader.entity.marketData.MarketDataEvent;
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
 *
 * @version $Revision$ $Date$
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
    public double getMarketPrice(MarketDataEvent marketDataEvent) {

        if (isOpen()) {

            if (marketDataEvent != null) {
                return marketDataEvent.getMarketValueDouble(getDirection());
            } else {
                return Double.NaN;
            }
        } else {
            return 0.0;
        }
    }

    @Override
    public double getMarketValue(MarketDataEvent marketDataEvent) {

        if (isOpen()) {

            return getQuantity() * getSecurity().getSecurityFamily().getContractSize() * getMarketPrice(marketDataEvent);
        } else {
            return 0.0;
        }
    }

    @Override
    public double getAveragePrice() {

        return getCost() / getQuantity() / getSecurity().getSecurityFamily().getContractSize();
    }

    @Override
    public double getUnrealizedPL(MarketDataEvent marketDataEvent) {

        return getMarketValue(marketDataEvent) - getCost();
    }

    @Override
    public double getMaxLoss(MarketDataEvent marketDataEvent) {

        if (isOpen() && getExitValue() != null) {

            double maxLossPerItem;
            if (Direction.LONG.equals(getDirection())) {
                maxLossPerItem = getMarketPrice(marketDataEvent) - getExitValue().doubleValue();
            } else {
                maxLossPerItem = getExitValue().doubleValue() - getMarketPrice(marketDataEvent);
            }
            return -getQuantity() * getSecurity().getSecurityFamily().getContractSize() * maxLossPerItem;
        } else {
            return 0.0;
        }
    }

    @Override
    public double getExposure(MarketDataEvent marketDataEvent, MarketDataEvent underlyingMarketDataEvent) {

        return getMarketValue(marketDataEvent) * getSecurity().getLeverage(marketDataEvent, underlyingMarketDataEvent);
    }

    @Override
    public CurrencyAmountVO getAttribution(MarketDataEvent marketDataEvent) {

        double amount = 0;
        Currency currency = null;
        SecurityFamily securityFamily = getSecurity().getSecurityFamily();

        // Forex are attributed in their baseCurrency
        if (getSecurity() instanceof Forex) {

            currency = ((Forex) getSecurity()).getBaseCurrency();
            amount = getQuantity() * securityFamily.getContractSize();

            // Futures on Forex are attributed in the base currenty for their underlying baseCurrency
        } else if (getSecurity() instanceof Future && getSecurity().getUnderlying() != null && getSecurity().getUnderlying() instanceof Forex) {

            Forex forex = (Forex) getSecurity().getUnderlying();
            currency = forex.getBaseCurrency();
            amount = getQuantity() * securityFamily.getContractSize();

            // everything else is attributed in their currency
        } else {
            currency = securityFamily.getCurrency();
            amount = getMarketValue(marketDataEvent);
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
