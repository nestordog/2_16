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
        buffer.append(",");
        DateTimeUtil.formatLocalZone(getDateTime().toInstant(), buffer);
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

}
