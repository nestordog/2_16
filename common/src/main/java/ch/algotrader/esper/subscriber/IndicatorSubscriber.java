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
package ch.algotrader.esper.subscriber;

import java.util.Date;
import java.util.Map;

import ch.algotrader.report.ListReporter;
import ch.algotrader.report.Report;
import ch.algotrader.util.DateTimeUtil;

/**
 * Prints all values as a comma-separated-list (CSV) to files/reports/IndicatorReport.csv
 *
 * Headers will be extracted from the first arriving event {@code statement}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class IndicatorSubscriber {

    private ListReporter reporter;

    public void update(Map<?, ?> map) {

        if (this.reporter == null) {
            this.reporter = new ListReporter(Report.generateFile("IndicatorReport"), map.keySet().toArray(new String[] {}));
        }

        Object[] values = new Object[map.entrySet().size()];
        int i = 0;
        for (Object obj : map.values()) {
            if (obj instanceof Date) {
                values[i++] = DateTimeUtil.formatAsGMT(((Date) obj).toInstant());
            } else {
                values[i++] = obj;
            }
        }

        this.reporter.write(values);
    }
}
