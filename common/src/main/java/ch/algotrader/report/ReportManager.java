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

package ch.algotrader.report;

import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class ReportManager {

    private static final Logger LOGGER = LogManager.getLogger(ReportManager.class);

    private static final Deque<Report> REPORTS = new ConcurrentLinkedDeque<>();

    public static void registerReport(Report report) {
        REPORTS.add(report);
    }

    public static void closeAll() {

        while (!REPORTS.isEmpty()) {
            Report report = REPORTS.remove();
            try {
                report.close();
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }
}
