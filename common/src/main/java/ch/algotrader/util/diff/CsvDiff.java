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

    private static final Logger LOGGER = LogManager.getLogger(CsvDiff.class);

    public static void diffAndLogAssertionErrors(FileDiffer fileDiffer, File expectedFile, File actualFile) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("[ASSERT] ====================================================");
            LOGGER.info("[ASSERT]   expected file: {}", expectedFile);
            LOGGER.info("[ASSERT]     actual file: {}", actualFile);
        }
        try {
            if (LOGGER.isInfoEnabled()) {
                final DiffStats stats = fileDiffer.diffFiles(expectedFile, actualFile);
                LOGGER.info("[ASSERT SUCCEEDED] lines: {}", stats);
            }
        } catch (CsvAssertionError e) {
            LOGGER.error("[ASSSERTION FAILED] {}", e.getMessage());
            LOGGER.error("[ASSSERTION FAILED] files: exp={}, act={}", e.getExpectedFile().getName(), e.getActualFile().getName());
            LOGGER.error("[ASSSERTION FAILED] lines: exp={}, act={}", e.getLineString(true, false), e.getLineString(false, true));
            LOGGER.error("[ASSSERTION FAILED] group: {}", e.getGroupString());
            final int numlen = e.getDiffs().isEmpty() ? 0 : 1 + (int)Math.floor(Math.log10(e.getDiffs().size()));
            LOGGER.error("[ASSSERTION FAILED] {}=========================================================================================", spaces(numlen));
            LOGGER.error("[ASSSERTION FAILED] {}NUM |       COLUMN(S)      |       EXPECTED       |        ACTUAL        | MESSAGE", spaces(numlen));
            LOGGER.error("[ASSSERTION FAILED] {}----|----------------------|----------------------|----------------------|---------------", spaces(numlen));
            for (int i = 0; i < e.getDiffs().size(); i++) {
                final DiffEntry diff = e.getDiffs().get(i);
                final DiffEntry.Context expContext = diff.getExpectedContext();
                final DiffEntry.Context actContext = diff.getActualContext();
                final String expCol = String.valueOf(expContext.getColumn());
                final String actCol = String.valueOf(actContext.getColumn());
                final Object expVal = expContext.getColumn() == null ? expContext.getLine().getRawLine() == null ? "--" : "<line>" : expContext.getValue();
                final Object actVal = actContext.getColumn() == null ? actContext.getLine().getRawLine() == null ? "--" : "<line>" : actContext.getValue();
                final String columns = expCol.equals(actCol) ? expCol : expCol + "/" + actCol;
                LOGGER.error("[ASSSERTION FAILED]  ({}) | {} | {} | {} | {}", fixedLen(i + 1, numlen), fixedLen(columns, 20), fixedLen(expVal, 20), fixedLen(actVal, 20), diff.getMessage());
            }
            LOGGER.error("[ASSSERTION FAILED] {}----|-------------------------------------------------------------------------------------", spaces(numlen));
//            LOG.error("[ASSSERTION FAILED] " + spaces(numlen) + "----|=====================================================================================");
            for (int i = 0; i < e.getDiffs().size(); i++) {
                final DiffEntry diff = e.getDiffs().get(i);
                final CsvLine expLine = diff.getExpectedContext().getLine();
                final CsvLine actLine = diff.getActualContext().getLine();
                LOGGER.error("[ASSSERTION FAILED]  ({}) | exp-line: [{}] {}", fixedLen(i+1, numlen), expLine.getLineIndex(), expLine.getRawLine());
                LOGGER.error("[ASSSERTION FAILED]   {}  | act-line: [{}] {}", spaces(numlen), actLine.getLineIndex(), actLine.getRawLine());
            }
            LOGGER.error("[ASSSERTION FAILED] {}=========================================================================================", spaces(numlen));
            throw e;
        } catch (IOException e) {
            LOGGER.error("[ASSSERTION FAILED] I/O exception");
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
