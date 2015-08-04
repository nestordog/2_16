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
import java.util.Date;

import ch.algotrader.enumeration.Direction;
import ch.algotrader.util.DateTimeUtil;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TickImpl extends Tick {

    private static final long serialVersionUID = 7518020445322413106L;

    @Override
    public BigDecimal getCurrentValue() {
        return getLast();
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
