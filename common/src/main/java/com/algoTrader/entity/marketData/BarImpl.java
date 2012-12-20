package com.algoTrader.entity.marketData;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.Direction;
import com.algoTrader.util.RoundUtil;

public class BarImpl extends Bar {

    private static final long serialVersionUID = 6293029012643523737L;

    private static @Value("${simulation}") boolean simulation;
    private static @Value("${simulation.simulateBidAsk}") boolean simulateBidAsk;

    @Override
    public BigDecimal getCurrentValue() {

        return getClose();
    }

    @Override
    public BigDecimal getMarketValue(Direction direction) {

        if (simulation && simulateBidAsk) {

            // tradeable securities with ask = 0 should return a simulated value
            SecurityFamily family = getSecurity().getSecurityFamily();
            if (family.isTradeable()) {

                if (family.getSpreadSlope() == null || family.getSpreadConstant() == null) {
                    throw new IllegalStateException("SpreadSlope and SpreadConstant have to be defined for dummyAsk " + getSecurity());
                }

                // spread depends on the pricePerContract (i.e. spread should be the same
                // for 12.- à contractSize 10 as for 1.20 à contractSize 100)
                double pricePerContract = getClose().doubleValue() * family.getContractSize();
                double spread = pricePerContract * family.getSpreadSlope() + family.getSpreadConstant();
                double relevantPrice = (pricePerContract + (Direction.LONG.equals(direction) ? -1 : 1) * (spread / 2.0)) / family.getContractSize();
                return RoundUtil.getBigDecimal(relevantPrice, family.getScale());
            }
        }

        return getClose();
    }
}
