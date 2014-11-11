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

import ch.algotrader.util.diff.value.ValueAsserter;

/**
 * Extension of {@link CsvColumn} adding the capability to compare and assert values
 * of this column for instance with values from the actual file.
 */
public interface AssertableCsvColumn extends CsvColumn {
    /**
     * Returns the asserter that can compare and assert values from this column.
     * @return the value asserter for this column
     */
    ValueAsserter<?> getValueAsserter();
}
