package com.algoTrader.util.spring;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.MarketChannel;
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

    private @Value("${simulation.eventsPerDay}") int eventsPerDay;
    public int getEventsPerDay() {
        return this.eventsPerDay;
    }

    private @Value("${simulation.logTransactions}") boolean logTransactions;
    public boolean getLogTransactions() {
        return this.logTransactions;
    }

    private @Value("${simulation.roundDigits}") int roundDigits;
    public int getRoundDigits() {
        return this.roundDigits;
    }

    private @Value("${portfolioBaseCurrency}") String portfolioBaseCurrency;
    public Currency getPortfolioBaseCurrency() {
        return Currency.fromString(this.portfolioBaseCurrency);
    }

    private @Value("${portfolioDigits}") int portfolioDigits;
    public int getPortfolioDigits() {
        return this.portfolioDigits;
    }

    private @Value("${sabrEnabled}") boolean sabrEnabled;
    public boolean getSabrEnabled() {
        return this.sabrEnabled;
    }

    private @Value("${sabrBeta}") double sabrBeta;
    public double getSabrBeta() {
        return this.sabrBeta;
    }

    private @Value("${saveToFile}") boolean saveToFile;
    public boolean getSaveToFile() {
        return this.saveToFile;
    }

    private @Value("${marketIntrest}") double marketIntrest;
    public double getMarketIntrest() {
        return this.marketIntrest;
    }

    private @Value("${fxEqualizationMinAmount}") BigDecimal fxEqualizationMinAmount;
    public BigDecimal getFxEqualizationMinAmount() {
        return this.fxEqualizationMinAmount;
    }

    private @Value("${fxEqualizationBatchSize}") long fxEqualizationBatchSize;

    public long getFxEqualizationBatchSize() {
        return this.fxEqualizationBatchSize;
    }

    private @Value("${maxObjectInStream}") long maxObjectInStream;
    public long getMaxObjectInStream() {
        return this.maxObjectInStream;
    }

    private @Value("${marketChannel}") String marketChannel;
    public MarketChannel getMarketChannel() {
        return MarketChannel.fromString(this.marketChannel);
    }

    private @Value("${initialMarginMarkup}") double initialMarginMarkup;
    public double getInitialMarginMarkup() {
        return this.initialMarginMarkup;
    }

    private @Value("${minSpreadPosition}") double minSpreadPosition;
    public double getMinSpreadPosition() {
        return this.minSpreadPosition;
    }

    private @Value("${maxSpreadPosition}") double maxSpreadPosition;
    public double getMaxSpreadPosition() {
        return this.maxSpreadPosition;
    }

    private @Value("${spreadPositionIncrement}") double spreadPositionIncrement;
    public double getSpreadPositionIncrement() {
        return this.spreadPositionIncrement;
    }

    // generic methods

    public String getString(String key) {
        return this.properties.getProperty(key);
    }

    public String getString(String strategyName, String key) {
        String value;
        if (StrategyImpl.BASE.equals(strategyName)) {
            value = this.properties.getProperty(key);
        } else {
            value = this.properties.getProperty(strategyName.toLowerCase() + "." + key);
            if (value == null) {
                value = this.properties.getProperty(key);
            }
        }
        return value;
    }

    public long getLong(String key) {
        String value = this.properties.getProperty(key);
        if (value != null) {
            return Long.valueOf(value);
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    public long getLong(String strategyName, String key) {
        String value;
        if (StrategyImpl.BASE.equals(strategyName)) {
            value = this.properties.getProperty(key);
        } else {
            value = this.properties.getProperty(strategyName.toLowerCase() + "." + key);
            if (value == null) {
                value = this.properties.getProperty(key);
            }
        }

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

    public int getInt(String strategyName, String key) {
        String value;
        if (StrategyImpl.BASE.equals(strategyName)) {
            value = this.properties.getProperty(key);
        } else {
            value = this.properties.getProperty(strategyName.toLowerCase() + "." + key);
            if (value == null) {
                value = this.properties.getProperty(key);
            }
        }

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

    public double getDouble(String strategyName, String key) {
        String value;
        if (StrategyImpl.BASE.equals(strategyName)) {
            value = this.properties.getProperty(key);
        } else {
            value = this.properties.getProperty(strategyName.toLowerCase() + "." + key);
            if (value == null) {
                value = this.properties.getProperty(key);
            }
        }

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

    public BigDecimal getBigDecimal(String strategyName, String key) {
        String value;
        if (StrategyImpl.BASE.equals(strategyName)) {
            value = this.properties.getProperty(key);
        } else {
            value = this.properties.getProperty(strategyName.toLowerCase() + "." + key);
            if (value == null) {
                value = this.properties.getProperty(key);
            }
        }

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

    public Boolean getBoolean(String strategyName, String key) {
        String value;
        if (StrategyImpl.BASE.equals(strategyName)) {
            value = this.properties.getProperty(key);
        } else {
            value = this.properties.getProperty(strategyName.toLowerCase() + "." + key);
            if (value == null) {
                value = this.properties.getProperty(key);
            }
        }

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

    /**
     * will not update EL referenced properties (only properties is updated)
     */
    public void setProperty(String strategyName, String key, String value) {
        this.properties.setProperty(strategyName.toLowerCase() + "." + key, value);
    }
}
