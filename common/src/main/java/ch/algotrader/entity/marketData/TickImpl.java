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
package ch.algotrader.entity.marketData;

import java.math.BigDecimal;
import java.util.Date;

import ch.algotrader.enumeration.Direction;
import ch.algotrader.util.DateTimeUtil;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class TickImpl extends Tick {

    private static final long serialVersionUID = 7518020445322413106L;

    @Override
    public BigDecimal getCurrentValue() {

        if (getBid() != null && getAsk() != null) {
            int scale = Math.max(getBid().scale(), getAsk().scale());
            return RoundUtil.getBigDecimal((getAsk().doubleValue() + getBid().doubleValue()) / 2.0, scale);
        } else if (getBid() != null) {
            return getBid();
        } else if (getAsk() != null) {
            return getAsk();
        } else {
            return getLast();
        }
    }

    @Override
    public BigDecimal getMarketValue(Direction direction) {

        if (Direction.LONG.equals(direction)) {
            return getBid() != null ? getBid() : getLast();
        } else if (Direction.SHORT.equals(direction)) {
            return getAsk() != null ? getAsk() : getLast();
        } else {
            return getLast();
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

        StringBuilder buffer = new StringBuilder();

        buffer.append(getSecurity());
        buffer.append(",");
        final Date dateTime = getDateTime();
        if (dateTime != null) {
            DateTimeUtil.formatLocalZone(dateTime.toInstant(), buffer);
        }
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

}
