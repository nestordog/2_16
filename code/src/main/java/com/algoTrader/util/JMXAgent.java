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

import org.apache.log4j.Logger;

public class JMXAgent {

    private static Logger logger = MyLogger.getLogger(JMXAgent.class.getName());

    public static final String RMI_REGISTRY_PORT = "com.algoTrarder.rmi.registryPort";
    public static final String RMI_SERVER_CONNECTION_PORT = "com.algoTrarder.rmi.serverPort";

    private JMXAgent() {
    }

    public static void premain(String agentArgs) throws Throwable {

        final int rmiRegistryPort = Integer.parseInt(System.getProperty(RMI_REGISTRY_PORT, "44444"));
        final int rmiServerPort = Integer.parseInt(System.getProperty(RMI_SERVER_CONNECTION_PORT, (rmiRegistryPort + 1) + ""));
        final String hostname = InetAddress.getLocalHost().getHostName();
        final String publicHostName = System.getProperty("java.rmi.server.hostname", hostname);

        logger.debug(RMI_REGISTRY_PORT + ":" + rmiRegistryPort);
        logger.debug(RMI_SERVER_CONNECTION_PORT + ":" + rmiServerPort);

        // Ensure cryptographically strong random number generator used
        // to choose the object number - see java.rmi.server.ObjID
        System.setProperty("java.rmi.server.randomIDs", "true");

        LocateRegistry.createRegistry(rmiRegistryPort);

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        Map<String, Object> env = new HashMap<String, Object>();

        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + hostname + ":" + rmiServerPort + "/jndi/rmi://" + hostname + ":" + rmiRegistryPort
                + "/jmxrmi");

        // Used only to display what the public address should be
        JMXServiceURL publicUrl = new JMXServiceURL("service:jmx:rmi://" + publicHostName + ":" + rmiServerPort + "/jndi/rmi://" + publicHostName + ":"
                + rmiRegistryPort + "/jmxrmi");

        logger.debug("Local Connection URL: " + url);
        logger.debug("Public Connection URL: " + publicUrl);

        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
        cs.start();
    }
}
