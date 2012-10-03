package com.algoTrader.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.jmx.export.annotation.ManagedResource;

import com.algoTrader.util.StrategyUtil;
import com.algoTrader.vo.IndicatorVO;
import com.algoTrader.vo.PortfolioValueVO;

@ManagedResource(objectName = "com.algoTrader.service:name=PortfolioChart,type=chart")
public class PortfolioChartServiceImpl extends PortfolioChartServiceBase {

    @Override
    protected Set<IndicatorVO> handleGetIndicators(long startDateTime) throws Exception {

        Set<IndicatorVO> set = new HashSet<IndicatorVO>();

        String strategyName = StrategyUtil.getStartedStrategyName();
        for (PortfolioValueVO portfolioValue : getPortfolioService().getPortfolioValuesSinceDate(strategyName, new Date(startDateTime))) {

            set.add(new IndicatorVO("netLiqValue", portfolioValue.getDateTime(), portfolioValue.getNetLiqValue().doubleValue()));
            set.add(new IndicatorVO("securitiesCurrentValue", portfolioValue.getDateTime(), portfolioValue.getSecuritiesCurrentValue().doubleValue()));
            set.add(new IndicatorVO("cashBalance", portfolioValue.getDateTime(), portfolioValue.getCashBalance().doubleValue()));
            set.add(new IndicatorVO("maintenanceMargin", portfolioValue.getDateTime(), portfolioValue.getMaintenanceMargin().doubleValue()));
            set.add(new IndicatorVO("performance", portfolioValue.getDateTime(), portfolioValue.getPerformance()));
            set.add(new IndicatorVO("leverage", portfolioValue.getDateTime(), portfolioValue.getLeverage()));
            set.add(new IndicatorVO("allocation", portfolioValue.getDateTime(), portfolioValue.getAllocation()));
        }

        return set;
    }
}
