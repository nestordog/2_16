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
package ch.algotrader.util.diff;

import java.io.File;
import java.util.List;

import ch.algotrader.util.diff.define.CsvColumn;
import ch.algotrader.util.diff.reader.CsvLine;

public interface DiffEntry {
    String getMessage();
    List<?> getGroupValues();
    Context getExpectedContext();
    Context getActualContext();

    interface Context {
        File getFile();
        CsvLine getLine();
        CsvColumn getColumn();
        Object getValue();
    }
}
