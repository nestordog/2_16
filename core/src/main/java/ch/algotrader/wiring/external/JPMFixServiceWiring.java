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
package ch.algotrader.wiring.external;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.adapter.fix.ManagedFixAdapter;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.ordermgmt.OrderBook;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.jpm.JPMFixOrderServiceImpl;

/**
 * JPM Fix service configuration.
 */
@Configuration
public class JPMFixServiceWiring {

    @Profile("jPMFix")
    @Bean(name = "jPMFixOrderService")
    public JPMFixOrderServiceImpl createJPMFixOrderService(
            final ManagedFixAdapter fixAdapter,
            final OrderBook orderBook,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final AccountDao accountDao,
            final CommonConfig commonConfig) {

        return new JPMFixOrderServiceImpl(fixAdapter, orderBook, orderPersistenceService, orderDao, accountDao, commonConfig);
    }

}
