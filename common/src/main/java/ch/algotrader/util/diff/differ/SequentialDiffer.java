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
package ch.algotrader.util.diff.differ;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ch.algotrader.util.diff.reader.CsvReader;

/**
 * Uses a sequence of asserters called sequentially. The first asserter may
 * for instance skip some lines to prepare for the real assert operation performed
 * by the second asserter in the sequence.
 */
public class SequentialDiffer implements CsvDiffer {

    private final List<CsvDiffer> asserters;

    public SequentialDiffer(CsvDiffer... asserters) {
        this.asserters = Arrays.asList(asserters);
    }

    @Override
    public int diffLines(CsvReader expectedReader, CsvReader actualReader) throws IOException {
        int linesCompared = 0;
        for (final CsvDiffer asserter : asserters) {
            linesCompared += asserter.diffLines(expectedReader, actualReader);
        }
        return linesCompared;
    }

}
