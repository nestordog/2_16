package com.algoTrader.entity;

import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.enumeration.Direction;
import com.algoTrader.util.PositionUtil;

public class PositionImpl extends Position {

    private static final long serialVersionUID = -2679980079043322328L;

    @Override
    public boolean isOpen() {

        return getQuantity() != 0;
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

            Tick tick = getSecurity().getLastTick();
            if (tick != null) {
                if (getQuantity() < 0) {

                    // short position
                    return tick.getAsk().doubleValue();
                } else {

                    // short position
                    return tick.getBid().doubleValue();
                }
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
    public double getRedemptionValueDouble() {

        if (isOpen() && getExitValue() != null) {

            return -getQuantity() * getSecurity().getSecurityFamily().getContractSize() * getExitValueDouble();
        } else {
            return 0.0;
        }
    }

    @Override
    public double getRedemptionValueBaseDouble() {

        return getRedemptionValueDouble() * getSecurity().getFXRateBase();
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
    public String toString() {

        return getQuantity() + " " + getSecurity();
    }
}
