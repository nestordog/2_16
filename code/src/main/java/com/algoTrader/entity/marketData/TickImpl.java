package com.algoTrader.entity.marketData;

import java.math.BigDecimal;

import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.RoundUtil;

public class TickImpl extends Tick {

    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");

    private static final long serialVersionUID = 7518020445322413106L;

    /**
     * Note: ticks that are not valid (i.e. low volume) are not fed into esper, so we don't need to check
     */
    public BigDecimal getCurrentValue() {

        int scale = getSecurity().getSecurityFamily().getScale();
        if (simulation) {
            if ((super.getBid().doubleValue() != 0) && (super.getAsk().doubleValue() != 0)) {
                return RoundUtil.getBigDecimal((getAsk().doubleValue() + getBid().doubleValue()) / 2.0, scale);
            } else {
                return getLast();
            }
        } else {
            if (this.getSecurity().getSecurityFamily().isTradeable()) {
                return RoundUtil.getBigDecimal((getAsk().doubleValue() + getBid().doubleValue()) / 2.0, scale);
            } else {
                return getLast();
            }
        }
    }

    public double getCurrentValueDouble() {

        return getCurrentValue().doubleValue();
    }

    public BigDecimal getBid() {

        if (simulation && super.getBid().equals(new BigDecimal(0))) {

            // tradeable securities with bid = 0 should return a simulated value
            SecurityFamily family = getSecurity().getSecurityFamily();
            if (family.isTradeable()) {

                if (family.getSpreadSlope() == null || family.getSpreadConstant() == null) {
                    throw new RuntimeException("SpreadSlope and SpreadConstant have to be defined for dummyBid " + getSecurity().getSymbol());
                }

                // spread depends on the pricePerContract (i.e. spread should be the same
                // for 12.- à contractSize 10 as for 1.20 à contractSize 100)
                double pricePerContract = getLast().doubleValue() * family.getContractSize();
                double spread = pricePerContract * family.getSpreadSlope() + family.getSpreadConstant();
                double dummyBid = (pricePerContract - (spread / 2.0)) / family.getContractSize();
                return RoundUtil.getBigDecimal(dummyBid < 0 ? 0 : dummyBid, family.getScale());
            } else {
                return super.getBid();
            }
        } else {
            return super.getBid();
        }
    }

    public BigDecimal getAsk() {

        if (simulation && super.getAsk().equals(new BigDecimal(0))) {

            // tradeable securities with ask = 0 should return a simulated value
            SecurityFamily family = getSecurity().getSecurityFamily();
            if (family.isTradeable()) {

                if (family.getSpreadSlope() == null || family.getSpreadConstant() == null) {
                    throw new RuntimeException("SpreadSlope and SpreadConstant have to be defined for dummyAsk " + getSecurity().getSymbol());
                }

                // spread depends on the pricePerContract (i.e. spread should be the same
                // for 12.- à contractSize 10 as for 1.20 à contractSize 100)
                double pricePerContract = getLast().doubleValue() * family.getContractSize();
                double spread = pricePerContract * family.getSpreadSlope() + family.getSpreadConstant();
                double dummyAsk = (pricePerContract + (spread / 2.0)) / family.getContractSize();
                return RoundUtil.getBigDecimal(dummyAsk, family.getScale());
            } else {
                return super.getAsk();
            }
        } else {
            return super.getAsk();
        }
    }

    public BigDecimal getSettlement() {

        if (simulation && super.getSettlement().equals(new BigDecimal(0))) {
            return getLast();
        } else {
            return super.getSettlement();
        }
    }

    public void validate() {

        getSecurity().validateTick(this);
    }
}
