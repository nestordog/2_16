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

package ch.algotrader.wiring.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.algotrader.rest.jetty.EmbeddedJettyServer;

@Configuration
public class JettyWiring {

    @Bean(name = "jettyService", destroyMethod = "stop")
    public EmbeddedJettyServer createJettyService(
            @Value("${http.port}") final int port,
            final ApplicationContext applicationContext) {

        return new EmbeddedJettyServer(port, applicationContext);
    }

}
