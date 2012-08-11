package com.algoTrader.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.jmx.export.annotation.ManagedResource;

import com.algoTrader.entity.strategy.PortfolioValue;
import com.algoTrader.util.StrategyUtil;
import com.algoTrader.vo.IndicatorVO;

@ManagedResource(objectName = "com.algoTrader.service:name=PortfolioChart,type=chart")
public class PortfolioChartServiceImpl extends PortfolioChartServiceBase {

    @Override
    protected Set<IndicatorVO> handleGetIndicators(long startDateTime) throws Exception {

        Set<IndicatorVO> set = new HashSet<IndicatorVO>();

        // only push values if chart is being reset
        if (startDateTime == 0) {

            String strategyName = StrategyUtil.getStartedStrategyName();
            double lastNetLiqValue = 0;
            double performance = 1.0;
            for (PortfolioValue portfolioValue : getLookupService().getPortfolioValuesByStrategyAndMinDate(strategyName, new Date(0))) {

                // calculate the performance
                if (lastNetLiqValue != 0) {
                    double adjustedNetLiqValue = portfolioValue.getNetLiqValueDouble();
                    if (portfolioValue.getCashFlow() != null) {
                        adjustedNetLiqValue -= portfolioValue.getCashFlow().doubleValue();
                    }
                    performance = performance * (adjustedNetLiqValue / lastNetLiqValue);
                }
                lastNetLiqValue = portfolioValue.getNetLiqValueDouble();

                set.add(new IndicatorVO("netLiqValue", portfolioValue.getDateTime(), portfolioValue.getNetLiqValueDouble()));
                set.add(new IndicatorVO("securitiesCurrentValue", portfolioValue.getDateTime(), portfolioValue.getSecuritiesCurrentValueDouble()));
                set.add(new IndicatorVO("cashBalance", portfolioValue.getDateTime(), portfolioValue.getCashBalanceDouble()));
                set.add(new IndicatorVO("maintenanceMargin", portfolioValue.getDateTime(), portfolioValue.getMaintenanceMarginDouble()));
                set.add(new IndicatorVO("performance", portfolioValue.getDateTime(), performance));
                set.add(new IndicatorVO("leverage", portfolioValue.getDateTime(), portfolioValue.getLeverage()));
                set.add(new IndicatorVO("allocation", portfolioValue.getDateTime(), portfolioValue.getAllocation()));
            }
        }

        return set;
    }
}
