package com.algoTrader.util.spring;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.Duration;
import com.algoTrader.enumeration.MarketDataType;

public class Configuration {

    private @Autowired() Properties properties;

    public Properties getProperties() {
        return this.properties;
    }

    // specific methods

    private @Value("${strategyName}") String strategyName;
    public String getStrategyName() {
        return this.strategyName;
    }

    private @Value("${dataSource.dataSet}") String dataSet;
    public String getDataSet() {
        return this.dataSet;
    }

    private @Value("${dataSource.dataSetType}") String dataSetType;
    public MarketDataType getDataSetType() {
        return MarketDataType.fromString(this.dataSetType);
    }

    private @Value("${dataSource.barSize}") String barSize;
    public Duration getBarSize() {
        return Duration.fromString(this.barSize);
    }

    private @Value("${simulation}") boolean simulation;
    public boolean getSimulation() {
        return this.simulation;
    }

    private @Value("${simulation.start}") long simulationStart;
    public long getSimulationStart() {
        return this.simulationStart;
    }

    private @Value("${simulation.end}") long simulationEnd;
    public long getSimulationEnd() {
        return this.simulationEnd;
    }

    private @Value("${simulation.initialBalance}") long initialBalance;
    public long getInitialBalance() {
        return this.initialBalance;
    }

    private @Value("${simulation.logTransactions}") boolean logTransactions;
    public boolean getLogTransactions() {
        return this.logTransactions;
    }

    private @Value("${simulation.roundDigits}") int roundDigits;
    public int getRoundDigits() {
        return this.roundDigits;
    }

    private @Value("${misc.portfolioBaseCurrency}") String portfolioBaseCurrency;
    public Currency getPortfolioBaseCurrency() {
        return Currency.fromString(this.portfolioBaseCurrency);
    }

    private @Value("${misc.portfolioDigits}") int portfolioDigits;
    public int getPortfolioDigits() {
        return this.portfolioDigits;
    }

    private @Value("${misc.sabrEnabled}") boolean sabrEnabled;
    public boolean getSabrEnabled() {
        return this.sabrEnabled;
    }

    private @Value("${misc.sabrBeta}") double sabrBeta;
    public double getSabrBeta() {
        return this.sabrBeta;
    }

    private @Value("${misc.marketIntrest}") double marketIntrest;
    public double getMarketIntrest() {
        return this.marketIntrest;
    }

    private @Value("${order.initialMarginMarkup}") double initialMarginMarkup;
    public double getInitialMarginMarkup() {
        return this.initialMarginMarkup;
    }

    // generic methods

    public String getString(String key) {
        return this.properties.getProperty(key);
    }

    public long getLong(String key) {
        String value = this.properties.getProperty(key);
        if (value != null) {
            return Long.valueOf(value);
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    public int getInt(String key) {
        String value = this.properties.getProperty(key);
        if (value != null) {
            return Integer.valueOf(value);
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    public double getDouble(String key) {
        String value = this.properties.getProperty(key);
        if (value != null) {
            return Double.valueOf(value);
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    public BigDecimal getBigDecimal(String key) {
        String value = this.properties.getProperty(key);
        if (value != null) {
            return new BigDecimal(value);
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    public boolean getBoolean(String key) {
        String value = this.properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * will not update EL referenced properties (only properties is updated)
     */
    public void setProperty(String key, String value) {
        this.properties.setProperty(key, value);
    }
}
