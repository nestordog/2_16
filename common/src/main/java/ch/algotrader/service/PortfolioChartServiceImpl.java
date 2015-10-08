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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.vo.PortfolioValueVO;
import ch.algotrader.vo.client.ChartDefinitionVO;
import ch.algotrader.vo.client.IndicatorVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@ManagedResource(objectName = "ch.algotrader.service:name=PortfolioChart,type=chart")
public class PortfolioChartServiceImpl extends ChartProvidingServiceImpl implements PortfolioChartService, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LogManager.getLogger(PortfolioChartServiceImpl.class);

    private final EngineManager engineManager;
    private final PortfolioService portfolioService;
    private volatile String strategyName;

    public PortfolioChartServiceImpl(final ChartDefinitionVO diagramDefinition,
            final EngineManager engineManager,
            final PortfolioService portfolioService) {

        super(diagramDefinition);

        Validate.notNull(engineManager, "EngineManager name is null");
        Validate.notNull(portfolioService, "PortfolioService is null");

        this.engineManager = engineManager;
        this.portfolioService = portfolioService;
    }

    private Engine getMainEngine() {
        Engine engine;
        Collection<Engine> strategyEngines = this.engineManager.getStrategyEngines();
        if (strategyEngines.isEmpty()) {
            engine = engineManager.getServerEngine();
        } else {
            Iterator<Engine> it = strategyEngines.iterator();
            engine = it.next();
            if (it.hasNext()) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Management services do not support multiple strategies. Using strategy {}", engine.getStrategyName());
                }
            }
        }
        return engine;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {
        Engine engine = getMainEngine();
        this.strategyName = engine.getStrategyName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Returns the {@link IndicatorVO Indicator Data}")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "startDateTime", description = "startDateTime") })
    public Collection<IndicatorVO> getIndicators(final long startDateTime) {

        List<IndicatorVO> vos = new ArrayList<>();

        // only push values if chart is being reset
        if (startDateTime == 0) {

            for (PortfolioValueVO portfolioValue : this.portfolioService.getPortfolioValuesInclPerformanceSinceDate(this.strategyName, new Date(startDateTime))) {

                vos.add(new IndicatorVO("netLiqValue", portfolioValue.getDateTime(), portfolioValue.getNetLiqValue().doubleValue()));
                vos.add(new IndicatorVO("securitiesCurrentValue", portfolioValue.getDateTime(), portfolioValue.getSecuritiesCurrentValue().doubleValue()));
                vos.add(new IndicatorVO("cashBalance", portfolioValue.getDateTime(), portfolioValue.getCashBalance().doubleValue()));
                vos.add(new IndicatorVO("performance", portfolioValue.getDateTime(), portfolioValue.getPerformance()));
                vos.add(new IndicatorVO("leverage", portfolioValue.getDateTime(), portfolioValue.getLeverage()));
                vos.add(new IndicatorVO("allocation", portfolioValue.getDateTime(), portfolioValue.getAllocation()));
            }
        }

        return vos;

    }
}
