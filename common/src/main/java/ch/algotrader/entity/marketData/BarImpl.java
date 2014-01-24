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
package ch.algotrader.entity.marketData;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.enumeration.Direction;
import ch.algotrader.util.ObjectUtil;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class BarImpl extends Bar {

    private static final long serialVersionUID = 6293029012643523737L;

    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss SSS");

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
                // for 12.- at contractSize 10 as for 1.20 at contractSize 100)
                double pricePerContract = getClose().doubleValue() * family.getContractSize();
                double spread = pricePerContract * family.getSpreadSlope() + family.getSpreadConstant();
                double relevantPrice = (pricePerContract + (Direction.LONG.equals(direction) ? -1 : 1) * (spread / 2.0)) / family.getContractSize();
                return RoundUtil.getBigDecimal(relevantPrice, family.getScale());
            }
        }

        return getClose();
    }

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer();

        buffer.append(getSecurity());
        buffer.append(",");
        buffer.append(format.format(getDateTime()));
        buffer.append(",open=");
        buffer.append(getOpen());
        buffer.append(",high=");
        buffer.append(getHigh());
        buffer.append(",low=");
        buffer.append(getLow());
        buffer.append(",close=");
        buffer.append(getClose());
        buffer.append(",vol=");
        buffer.append(getVol());
        buffer.append(",barSize=");
        buffer.append(getBarSize());
        buffer.append(",feedType=");
        buffer.append(getFeedType());

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof Bar) {
            Bar that = (Bar) obj;
            return ObjectUtil.equalsNonNull(this.getSecurity(), that.getSecurity()) &&
                    ObjectUtil.equalsNonNull(this.getDateTime(), that.getDateTime()) &&
                    ObjectUtil.equalsNonNull(this.getFeedType(), that.getFeedType()) &&
                    ObjectUtil.equalsNonNull(this.getBarSize(), that.getBarSize());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + ObjectUtil.hashCode(getSecurity());
        hash = hash * 37 + ObjectUtil.hashCode(getDateTime());
        hash = hash * 37 + ObjectUtil.hashCode(getFeedType());
        hash = hash * 37 + ObjectUtil.hashCode(getBarSize());
        return hash;
    }
}
