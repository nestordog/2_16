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
 */
public final class CommonConfigBuilder {

    private String dataSet;
    private MarketDataType dataSetType;
    private File dataSetLocation;
    private Duration barSize;
    private final boolean feedCSV;
    private final boolean feedDB;
    private boolean feedGenericEvents;
    private boolean feedAllMarketDataFiles;
    private final int feedBatchSize;
    private File reportLocation;
    private boolean disableReports;
    private boolean simulation;
    private BigDecimal simulationInitialBalance;
    private boolean embedded;
    private Currency portfolioBaseCurrency;
    private int portfolioDigits;
    private String defaultAccountName;
    private boolean validateCrossedSpread;
    private boolean displayClosedPositions;

    CommonConfigBuilder() {
        this.dataSet = "current";
        this.dataSetType = MarketDataType.TICK;
        this.barSize = Duration.MIN_1;
        this.feedCSV = true;
        this.feedDB = false;
        this.feedGenericEvents = false;
        this.feedAllMarketDataFiles = false;
        this.feedBatchSize = 20;
        this.simulation = false;
        this.simulationInitialBalance = new BigDecimal(1000000L);
        this.embedded = false;
        this.portfolioBaseCurrency = Currency.USD;
        this.portfolioDigits = 2;
        this.defaultAccountName = "IB_NATIVE_TEST";
    }

    public static CommonConfigBuilder create() {
        return new CommonConfigBuilder();
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

    public CommonConfigBuilder setReportLocation(File reportLocation) {
        this.reportLocation = reportLocation;
        return this;
    }

    public CommonConfigBuilder setDisableReports(boolean disableReports) {
        this.disableReports = disableReports;
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

    public CommonConfigBuilder setEmbedded(boolean embedded) {
        this.embedded = embedded;
        return this;
    }

    public CommonConfigBuilder setPortfolioBaseCurrency(final Currency portfolioBaseCurrency) {
        this.portfolioBaseCurrency = portfolioBaseCurrency;
        return this;
    }

    public CommonConfigBuilder setPortfolioDigits(final int portfolioDigits) {
        this.portfolioDigits = portfolioDigits;
        return this;
    }

    public CommonConfigBuilder setDefaultAccountName(final String defaultAccountName) {
        this.defaultAccountName = defaultAccountName;
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
                this.dataSet, this.dataSetType, this.dataSetLocation, this.barSize, this.feedCSV, this.feedDB, this.feedGenericEvents, this.feedAllMarketDataFiles,
                this.feedBatchSize, this.reportLocation, this.disableReports, this.simulation, this.simulationInitialBalance, this.embedded,
                this.portfolioBaseCurrency, this.portfolioDigits, this.defaultAccountName, this.validateCrossedSpread, this.displayClosedPositions);
    }

}
