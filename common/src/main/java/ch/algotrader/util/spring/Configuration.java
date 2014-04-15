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
package ch.algotrader.util.spring;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.entity.strategy.StrategyImpl;

import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.MarketDataType;

/**
 * Spring Bean containing all properties defined in {@code conf.propertiese} files and via VM-argument.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
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

    public String getStartedStrategyName() {

        if (this.simulation) {
            return StrategyImpl.BASE;
        } else {
            return this.strategyName;
        }
    }

    public boolean isStartedStrategyBASE() {

        return StrategyImpl.BASE.equals(getStartedStrategyName());
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
