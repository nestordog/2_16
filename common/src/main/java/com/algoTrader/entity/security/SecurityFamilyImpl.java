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
package com.algoTrader.entity.security;

import java.math.BigDecimal;
import java.text.ChoiceFormat;

import com.algoTrader.util.RoundUtil;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SecurityFamilyImpl extends SecurityFamily {

    private static final long serialVersionUID = -2318908709333325986L;

    @Override
    public String toString() {

        return getName();
    }

    @Override
    public BigDecimal getTickSize(BigDecimal price, boolean upwards) {

        return RoundUtil.getBigDecimal(getTickSize(price.doubleValue(), upwards), getScale());
    }

    @Override
    public double getTickSize(double price, boolean upwards) {

        // add or subtract a very small amount to the price to get the tickSize just above or below the trigger
        double adjustedPrice = upwards ? price * 1.00000000001 : price / 1.00000000001;
        return Double.valueOf(new ChoiceFormat(getTickSizePattern()).format(adjustedPrice));
    }


    @Override
    public BigDecimal getTotalCommission() {

        if (getExecutionCommission() != null && getClearingCommission() != null) {
            return getExecutionCommission().add(getClearingCommission());
        } else if (getExecutionCommission() == null) {
            return getClearingCommission();
        } else if (getClearingCommission() == null) {
            return getExecutionCommission();
        } else {
            return null;
        }
    }

    @Override
    public BigDecimal adjustPrice(BigDecimal price, int ticks) {

        if (ticks > 0) {
            for (int i = 0; i < ticks; i++) {
                price = price.add(getTickSize(price, true));
            }
        } else if (ticks < 0) {
            for (int i = 0; i > ticks; i--) {
                price = price.subtract(getTickSize(price, false));
            }
        }
        return price;
    }

    @Override
    public int getSpreadTicks(BigDecimal bid, BigDecimal ask) {

        int ticks = 0;
        BigDecimal price = bid;
        if (bid.compareTo(ask) <= 0) {
            while (price.compareTo(ask) < 0) {
                ticks++;
                price = adjustPrice(price, 1);
            }
        } else {
            while (price.compareTo(ask) > 0) {
                ticks--;
                price = adjustPrice(price, -1);
            }
        }
        return ticks;
    }
}
