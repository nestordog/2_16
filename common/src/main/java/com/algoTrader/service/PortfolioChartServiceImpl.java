/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.jmx.export.annotation.ManagedResource;

import com.algoTrader.vo.IndicatorVO;
import com.algoTrader.vo.PortfolioValueVO;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@ManagedResource(objectName = "com.algoTrader.service:name=PortfolioChart,type=chart")
public class PortfolioChartServiceImpl extends PortfolioChartServiceBase {

    @Override
    protected Set<IndicatorVO> handleGetIndicators(long startDateTime) throws Exception {

        Set<IndicatorVO> set = new HashSet<IndicatorVO>();

        // only push values if chart is being reset
        if (startDateTime == 0) {

            String strategyName = getConfiguration().getStartedStrategyName();
            for (PortfolioValueVO portfolioValue : getPortfolioService().getPortfolioValuesInclPerformanceSinceDate(strategyName, new Date(startDateTime))) {

                set.add(new IndicatorVO("netLiqValue", portfolioValue.getDateTime(), portfolioValue.getNetLiqValue().doubleValue()));
                set.add(new IndicatorVO("securitiesCurrentValue", portfolioValue.getDateTime(), portfolioValue.getSecuritiesCurrentValue().doubleValue()));
                set.add(new IndicatorVO("cashBalance", portfolioValue.getDateTime(), portfolioValue.getCashBalance().doubleValue()));
                set.add(new IndicatorVO("maintenanceMargin", portfolioValue.getDateTime(), portfolioValue.getMaintenanceMargin().doubleValue()));
                set.add(new IndicatorVO("performance", portfolioValue.getDateTime(), portfolioValue.getPerformance()));
                set.add(new IndicatorVO("leverage", portfolioValue.getDateTime(), portfolioValue.getLeverage()));
                set.add(new IndicatorVO("allocation", portfolioValue.getDateTime(), portfolioValue.getAllocation()));
            }
        }

        return set;
    }
}
