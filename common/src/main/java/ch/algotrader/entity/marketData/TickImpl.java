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
package ch.algotrader.entity.marketData;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.enumeration.Direction;
import ch.algotrader.util.ObjectUtil;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TickImpl extends Tick {

    private static final long serialVersionUID = 7518020445322413106L;

    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss SSS");

    /**
     * Note: ticks that are not valid (i.e. low volume) are not fed into esper, so we don't need to check
     */
    @Override
    public BigDecimal getCurrentValue() {

        int scale = getSecurity().getSecurityFamily().getScale();
        CommonConfig commonConfig = ConfigLocator.instance().getCommonConfig();
        if (commonConfig.isSimulation()) {
            if ((super.getBid().doubleValue() != 0) && (super.getAsk().doubleValue() != 0)) {
                return RoundUtil.getBigDecimal((getAsk().doubleValue() + getBid().doubleValue()) / 2.0, scale);
            } else {
                return getLast();
            }
        } else {
            if (this.getSecurity().getSecurityFamily().isTradeable() || this.getSecurity().getSecurityFamily().isSynthetic()) {

                // options just before expiration might not have a BID anymore
                if (getBid() != null && getAsk() != null) {
                    return RoundUtil.getBigDecimal((getAsk().doubleValue() + getBid().doubleValue()) / 2.0, scale);
                } else if (getBid() != null) {
                    return RoundUtil.getBigDecimal(getBid().doubleValue(), scale);
                } else if (getAsk() != null) {
                    return RoundUtil.getBigDecimal(getAsk().doubleValue(), scale);
                } else {
                    return getLast();
                }

            } else {
                return getLast();
            }
        }
    }

    @Override
    public BigDecimal getMarketValue(Direction direction) {

        if (Direction.LONG.equals(direction)) {

            // bid might be null
            return getBid() != null ? getBid() : new BigDecimal(0);
        } else if (Direction.SHORT.equals(direction)) {
            return getAsk();
        } else {
            return new BigDecimal(0);
        }
    }

    @Override
    public BigDecimal getBidAskSpread() {

        return RoundUtil.getBigDecimal(getBidAskSpreadDouble());
    }

    @Override
    public double getBidAskSpreadDouble() {

        if (getBid() != null && getAsk() != null) {
            return getAsk().doubleValue() - getBid().doubleValue();
        } else {
            return 0.0;
        }
    }

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer();

        buffer.append(getSecurity());
        buffer.append(",");
        buffer.append(getDateTime() != null ? format.format(getDateTime()) : null);
        buffer.append(",last=");
        buffer.append(getLast());
        buffer.append(",lastDateTime=");
        buffer.append(getLastDateTime());
        buffer.append(",bid=");
        buffer.append(getBid());
        buffer.append(",ask=");
        buffer.append(getAsk());
        buffer.append(",volBid=");
        buffer.append(getVolBid());
        buffer.append(",volAsk=");
        buffer.append(getVolAsk());
        buffer.append(",vol=");
        buffer.append(getVol());
        buffer.append(",feedType=");
        buffer.append(getFeedType());

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof Tick) {
            Tick that = (Tick) obj;
            return ObjectUtil.equalsNonNull(this.getSecurity(), that.getSecurity()) &&
                    ObjectUtil.equalsNonNull(this.getDateTime(), that.getDateTime())  &&
                    ObjectUtil.equalsNonNull(this.getFeedType(), that.getFeedType());
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
        return hash;
    }
}
