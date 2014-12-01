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

import ch.algotrader.util.diff.convert.ValueConverter;

/**
 * Interface defining a column of a CSV file.
 */
public interface CsvColumn {
    /** The column name*/
    String name();

    /** The column index*/
    int index();

    /** Returns a converter to convert a CSV string value into the appropriate type*/
    ValueConverter<?> converter();
}
