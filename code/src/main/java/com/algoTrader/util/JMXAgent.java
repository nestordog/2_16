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

public class JMXAgent {

    public static final String RMI_REGISTRY_PORT = "com.algoTrarder.rmi.registryPort";
    public static final String RMI_SERVER_CONNECTION_PORT = "com.algoTrarder.rmi.serverPort";

    private JMXAgent() {
    }

    public static void premain(String agentArgs) throws Throwable {

        final int rmiRegistryPort = Integer.parseInt(System.getProperty(RMI_REGISTRY_PORT, "44444"));
        final int rmiServerPort = Integer.parseInt(System.getProperty(RMI_SERVER_CONNECTION_PORT, (rmiRegistryPort + 1) + ""));
        final String hostname = InetAddress.getLocalHost().getHostName();
        final String publicHostName = System.getProperty("java.rmi.server.hostname", hostname);

        System.out.println(RMI_REGISTRY_PORT + ":" + rmiRegistryPort);
        System.out.println(RMI_SERVER_CONNECTION_PORT + ":" + rmiServerPort);

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

        System.out.println("Local Connection URL: " + url);
        System.out.println("Public Connection URL: " + publicUrl);

        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
        cs.start();
    }
}
