/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.util;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class JMXAgent {

    public static final String RMI_REGISTRY_PORT = "com.algoTrarder.rmi.registryPort";
    public static final String RMI_SERVER_CONNECTION_PORT = "com.algoTrarder.rmi.serverPort";

    private JMXAgent() {
    }

    public static void premain(String agentArgs) throws Throwable {

        final int rmiRegistryPort = Integer.parseInt(System.getProperty(RMI_REGISTRY_PORT, "1099"));
        final int rmiServerPort = Integer.parseInt(System.getProperty(RMI_SERVER_CONNECTION_PORT, (rmiRegistryPort + 1) + ""));
        final String hostname = InetAddress.getLocalHost().getHostName();
        final String publicHostName = System.getProperty("java.rmi.server.hostname", hostname);

        // Ensure cryptographically strong random number generator used
        // to choose the object number - see java.rmi.server.ObjID
        System.setProperty("java.rmi.server.randomIDs", "true");

        LocateRegistry.createRegistry(rmiRegistryPort);

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        Map<String, Object> env = new HashMap<String, Object>();
        // Provide SSL-based RMI socket factories.
        //
        // The protocol and cipher suites to be enabled will be the ones
        // defined by the default JSSE implementation and only server
        // authentication will be required.
        //
        SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
        SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();
        env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
        env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);

        JMXServiceURL privateUrl = new JMXServiceURL("service:jmx:rmi://" + hostname + ":" + rmiServerPort + "/jndi/rmi://" + hostname + ":" + rmiRegistryPort + "/jmxrmi");

        // Used only to display what the public address should be
        JMXServiceURL publicUrl = new JMXServiceURL("service:jmx:rmi://" + publicHostName + ":" + rmiServerPort + "/jndi/rmi://" + publicHostName + ":" + rmiRegistryPort + "/jmxrmi");

        System.out.println("Local Connection URL: " + privateUrl);
        System.out.println("Public Connection URL: " + publicUrl);

        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(privateUrl, env, mbs);
        cs.start();
    }
}
