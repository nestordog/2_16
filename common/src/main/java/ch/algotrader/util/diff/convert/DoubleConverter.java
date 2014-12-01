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
package ch.algotrader.util.diff.convert;

/**
 * Converter for double values.
 */
public class DoubleConverter extends AbstractValueConverter<Double> {

    /**
     * The singleton instance.
     */
    public static final DoubleConverter INSTANCE = new DoubleConverter();

    public DoubleConverter() {
        super(Double.class);
    }

    @Override
    public Double convert(String column, String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("[" + column + "] cannot parse double value: " + value, e);
        }
    }
}
