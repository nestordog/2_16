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
package ch.algotrader.wiring.services;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.marketData.TickDao;
import ch.algotrader.entity.security.OptionDao;
import ch.algotrader.entity.security.OptionFamilyDao;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.service.ImportService;
import ch.algotrader.service.ImportServiceImpl;

/**
 * misc profile configuration.
 */
@Configuration
public class MiscWiring {

    @Profile("misc")
    @Bean(name = "importService")
    public ImportService createImportService(final CommonConfig commonConfig,
            final SessionFactory sessionFactory,
            final SecurityDao securityDao,
            final TickDao tickDao,
            final OptionFamilyDao optionFamilyDao,
            final OptionDao optionDao) {

        return new ImportServiceImpl(commonConfig, sessionFactory, securityDao, tickDao, optionFamilyDao, optionDao);
    }

}
