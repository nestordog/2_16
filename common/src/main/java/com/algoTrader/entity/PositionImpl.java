package com.algoTrader.entity;

import com.algoTrader.entity.marketData.MarketDataEvent;
import com.algoTrader.entity.security.Forex;
import com.algoTrader.entity.security.ForexI;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.Direction;
import com.algoTrader.util.PositionUtil;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.CurrencyAmountVO;

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

    /**
     * always positive
     */
    @Override
    public double getMarketPriceDouble() {

        if (isOpen()) {

            MarketDataEvent marketDataEvent = getSecurity().getCurrentMarketDataEvent();
            if (marketDataEvent != null) {
                return marketDataEvent.getRelevantPriceDouble(getDirection());
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

    /**
     * short positions: negative long positions: positive
     */
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

    /**
     * always positive
     */
    @Override
    public double getAveragePriceDouble() {

        return PositionUtil.getAveragePrice(getSecurity(), getTransactions(), true);
    }

    @Override
    public double getAveragePriceBaseDouble() {

        return getAveragePriceDouble() * getSecurity().getFXRateBase();
    }

    /**
     * in days
     */
    @Override
    public double getAverageAge() {

        return PositionUtil.getAverageAge(getTransactions());
    }

    /**
     * short positions: negative long positions: positive
     */
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
    public double getExitValueDoubleBase() {

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

        return getQuantity() + " " + getSecurity();
    }
}
