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

import java.io.Closeable;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;

import ch.algotrader.config.ConfigLocator;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimePatterns;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public abstract class Report implements Closeable {

    public static File generateFile(final String fileName) {

        ConfigParams configParams = ConfigLocator.instance().getConfigParams();
        File reportDir = new File(configParams.getString("report.reportLocation", "files/report"));
        boolean useTimestamp = configParams.getBoolean("report.filenameTimestamp", false);

        StringBuilder buf = new StringBuilder();
        buf.append(fileName);
        if (useTimestamp) {
            buf.append('-');
            DateTimePatterns.FILE_TIMESTAMP.formatTo(LocalDateTime.now(), buf);
        }
        buf.append(".csv");
        return new File(reportDir, buf.toString());
    }

    protected String formatDateTime(final Date date) {
        return DateTimePatterns.LOCAL_DATE_TIME.format(DateTimeLegacy.toLocalDateTime(date));
    }

}
