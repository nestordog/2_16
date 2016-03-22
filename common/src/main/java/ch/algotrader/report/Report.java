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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Locale;

import ch.algotrader.config.ConfigLocator;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimePatterns;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public abstract class Report implements Closeable {

    private final static DateTimeFormatter REPORT_DATE_TIME;
    static {
        REPORT_DATE_TIME = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                .appendLiteral('.')
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .appendLiteral('.')
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .appendLiteral(' ')
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .toFormatter(Locale.ROOT);
    }

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
        return REPORT_DATE_TIME.format(DateTimeLegacy.toLocalDateTime(date));
    }

}
