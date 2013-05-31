/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.entity;

import ch.algotrader.util.PositionUtil;
import ch.algotrader.util.RoundUtil;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.marketData.MarketDataEvent;
import com.algoTrader.entity.security.Forex;
import com.algoTrader.entity.security.ForexI;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.entity.strategy.Strategy;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.Direction;
import com.algoTrader.vo.CurrencyAmountVO;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
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
    public double getMarketPriceDouble() {

        if (isOpen()) {

            MarketDataEvent marketDataEvent = getSecurity().getCurrentMarketDataEvent();
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
    public double getMarketPriceBaseDouble() {

        return getMarketPriceDouble() * getSecurity().getFXRateBase();
    }

    @Override
    public double getMarketValueDouble() {

        if (isOpen()) {

            return getQuantity() * getSecurity().getSecurityFamily().getContractSize() * getMarketPriceDouble();
        } else {
            return 0.0;
        }
    }

    @Override
    public double getMarketValueBaseDouble() {

        return getMarketValueDouble() * getSecurity().getFXRateBase();
    }

    @Override
    public double getAveragePriceDouble() {

        return PositionUtil.getAveragePrice(getSecurity(), getTransactions(), true);
    }

    @Override
    public double getAveragePriceBaseDouble() {

        return getAveragePriceDouble() * getSecurity().getFXRateBase();
    }

    @Override
    public double getAverageAge() {

        return PositionUtil.getAverageAge(getTransactions());
    }

    @Override
    public double getCostDouble() {

        if (isOpen()) {
            return PositionUtil.getCost(getTransactions(), true);
        } else {
            return 0.0;
        }
    }

    @Override
    public double getCostBaseDouble() {

        return getCostDouble() * getSecurity().getFXRateBase();
    }

    @Override
    public double getUnrealizedPLDouble() {
        if (isOpen()) {

            return getMarketValueDouble() - getCostDouble();
        } else {
            return 0.0;
        }
    }

    @Override
    public double getUnrealizedPLBaseDouble() {

        return getUnrealizedPLDouble() * getSecurity().getFXRateBase();
    }

    @Override
    public double getRealizedPLDouble() {

        return PositionUtil.getRealizedPL(getTransactions(), true);
    }

    @Override
    public double getRealizedPLBaseDouble() {

        return getRealizedPLDouble() * getSecurity().getFXRateBase();
    }

    @Override
    public double getExitValueDouble() {

        if (getExitValue() != null) {
            return getExitValue().doubleValue();
        } else {
            return 0.0;
        }
    }

    @Override
    public double getExitValueBaseDouble() {

        return getExitValueDouble() * getSecurity().getFXRateBase();
    }

    @Override
    public double getMaintenanceMarginDouble() {

        if (isOpen() && getMaintenanceMargin() != null) {
            return getMaintenanceMargin().doubleValue();
        } else {
            return 0.0;
        }
    }

    @Override
    public double getMaintenanceMarginBaseDouble() {

        return getMaintenanceMarginDouble() * getSecurity().getFXRateBase();
    }

    @Override
    public double getMaxLossDouble() {

        if (isOpen() && getExitValue() != null) {

            double maxLossPerItem;
            if (Direction.LONG.equals(getDirection())) {
                maxLossPerItem = getMarketPriceDouble() - getExitValueDouble();
            } else {
                maxLossPerItem = getExitValueDouble() - getMarketPriceDouble();
            }
            return -getQuantity() * getSecurity().getSecurityFamily().getContractSize() * maxLossPerItem;
        } else {
            return 0.0;
        }
    }

    @Override
    public double getMaxLossBaseDouble() {

        return getMaxLossDouble() * getSecurity().getFXRateBase();
    }

    @Override
    public double getExposure() {

        return getMarketValueDouble() * getSecurity().getLeverage();
    }

    @Override
    public CurrencyAmountVO getAttribution() {

        double amount = 0;
        Currency currency = null;
        SecurityFamily securityFamily = getSecurity().getSecurityFamily();

        // Forex and ForexFutures are attributed in their baseCurrency
        if (getSecurity() instanceof ForexI) {

            currency = ((ForexI) getSecurity()).getBaseCurrency();
            amount = getQuantity() * securityFamily.getContractSize();

        } else {
            currency = securityFamily.getCurrency();
            amount = getMarketValueDouble();
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

        return getQuantity() + "," + getSecurity();
    }
}
