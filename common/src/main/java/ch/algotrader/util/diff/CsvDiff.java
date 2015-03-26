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
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.util.diff.differ.FileDiffer;
import ch.algotrader.util.diff.reader.CsvLine;

/**
 * Performs a diff of two CSV files based on a {@link FileDiffer}.
 */
public class CsvDiff {

    private static final Logger LOG = LogManager.getLogger(CsvDiff.class);

    public static void diffAndLogAssertionErrors(FileDiffer fileDiffer, File expectedFile, File actualFile) {
        LOG.info("[ASSERT] ====================================================");
        LOG.info("[ASSERT]   expected file: " + expectedFile);
        LOG.info("[ASSERT]     actual file: " + actualFile);
        try {
            final DiffStats stats = fileDiffer.diffFiles(expectedFile, actualFile);
            LOG.info("[ASSERT SUCCEEDED] lines: " + stats);
        } catch (CsvAssertionError e) {
            LOG.error("[ASSSERTION FAILED] " + e.getMessage());
            LOG.error("[ASSSERTION FAILED] files: exp=" + e.getExpectedFile().getName() + ", act=" + e.getActualFile().getName());
            LOG.error("[ASSSERTION FAILED] lines: exp=" + e.getLineString(true, false) + ", act=" + e.getLineString(false, true));
            LOG.error("[ASSSERTION FAILED] group: " + e.getGroupString());
            final int numlen = e.getDiffs().isEmpty() ? 0 : 1 + (int)Math.floor(Math.log10(e.getDiffs().size()));
            LOG.error("[ASSSERTION FAILED] " + spaces(numlen) + "=========================================================================================");
            LOG.error("[ASSSERTION FAILED] " + spaces(numlen) + "NUM |       COLUMN(S)      |       EXPECTED       |        ACTUAL        | MESSAGE");
            LOG.error("[ASSSERTION FAILED] " + spaces(numlen) + "----|----------------------|----------------------|----------------------|---------------");
            for (int i = 0; i < e.getDiffs().size(); i++) {
                final DiffEntry diff = e.getDiffs().get(i);
                final DiffEntry.Context expContext = diff.getExpectedContext();
                final DiffEntry.Context actContext = diff.getActualContext();
                final String expCol = String.valueOf(expContext.getColumn());
                final String actCol = String.valueOf(actContext.getColumn());
                final Object expVal = expContext.getColumn() == null ? expContext.getLine().getRawLine() == null ? "--" : "<line>" : expContext.getValue();
                final Object actVal = actContext.getColumn() == null ? actContext.getLine().getRawLine() == null ? "--" : "<line>" : actContext.getValue();
                final String columns = expCol.equals(actCol) ? expCol : expCol + "/" + actCol;
                LOG.error("[ASSSERTION FAILED]  (" + fixedLen(i+1, numlen) + ") | "//
                        + fixedLen(columns, 20) + " | " + fixedLen(expVal, 20) + " | " + fixedLen(actVal, 20) + " | " //
                        + diff.getMessage());
            }
            LOG.error("[ASSSERTION FAILED] " + spaces(numlen) + "----|-------------------------------------------------------------------------------------");
//            LOG.error("[ASSSERTION FAILED] " + spaces(numlen) + "----|=====================================================================================");
            for (int i = 0; i < e.getDiffs().size(); i++) {
                final DiffEntry diff = e.getDiffs().get(i);
                final CsvLine expLine = diff.getExpectedContext().getLine();
                final CsvLine actLine = diff.getActualContext().getLine();
                LOG.error("[ASSSERTION FAILED]  (" + fixedLen(i+1, numlen) + ") | exp-line: [" + expLine.getLineIndex() + "] " + expLine.getRawLine());
                LOG.error("[ASSSERTION FAILED]   " + spaces(numlen) + "  | act-line: [" + actLine.getLineIndex() + "] " + actLine.getRawLine());
            }
            LOG.error("[ASSSERTION FAILED] " + spaces(numlen) + "=========================================================================================");
            throw e;
        } catch (IOException e) {
            LOG.error("[ASSSERTION FAILED] I/O exception");
            throw new RuntimeException(e);
        }
    }

    private static String fixedLen(Object value, int len) {
        final String s = String.valueOf(value);
        if (value instanceof Number) {
            //right align
            if (s.length() <= len) {
                return spaces(len - s.length()) + s;
            }
            return s.substring(0, Math.max(0, len-2)) + "..".substring(0, Math.min(2, len));
        } else {
            //left align
            if (s.length() < len) {
                return s + spaces(len - s.length());
            }
            return s.substring(0, len);
        }
    }

    private static String spaces(int n) {
        final String spaces = "                                  ";
        return spaces.substring(0, Math.min(n, spaces.length()));
    }
}
