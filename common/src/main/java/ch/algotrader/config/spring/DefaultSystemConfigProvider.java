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
package ch.algotrader.config.spring;

import java.util.Map;

import org.springframework.core.convert.ConversionService;

import ch.algotrader.config.ConfigProvider;

/**
 * Extension of {@link DefaultConfigProvider} that enforces precedence of
 * system properties over the content of {@link java.util.Map}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultSystemConfigProvider extends DefaultConfigProvider {

    public DefaultSystemConfigProvider(
            final Map<String, ?> paramMap,
            final ConversionService conversionService,
            final ConfigProvider defaultConfigProvider) {
        super(paramMap, conversionService, defaultConfigProvider);
    }

    public DefaultSystemConfigProvider(
            final Map<String, ?> paramMap,
            final ConversionService conversionService) {
        super(paramMap, conversionService, null);
    }

    public DefaultSystemConfigProvider(final Map<String, ?> paramMap) {
        super(paramMap);
    }

    @Override
    protected Object getRawValue(final String name) {

        Object systemProp = System.getProperty(name);
        if (systemProp != null) {
            return systemProp;
        } else {
            return super.getRawValue(name);
        }

    }

}
