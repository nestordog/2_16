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
package ch.algotrader.wiring.common;

import java.util.Date;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.config.spring.ObjectToStringConverter;
import ch.algotrader.config.spring.StringToDateConverter;

/**
 * Common framework configuration.
 */
@Configuration
public class CommonConfigWiring {

    @Bean(name = "conversionService")
    public ConversionService createConversionService() {
        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(new StringToDateConverter());
        conversionService.addConverter(Date.class, String.class, new ObjectToStringConverter());
        return conversionService;
    }

    @Bean(name = "configParams")
    public ConfigParams createConfigParams() throws Exception {

        return ConfigLocator.instance().getConfigParams();
    }

    @Bean(name = "commonConfig")
    public CommonConfig createCommonConfig() {

        return ConfigLocator.instance().getConfig(CommonConfig.class);
    }

}

