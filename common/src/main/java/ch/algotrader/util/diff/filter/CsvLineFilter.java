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
package ch.algotrader.util.diff.filter;

import ch.algotrader.util.diff.reader.CsvLine;

/**
 * Filter to for a {@link CsvLine} for instance selecting only rows matching
 * a selection criteria.
 */
public interface CsvLineFilter {
    /**
     * Returns true to accept the given line and false if should be filtered out.
     *
     * @param line the line to test
     * @return true to accept and false to reject the line
     */
    boolean accept(CsvLine line);
}
