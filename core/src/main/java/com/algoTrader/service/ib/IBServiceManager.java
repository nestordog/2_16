package com.algoTrader.service.ib;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.ClassUtils;

import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.util.ServiceUtil;

public class IBServiceManager {

    public static void init() {

        for (IBServiceI service : ServiceUtil.getServicesByInterface(IBServiceI.class)) {
            service.init();
        }
    }

    public static void connect() {

        for (IBServiceI service : ServiceUtil.getServicesByInterface(IBServiceI.class)) {
            service.connect();
        }
    }

    public static Map<String, ConnectionState> getAllConnectionStates() {

        Map<String, ConnectionState> connectionStates = new HashMap<String, ConnectionState>();
        for (IBServiceI service : ServiceUtil.getServicesByInterface(IBServiceI.class)) {
            String className = ClassUtils.getShortName(service.getClass().getInterfaces()[0]);
            connectionStates.put(className, service.getConnectionState());
        }
        return connectionStates;
    }
}
