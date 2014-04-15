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
package ch.algotrader.util;

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
 * Custom JMX Agent that allows the definition of a fixed RMI Server Port.
 * <p>
 * By default only the RMI Registry Port can be defined through vm arguments (com.sun.management.jmxremote.port).
 * The RMI Server Port is chosen randomly which is not feasible through firewalls.
 * <p>
 * The JMX Agent takes the following two vm arguments to define both ports:
 * <pre>
 * -Dch.algotrader.rmi.registryPort
 * -Dch.algotrader.rmi.serverPort</pre>
 * In order to use this Custom JMXAgent the following has to be specified on the commandline:
 * <pre>-javaagent:lib/agent.jar</pre>
 * In addition the following vm argument has to be specified, which defines the public external host name:
 * <pre>-Djava.rmi.server.hostname=algotrader.ch</pre>
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class JMXAgent {

    public static final String RMI_REGISTRY_PORT = "ch.algotrader.rmi.registryPort";
    public static final String RMI_SERVER_CONNECTION_PORT = "ch.algotrader.rmi.serverPort";
    public static final String RMI_SSL = "ch.algotrader.rmi.ssl";

    private JMXAgent() {
    }

    public static void premain(String agentArgs) throws Throwable {

        final int rmiRegistryPort = Integer.parseInt(System.getProperty(RMI_REGISTRY_PORT, "1099"));
        final int rmiServerPort = Integer.parseInt(System.getProperty(RMI_SERVER_CONNECTION_PORT, (rmiRegistryPort + 1) + ""));
        final boolean rmiSsl = Boolean.parseBoolean(System.getProperty(RMI_SSL, "false"));

        final String hostname = InetAddress.getLocalHost().getHostName();
        final String publicHostName = System.getProperty("java.rmi.server.hostname", hostname);

        // Ensure cryptographically strong random number generator used
        System.setProperty("java.rmi.server.randomIDs", "true");

        LocateRegistry.createRegistry(rmiRegistryPort);

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        Map<String, Object> env = new HashMap<String, Object>();

        // Provide SSL-based RMI socket factories.
        if (rmiSsl) {
            SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
            SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();
            env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
            env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
        }

        JMXServiceURL privateUrl = new JMXServiceURL("service:jmx:rmi://" + hostname + ":" + rmiServerPort + "/jndi/rmi://" + hostname + ":" + rmiRegistryPort + "/jmxrmi");

        // Used only to display what the public address should be
        JMXServiceURL publicUrl = new JMXServiceURL("service:jmx:rmi://" + publicHostName + ":" + rmiServerPort + "/jndi/rmi://" + publicHostName + ":" + rmiRegistryPort + "/jmxrmi");

        System.out.println("Local Connection URL: " + privateUrl);
        System.out.println("Public Connection URL: " + publicUrl);

        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(privateUrl, env, mbs);
        cs.start();
    }
}
