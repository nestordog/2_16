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
package ch.algotrader.service;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.vo.ChartDefinitionVO;
import ch.algotrader.vo.IndicatorVO;
import ch.algotrader.vo.PortfolioValueVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@ManagedResource(objectName = "ch.algotrader.service:name=PortfolioChart,type=chart")
public class PortfolioChartServiceImpl extends ChartProvidingServiceImpl implements PortfolioChartService {

    private final CommonConfig commonConfig;

    private final PortfolioService portfolioService;

    public PortfolioChartServiceImpl(final ChartDefinitionVO diagramDefinition,
            final CommonConfig commonConfig,
            final PortfolioService portfolioService) {

        super(diagramDefinition);

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(portfolioService, "PortfolioService is null");

        this.commonConfig = commonConfig;
        this.portfolioService = portfolioService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Returns the {@link IndicatorVO Indicator Data}")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "startDateTime", description = "startDateTime") })
    public Collection<IndicatorVO> getIndicators(final long startDateTime) {

        try {
            Set<IndicatorVO> set = new HashSet<IndicatorVO>();

            // only push values if chart is being reset
            if (startDateTime == 0) {

                String strategyName = this.commonConfig.getStrategyName();
                for (PortfolioValueVO portfolioValue : this.portfolioService.getPortfolioValuesInclPerformanceSinceDate(strategyName, new Date(startDateTime))) {

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
        } catch (Exception ex) {
            throw new ChartProvidingServiceException(ex.getMessage(), ex);
        }
    }
}
