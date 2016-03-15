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

package ch.algotrader.rest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ch.algotrader.config.ConfigParams;

@RestController
@RequestMapping(path = "/rest")
public class ConfigRestController extends RestControllerBase {

    private final ConfigParams configParams;

    public ConfigRestController(final ConfigParams configParams) {
        this.configParams = configParams;
    }

    private String getBrokerHost() {
        String host = this.configParams.getString("activeMQ.host");
        if ("localhost".equalsIgnoreCase(host)) {
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                host = localHost.getHostName();
            } catch (IOException ex) {
            }
        }
        return host;
    }

    @CrossOrigin
    @RequestMapping(path = "/broker/url/main", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public URI getBrokerMainURI() throws URISyntaxException {

        return new URI("tcp", null, getBrokerHost(), this.configParams.getInteger("activeMQ.port"), null, null, null);
    }

    @CrossOrigin
    @RequestMapping(path = "/broker/url/ws", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public URI getBrokerWebsocketURI() throws URISyntaxException {

        boolean sslEnabled = configParams.getBoolean("security.ssl");
        if (sslEnabled) {
            return new URI("wss", null, getBrokerHost(), this.configParams.getInteger("activeMQ.wss.port"), null, null, null);
        } else {
            return new URI("ws", null, getBrokerHost(), this.configParams.getInteger("activeMQ.ws.port"), null, null, null);
        }
    }

    @CrossOrigin
    @RequestMapping(path = "/config-params", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> getConfigParams() throws URISyntaxException {

        List<String> names = new ArrayList<>(this.configParams.getConfigProvider().getNames());
        Collections.sort(names);
        Map<String, String> map = new LinkedHashMap<>(names.size());
        for (String name: names) {
            String value = this.configParams.getParameter(name, String.class);
            if (value != null) {
                map.put(name, value);
            }
        }
        return map;
    }

}
