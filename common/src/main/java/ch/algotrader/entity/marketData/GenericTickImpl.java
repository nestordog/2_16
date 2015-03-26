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

import ch.algotrader.enumeration.Direction;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class GenericTickImpl extends GenericTick {

    private static final long serialVersionUID = 6171811094429421819L;

    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss SSS");

    @Override
    public BigDecimal getCurrentValue() {
        if (getMoneyValue() != null) {
            return getMoneyValue();
        } else if (getDoubleValue() != null) {
            return new BigDecimal(getDoubleValue());
        } else if (getIntValue() != null) {
            return new BigDecimal(getIntValue());
        } else {
            return new BigDecimal(0);
        }
    }

    @Override
    public BigDecimal getMarketValue(Direction direction) {
        return getCurrentValue();
    }

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();

        buffer.append(getSecurity());
        buffer.append(",");
        buffer.append(getDateTime() != null ? format.format(getDateTime()) : null);
        buffer.append(",");
        buffer.append(getTickType());
        buffer.append("=");

        if (getMoneyValue() != null) {
            buffer.append(getMoneyValue());
        } else if (getDoubleValue() != null) {
            buffer.append(getDoubleValue());
        } else if (getIntValue() != null) {
            buffer.append(getIntValue());
        }

        buffer.append(",feedType=");
        buffer.append(getFeedType());

        return buffer.toString();
    }

}
