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
package ch.algotrader.util.diff.convert;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry to lookup {@link ValueConverter} instances given a type class.
 */
public class ConverterRegistry {

    /**
     * Constant for default instances.
     */
    public static final ConverterRegistry DEFAULT = createDefaultRegistry();

    private final Map<Class<?>, ValueConverter<?>> convertersByType;

    private ConverterRegistry(Map<Class<?>, ValueConverter<?>> convertersByType) {
        this.convertersByType = new LinkedHashMap<Class<?>, ValueConverter<?>>(convertersByType);
    }

    private static ConverterRegistry createDefaultRegistry() {
        return new Builder()//
                .add(String.class, StringConverter.INSTANCE)//
                .add(Long.class, LongConverter.INSTANCE)//
                .add(Double.class, DoubleConverter.INSTANCE)//
                .add(Date.class, new DateConverter("yyyy-MM-dd HH:mm:ss"))//
                .build();
    }

    public static class Builder {
        private final Map<Class<?>, ValueConverter<?>> convertersByType = new LinkedHashMap<Class<?>, ValueConverter<?>>();

        public Builder() {
            super();
        }

        public Builder(ConverterRegistry base) {
            addAll(base);
        }

        public <T> Builder add(Class<T> type, ValueConverter<? extends T> converter) {
            convertersByType.put(type, converter);
            return this;
        }

        public <T> Builder addAll(ConverterRegistry registry) {
            convertersByType.putAll(registry.convertersByType);
            return this;
        }

        public ConverterRegistry build() {
            return new ConverterRegistry(convertersByType);
        }
    }

    /**
     * Returns the converter for the given type or null if no such converter is defined
     * by this registry.
     *
     * @param type the type
     * @return the converter for the specified type
     */
    @SuppressWarnings("unchecked")
    //safe as our builder only allows add for consistent types
    public <T> ValueConverter<? extends T> getConverterByType(Class<T> type) {
        return (ValueConverter<? extends T>) convertersByType.get(type);
    }

}
