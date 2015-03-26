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
import java.net.URI;
import java.net.URL;

/**
 * Typed configuration parameters.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public final class ConfigParams {

    private final ConfigProvider configProvider;

    public ConfigParams(final ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public <T> T getParameter(final String name, final Class<T> clazz) {
        return this.configProvider.getParameter(name, clazz);
    }

    public <T> T getParameter(final String name, final Class<T> clazz, final T defaultValue) {
        T value = this.configProvider.getParameter(name, clazz);
        return value != null ? value : defaultValue;
    }

    public String getString(final String name) {
        return this.configProvider.getParameter(name, String.class);
    }

    public String getString(final String name, final String defaultValue) {
        String value = this.configProvider.getParameter(name, String.class);
        return value != null ? value : defaultValue;
    }

    public Integer getInteger(String name) {
        return this.configProvider.getParameter(name, Integer.class);
    }

    public int getInteger(final String name, final int defaultValue) {
        Integer value = this.configProvider.getParameter(name, Integer.class);
        return value != null ? value : defaultValue;
    }

    public Long getLong(String name) {
        return this.configProvider.getParameter(name, Long.class);
    }

    public long getLong(final String name, final long defaultValue) {
        Long value = this.configProvider.getParameter(name, Long.class);
        return value != null ? value : defaultValue;
    }

    public Boolean getBoolean(final String name) {
        return this.configProvider.getParameter(name, Boolean.class);
    }

    public Boolean getBoolean(final String name, final boolean defaultValue) {
        Boolean value = this.configProvider.getParameter(name, Boolean.class);
        return value != null ? value : defaultValue;
    }

    public Double getDouble(final String name) {
        return this.configProvider.getParameter(name, Double.class);
    }

    public double getDouble(final String name, final double defaultValue) {
        Double value = this.configProvider.getParameter(name, Double.class);
        return value != null ? value : defaultValue;
    }

    public BigDecimal getBigDecimal(final String name) {
        return this.configProvider.getParameter(name, BigDecimal.class);
    }

    public BigDecimal getBigDecimal(final String name, final BigDecimal defaultValue) {
        BigDecimal value = this.configProvider.getParameter(name, BigDecimal.class);
        return value != null ? value : defaultValue;
    }

    public URL getURL(final String name) {
        return this.configProvider.getParameter(name, URL.class);
    }

    public URL getURL(final String name, final URL defaultValue) {
        URL value = this.configProvider.getParameter(name, URL.class);
        return value != null ? value : defaultValue;
    }

    public URI getURI(final String name) {
        return this.configProvider.getParameter(name, URI.class);
    }

    public URI getURI(final String name, final URI defaultValue) {
        URI value = this.configProvider.getParameter(name, URI.class);
        return value != null ? value : defaultValue;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Config{");
        sb.append("configProvider=").append(configProvider);
        sb.append('}');
        return sb.toString();
    }

}
