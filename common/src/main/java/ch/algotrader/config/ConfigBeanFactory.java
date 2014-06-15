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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.Validate;

/**
 * Generic bean factory for config beans annotated with {@link ch.algotrader.config.ConfigName} annotation.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public final class ConfigBeanFactory {

    @SuppressWarnings("unchecked")
    public <T> T create(final ConfigParams configParams, final Class<T> clazz) {

        Validate.notNull(configParams, "ConfigParams is null");
        Validate.notNull(clazz, "Target class is null");

        Constructor<?>[] constructors = clazz.getConstructors();
        if (constructors.length != 1) {

            throw new ConfigBeanCreationException(clazz.getName() + " config bean class is expected to declare one constructor only");
        }

        Constructor<?> constructor = constructors[0];
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        if (parameterTypes.length != parameterAnnotations.length) {

            throw new ConfigBeanCreationException(clazz.getName() + " config bean metadata is inconsistent");
        }

        Object[] paramValues = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {

            Class<?> paramType = parameterTypes[i];
            Annotation[] annotations = parameterAnnotations[i];
            ConfigName configName = null;
            for (Annotation annotation: annotations) {
                if (annotation.annotationType().equals(ConfigName.class)) {
                    configName = (ConfigName) annotation;
                    break;
                }
            }
            if (configName == null) {

                throw new ConfigBeanCreationException(clazz.getName() + " config bean parameter does not have mandatory metadata");
            }
            Object paramValue = configParams.getParameter(configName.value(), paramType);
            if (paramValue == null) {
                throw new ConfigBeanCreationException("Config parameter '" + configName.value() + "' is undefined");

            }
            paramValues[i] = paramValue;
        }
        try {
            return (T) constructor.newInstance(paramValues);
        } catch (InstantiationException ex) {
            throw new ConfigBeanCreationException(ex);
        } catch (IllegalAccessException ex) {
            throw new ConfigBeanCreationException(ex);
        } catch (InvocationTargetException ex) {
            throw new ConfigBeanCreationException(ex);
        }
    }

}
