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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import ch.algotrader.entity.strategy.PortfolioValueI;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * @author <a href="mailto:mterzer@algotrader.ch">Marco Terzer</a>
 */
public class PortfolioReport extends ListReporter {

    private final BigDecimal initialBalance;
    private volatile boolean initialized = false;

    public static PortfolioReport create(final BigDecimal initialBalance) throws IOException {
        return new PortfolioReport(
                Report.generateFile("PortfolioReport"),
                new String[] { "dateTime", "cashBalance", "securitiesCurrentValue", "netLiqValue" },
                initialBalance);
    }

    protected PortfolioReport(final File file, String[] header, final BigDecimal initialBalance) throws IOException {
        super(file, header);
        this.initialBalance = initialBalance;
    }

    public void write(final Date currentDateTime, final PortfolioValueI portfolioValue)  throws IOException {

        long startTime = System.nanoTime();

        // don't log anything until any trades took place
        if (!portfolioValue.getNetLiqValue().equals(this.initialBalance)) {
            this.initialized = true;
        }

        if (this.initialized) {
            writeAndFlush(formatDateTime(currentDateTime), //
                    portfolioValue.getCashBalance(), //
                    portfolioValue.getSecuritiesCurrentValue(), //
                    portfolioValue.getNetLiqValue());
        }

        MetricsUtil.accountEnd("LogPortfolioValueSubscriber", startTime);

    }
}
