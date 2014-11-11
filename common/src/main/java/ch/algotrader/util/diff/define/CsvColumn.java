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
package ch.algotrader.util.diff.define;

import ch.algotrader.util.diff.reader.CsvLine;

/**
 * Interface usually implemented by an enum that defines the columns of a CSV file.
 */
public interface CsvColumn {
    /** The column name, usually the enum constant's name*/
    String name();
    /** The column label*/
    String label();
    /** The column index, usually the same as the enum constant's ordinal*/
    int index();
    /** Returns the appropriate value from the given CSV row, or null if it doesn't exist*/
    Object get(CsvLine row);
    /** Converts a string value into the appropriate type*/
    Object convert(String value);
}
