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
package ch.algotrader.util.diff.convert;

/**
 * Defines type and converter for a value.
 *
 * @param <T> the value type
 */
public interface ValueConverter<T> {
    /**
     * Returns the class representing the type of the value
     */
    Class<? extends T> type();

    /**
     * Converts the given string {@code value} into the appropriate type and
     * returns it. Throws an exception if the conversion fails.
     *
     * @param column name of the column; used in exception if the conversion fails
     * @param value the string value to convert
     */
    T convert(String column, String value);
}
