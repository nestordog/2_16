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
package ch.algotrader.report;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.algotrader.entity.strategy.PortfolioValueI;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * @author <a href="mailto:mterzer@algotrader.ch">Marco Terzer</a>
 *
 * @version $Revision$ $Date$
 */
public class PortfolioReport extends ListReporter {

    protected static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private final BigDecimal initialBalance;
    private boolean initialized = false;

    public PortfolioReport(final BigDecimal initialBalance) {

        super("PortfolioReport", new String[] { "dateTime", "cashBalance", "securitiesCurrentValue", "netLiqValue" });
        this.initialBalance = initialBalance;
    }

    public void write(final Date currentDateTime, final PortfolioValueI portfolioValue) {

        long startTime = System.nanoTime();

        // don't log anything until any trades took place
        if (!portfolioValue.getNetLiqValue().equals(this.initialBalance)) {
            this.initialized = true;
        }

        if (this.initialized) {
            write(DATE_TIME_FORMAT.format(currentDateTime), //
                    portfolioValue.getCashBalance(), //
                    portfolioValue.getSecuritiesCurrentValue(), //
                    portfolioValue.getNetLiqValue());
        }

        MetricsUtil.accountEnd("LogPortfolioValueSubscriber", startTime);

    }
}
