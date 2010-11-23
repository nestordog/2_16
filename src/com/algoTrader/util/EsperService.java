package com.algoTrader.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.algoTrader.enumeration.RuleName;
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.ConfigurationVariable;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.SafeIterator;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;
import com.espertech.esper.jmx.client.ConnectorConfigPlatform;
import com.espertech.esper.jmx.client.JMXEndpoint;
import com.espertech.esper.jmx.client.JMXEndpointConfiguration;
import com.espertech.esper.util.JavaClassHelper;

public class EsperService {

    private static EPServiceProvider cep;
    private static boolean internalClock = false;

    public static EPServiceProvider getEPServiceInstance() {

        if (cep == null) {

            Configuration config = new Configuration();
            config.configure();

            try {
                Map<String, ConfigurationVariable> variables = config.getVariables();
                for (Map.Entry<String, ConfigurationVariable> entry : variables.entrySet()) {
                    String key = entry.getKey().replace("_", ".");
                    String obj = ConfigurationUtil.getBaseConfig().getString(key);
                    Class<?> clazz = Class.forName(entry.getValue().getType());
                    Object castedObj = JavaClassHelper.parse(clazz, obj);
                    entry.getValue().setInitializationValue(castedObj);
                }
            } catch (ClassNotFoundException e) {
                // do nothing
            }

            cep = EPServiceProviderManager.getDefaultProvider(config);

            // must send time event (1.1.1990) before first schedule pattern
            cep.getEPRuntime().sendEvent(new CurrentTimeEvent(631148400000l));
        }
        return cep;
    }

    public static void destroyEPServiceInstance() {

        if (cep != null) {

            cep.destroy();
            cep = null;
        }
    }

    public static boolean hasInstance() {

        return (cep != null);
    }

    public static EPStatement getStatement(RuleName ruleName) {

        return getEPServiceInstance().getEPAdministrator().getStatement(ruleName.getValue());
    }

    public static long getCurrentTime() {

        return getEPServiceInstance().getEPRuntime().getCurrentTime();
    }

    public static void sendEvent(Object obj) {

        getEPServiceInstance().getEPRuntime().sendEvent(obj);
    }

    public static void route(Object obj) {

        getEPServiceInstance().getEPRuntime().route(obj);
    }

    /**
     * use if you don't know what the rule will return
     *
     * @return plain objects (non generic)
     */
    public static Object getLastEvent(RuleName ruleName) {

        EPStatement statement = getStatement(ruleName);
        if (statement != null && statement.isStarted()) {
            SafeIterator<EventBean> it = statement.safeIterator();
            try {
                while (it.hasNext()) {
                    EventBean bean = it.next();
                    return bean.getUnderlying();
                }
            } finally {
                it.close();
            }
        }
        return null;
    }

    /**
     *
     * @return Object of type E
     */
    public static <E> E getLastEvent(RuleName ruleName, Class<E> clazz) {

        EPStatement statement = getStatement(ruleName);
        if (statement != null && statement.isStarted()) {
            SafeIterator<EventBean> it = statement.safeIterator();
            try {
                while (it.hasNext()) {
                    EventBean bean = it.next();
                    Object underlaying = bean.getUnderlying();
                    return clazz.cast(underlaying);
                }
            } finally {
                it.close();
            }
        }
        return null;
    }

    /**
     *
     * @return List with object of type E
     */
    public static <E> List<E> getAllEvents(RuleName ruleName, Class<E> clazz) {

        EPStatement statement = getStatement(ruleName);
        List<E> list = new ArrayList<E>();
        if (statement != null && statement.isStarted()) {
            SafeIterator<EventBean> it = statement.safeIterator();
            try {
                while (it.hasNext()) {
                    EventBean bean = it.next();
                    Object underlaying = bean.getUnderlying();
                    E e = clazz.cast(underlaying);
                    list.add(e);
                }
            } finally {
                it.close();
            }
        }
        return list;
    }

    public static void setInternalClock(boolean internal) {

        internalClock = internal;

        if (internal) {
            EsperService.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_INTERNAL));
        } else {
            EsperService.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
        }
    }

    public static boolean getInternalClock() {

        return internalClock;
    }

    public static void setPropertyValue(String key, String value) {

        key = key.replace(".", "_");
        EPRuntime runtime = cep.getEPRuntime();
        if (runtime.getVariableValueAll().containsKey(key)) {
            Class<?> clazz = runtime.getVariableValue(key).getClass();
            Object castedObj = JavaClassHelper.parse(clazz, value);
            runtime.setVariableValue(key, castedObj);
        }
    }

    public static void enableJmx() {

        // Indicate that the platform MBeanServer should be used
        EPServiceProvider cep = getEPServiceInstance();
        ConnectorConfigPlatform platformConfig = new ConnectorConfigPlatform();

        // Configure EsperJMX endpoint
        JMXEndpointConfiguration jmxConfig = new JMXEndpointConfiguration();
        jmxConfig.setConnectorConfiguration(platformConfig);
        jmxConfig.setCreateStmtListenerMBean(true);

        // Start EsperJMX endpoint
        JMXEndpoint endpoint = new JMXEndpoint(cep, jmxConfig);
        endpoint.start();
    }
}
