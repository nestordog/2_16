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
package ch.algotrader.config;

import java.math.BigDecimal;

import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.MarketDataType;

/**
 * Algotrader common configuration object.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public final class CommonConfig {

    private final String strategyName;
    private final String dataSet;
    private final MarketDataType dataSetType;
    private final Duration barSize;
    private final boolean simulation;
    private final BigDecimal simulationInitialBalance;
    private final boolean singleVM;
    private final int portfolioDigits;
    private final Currency portfolioBaseCurrency;


    public CommonConfig(
            @ConfigName("strategyName") final String strategyName,
            @ConfigName("dataSource.dataSet") final String dataSet,
            @ConfigName("dataSource.dataSetType") final MarketDataType dataSetType,
            @ConfigName("dataSource.barSize") final Duration barSize,
            @ConfigName("simulation") final boolean simulation,
            @ConfigName("simulation.initialBalance") final BigDecimal simulationInitialBalance,
            @ConfigName("misc.singleVM") final boolean singleVM,
            @ConfigName("misc.portfolioDigits") final int portfolioDigits,
            @ConfigName("misc.portfolioBaseCurrency") final Currency portfolioBaseCurrency) {
        this.strategyName = strategyName;
        this.dataSet = dataSet;
        this.dataSetType = dataSetType;
        this.barSize = barSize;
        this.simulation = simulation;
        this.simulationInitialBalance = simulationInitialBalance;
        this.singleVM = singleVM;
        this.portfolioDigits = portfolioDigits;
        this.portfolioBaseCurrency = portfolioBaseCurrency;
    }

    public String getStrategyName() {
        return this.strategyName;
    }

    public String getDataSet() {
        return this.dataSet;
    }

    public MarketDataType getDataSetType() {
        return this.dataSetType;
    }

    public Duration getBarSize() {
        return this.barSize;
    }

    public boolean isSimulation() {
        return this.simulation;
    }

    public BigDecimal getSimulationInitialBalance() {
        return this.simulationInitialBalance;
    }

    public boolean isSingleVM() {
        return this.singleVM;
    }

    public int getPortfolioDigits() {
        return this.portfolioDigits;
    }

    public Currency getPortfolioBaseCurrency() {
        return this.portfolioBaseCurrency;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[");
        sb.append("strategyName='").append(this.strategyName).append('\'');
        sb.append(", dataSet='").append(this.dataSet).append('\'');
        sb.append(", dataSetType=").append(this.dataSetType);
        sb.append(", barSize=").append(this.barSize);
        sb.append(", simulation=").append(this.simulation);
        sb.append(", simulationInitialBalance=").append(this.simulationInitialBalance);
        sb.append(", singleVM=").append(this.singleVM);
        sb.append(", portfolioDigits=").append(this.portfolioDigits);
        sb.append(", portfolioBaseCurrency=").append(this.portfolioBaseCurrency);
        sb.append(']');
        return sb.toString();
    }

}
