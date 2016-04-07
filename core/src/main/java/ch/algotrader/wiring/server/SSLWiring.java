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
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import ch.algotrader.config.ConfigParams;

@Configuration
public class SSLWiring {

    private static final String CLASS_PATH_SCHEME = "classpath://";
    private static final String FILE_SCHEME = "file://";

    @Bean(name = "serverSSLContext")
    public SSLContext createSSLContext(final ConfigParams configParams) throws GeneralSecurityException, IOException {

        boolean sslEnabled = configParams.getBoolean("security.ssl");
        String keystoreResource = configParams.getString("ssl.keystore");
        String keystoreType = configParams.getString("ssl.keystoreType");
        String keystorePassword = configParams.getString("ssl.keystorePassword");
        String keyPassword = configParams.getString("ssl.keyPassword");

        if (sslEnabled && keystoreResource != null && !keystoreResource.isEmpty()) {

            Resource resource;
            if (keystoreResource.startsWith(CLASS_PATH_SCHEME)) {
                resource = new ClassPathResource(keystoreResource.substring(CLASS_PATH_SCHEME.length()));
            } else if (keystoreResource.startsWith(FILE_SCHEME)) {
                resource = new FileSystemResource(keystoreResource.substring(FILE_SCHEME.length()));
            } else if (keystoreResource.contains("://")) {
                resource = new UrlResource(keystoreResource);
            } else {
                resource = new ClassPathResource(keystoreResource);
            }

            KeyStore keystore = KeyStore.getInstance(keystoreType != null ? keystoreType : KeyStore.getDefaultType());
            try (InputStream instream = resource.getInputStream()) {
                keystore.load(instream, keystorePassword != null ? keystorePassword.toCharArray() : null);
            }

            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(keystore, keyPassword.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmfactory.getKeyManagers(), null, null);

            return sslContext;
        } else {
            return null;
        }
    }

}
