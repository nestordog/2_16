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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.config.ConfigParams;
import ch.algotrader.jetty.EmbeddedJettyServer;
import ch.algotrader.util.collection.Pair;

@Configuration
@Profile("html5")
public class JettyWiring {

    @Bean(name = "jettyService", destroyMethod = "stop")
    public EmbeddedJettyServer createJettyService(
            final ConfigParams configParams,
            final SSLContext serverSSLContext,
            final ApplicationContext applicationContext) throws GeneralSecurityException, IOException {

        boolean sslEnabled = configParams.getBoolean("security.ssl");
        int httpPort = configParams.getInteger("jetty.http.port", 9090);
        int httpsPort = configParams.getInteger("jetty.https.port", 9443);
        String requestFile = configParams.getString("jetty.requestLog");
        String username = sslEnabled ? configParams.getString("jetty.user") : null;
        String password = sslEnabled ? configParams.getString("jetty.password") : null;

        List<Pair<String, String>> simpleCreds = sslEnabled && username != null && !username.isEmpty() ?
                Collections.singletonList(new Pair<>(username, password)) : null;

        return new EmbeddedJettyServer(
                sslEnabled ? httpsPort : httpPort,
                requestFile,
                simpleCreds,
                serverSSLContext,
                applicationContext);
    }

}
