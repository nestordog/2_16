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
package ch.algotrader.esper.subscriber;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.report.ListReporter;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * Prints portfolio values CashBalance and SecuritiesCurrentValue to files/report/PortfolioReport.csv
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PortfolioValueSubscriber {

    private boolean initialized = false;
    private ListReporter reporter;

    public void update(PortfolioValue portfolioValue) {

        long startTime = System.nanoTime();

        if (this.reporter == null) {
            this.reporter = new ListReporter("PortfolioReport", new String[] { "cashBalance", "securitiesCurrentValue" });
        }

        // don't log anything until any trades took place
        CommonConfig commonConfig = ConfigLocator.instance().getCommonConfig();
        if (!portfolioValue.getNetLiqValue().equals(commonConfig.getSimulationInitialBalance())) {
            this.initialized = true;
        }

        if (this.initialized) {
            this.reporter.write(portfolioValue.getCashBalance(), portfolioValue.getSecuritiesCurrentValue());
        }

        MetricsUtil.accountEnd("LogPortfolioValueSubscriber", startTime);
    }
}
