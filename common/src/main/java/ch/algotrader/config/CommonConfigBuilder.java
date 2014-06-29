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

import java.io.File;
import java.math.BigDecimal;

import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.MarketDataType;

/**
 * Factory for Algotrader standard platform configuration objects.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public final class CommonConfigBuilder {

    private String strategyName;
    private String dataSet;
    private MarketDataType dataSetType;
    private File dataSetLocation;
    private Duration barSize;
    private boolean feedGenericEvents;
    private boolean feedAllMarketDataFiles;
    private boolean simulation;
    private BigDecimal simulationInitialBalance;
    private boolean simulationLogTransactions;
    private boolean singleVM;
    private int portfolioDigits;
    private Currency portfolioBaseCurrency;
    private BigDecimal initialMarginMarkup;
    private boolean validateCrossedSpread;
    private boolean displayClosedPositions;

    CommonConfigBuilder() {
        this.strategyName = "BASE";
        this.dataSet = "current";
        this.dataSetType = MarketDataType.TICK;
        this.barSize = Duration.MIN_1;
        this.simulation = false;
        this.simulationInitialBalance = new BigDecimal(1000000L);
        this.singleVM = false;
        this.portfolioDigits = 2;
        this.portfolioBaseCurrency = Currency.USD;
        this.initialMarginMarkup = new BigDecimal("1.25");
    }

    public static CommonConfigBuilder create() {
        return new CommonConfigBuilder();
    }

    public CommonConfigBuilder setStrategyName(final String strategyName) {
        this.strategyName = strategyName;
        return this;
    }

    public CommonConfigBuilder setDataSet(final String dataSet) {
        this.dataSet = dataSet;
        return this;
    }

    public CommonConfigBuilder setDataSetType(final MarketDataType dataSetType) {
        this.dataSetType = dataSetType;
        return this;
    }

    public CommonConfigBuilder setDataSetLocation(final File dataSetLocation) {
        this.dataSetLocation = dataSetLocation;
        return this;
    }

    public CommonConfigBuilder setBarSize(final Duration barSize) {
        this.barSize = barSize;
        return this;
    }

    public CommonConfigBuilder setFeedGenericEvents(boolean feedGenericEvents) {
        this.feedGenericEvents = feedGenericEvents;
        return this;
    }

    public CommonConfigBuilder setFeedAllMarketDataFiles(boolean feedAllMarketDataFiles) {
        this.feedAllMarketDataFiles = feedAllMarketDataFiles;
        return this;
    }

    public CommonConfigBuilder setSimulation(final boolean simulation) {
        this.simulation = simulation;
        return this;
    }

    public CommonConfigBuilder setSimulationInitialBalance(final BigDecimal simulationInitialBalance) {
        this.simulationInitialBalance = simulationInitialBalance;
        return this;
    }

    public CommonConfigBuilder setSimulationLogTransactions(boolean simulationLogTransactions) {
        this.simulationLogTransactions = simulationLogTransactions;
        return this;
    }

    public CommonConfigBuilder setSingleVM(boolean singleVM) {
        this.singleVM = singleVM;
        return this;
    }

    public CommonConfigBuilder setPortfolioDigits(final int portfolioDigits) {
        this.portfolioDigits = portfolioDigits;
        return this;
    }

    public CommonConfigBuilder setPortfolioBaseCurrency(final Currency portfolioBaseCurrency) {
        this.portfolioBaseCurrency = portfolioBaseCurrency;
        return this;
    }

    public CommonConfigBuilder setInitialMarginMarkup(BigDecimal initialMarginMarkup) {
        this.initialMarginMarkup = initialMarginMarkup;
        return this;
    }

    public CommonConfigBuilder setValidateCrossedSpread(boolean validateCrossedSpread) {
        this.validateCrossedSpread = validateCrossedSpread;
        return this;
    }

    public CommonConfigBuilder setDisplayClosedPositions(boolean displayClosedPositions) {
        this.displayClosedPositions = displayClosedPositions;
        return this;
    }

    public CommonConfig build() {
        return new CommonConfig(
                strategyName, dataSet, dataSetType, dataSetLocation, barSize, feedGenericEvents, feedAllMarketDataFiles,
                simulation, simulationInitialBalance, simulationLogTransactions, singleVM,
                portfolioDigits, portfolioBaseCurrency, initialMarginMarkup, validateCrossedSpread, displayClosedPositions);
    }

}
