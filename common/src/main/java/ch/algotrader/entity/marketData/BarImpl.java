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
import java.util.Objects;

import ch.algotrader.enumeration.Direction;
import ch.algotrader.util.DateTimeUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class BarImpl extends Bar {

    private static final long serialVersionUID = 6293029012643523737L;

    @Override
    public BigDecimal getCurrentValue() {

        return getClose();
    }

    @Override
    public BigDecimal getMarketValue(Direction direction) {

        return getClose();
    }

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();

        buffer.append(getSecurity());
        Date date = getDateTime();
        if (date != null) {
            buffer.append(",");
            DateTimeUtil.formatLocalZone(date.toInstant(), buffer);
        }
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
            return Objects.equals(this.getSecurity(), that.getSecurity()) &&
                    Objects.equals(this.getDateTime(), that.getDateTime()) &&
                    Objects.equals(this.getFeedType(), that.getFeedType()) &&
                    Objects.equals(this.getBarSize(), that.getBarSize());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + Objects.hashCode(getSecurity());
        hash = hash * 37 + Objects.hashCode(getDateTime());
        hash = hash * 37 + Objects.hashCode(getFeedType());
        hash = hash * 37 + Objects.hashCode(getBarSize());
        return hash;
    }
}
