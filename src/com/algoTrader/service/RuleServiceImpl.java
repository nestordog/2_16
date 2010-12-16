package com.algoTrader.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Rule;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.StrategyUtil;
import com.algoTrader.util.io.CsvTickInputAdapter;
import com.algoTrader.util.io.CsvTickInputAdapterSpec;
import com.algoTrader.util.io.DBInputAdapter;
import com.espertech.esper.adapter.InputAdapter;
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.ConfigurationVariable;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EPSubscriberException;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.SafeIterator;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;
import com.espertech.esper.jmx.client.ConnectorConfigPlatform;
import com.espertech.esper.jmx.client.JMXEndpoint;
import com.espertech.esper.jmx.client.JMXEndpointConfiguration;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esperio.AdapterCoordinator;
import com.espertech.esperio.AdapterCoordinatorImpl;
import com.espertech.esperio.csv.CSVInputAdapter;
import com.espertech.esperio.csv.CSVInputAdapterSpec;

public class RuleServiceImpl extends RuleServiceBase {

    private static Logger logger = MyLogger.getLogger(RuleServiceImpl.class.getName());
    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");

    private static final long initTime = 631148400000l; // 01.01.1990

    private Map<String, AdapterCoordinator> coordinators = new HashMap<String, AdapterCoordinator>();
    private Map<String, Boolean> internalClock = new HashMap<String, Boolean>();
    private Map<String, EPServiceProvider> serviceProviders = new HashMap<String, EPServiceProvider>();

    protected void handleInitServiceProvider(String strategyName) {

        String providerURI = getProviderURI(strategyName);

        Configuration configuration = new Configuration();
        configuration.configure("esper-" + providerURI.toLowerCase() + ".cfg.xml");

        initVariables(strategyName, configuration);

        Strategy strategy = getLookupService().getStrategyByNameFetched(strategyName);
        configuration.getVariables().get("strategy").setInitializationValue(strategy);

        EPServiceProvider serviceProvider = EPServiceProviderManager.getProvider(providerURI, configuration);

        // must send time event before first schedule pattern
        serviceProvider.getEPRuntime().sendEvent(new CurrentTimeEvent(initTime));
        this.internalClock.put(strategyName, false);

        this.serviceProviders.put(providerURI, serviceProvider);

        logger.debug("initialized service provider: " + strategyName);
    }

    protected boolean handleIsInitialized(String strategyName) throws Exception {

        return this.serviceProviders.containsKey(getProviderURI(strategyName));
    }

    protected void handleDestroyServiceProvider(String strategyName) {

        getServiceProvider(strategyName).destroy();
        this.serviceProviders.remove(getProviderURI(strategyName));

        logger.debug("destroyed service provider: " + strategyName);
    }

    protected void handleActivate(String strategyName, String ruleName) throws Exception {

        Rule rule = getLookupService().getRuleByName(ruleName);
        activate(strategyName, rule, null);
    }

    protected void handleActivate(String strategyName, String ruleName, int targetId) throws Exception {

        Rule rule = getLookupService().getRuleByName(ruleName);
        activate(strategyName, rule, targetId);
    }

    @SuppressWarnings("unchecked")
    protected void handleActivateInit(String strategyName) throws Exception {

        List<Rule> rules = getLookupService().getInitRules(strategyName);
        for (Rule rule : rules) {
            activate(strategyName, rule, null);
        }
    }

    @SuppressWarnings("unchecked")
    protected void handleActivateAll(String strategyName) throws java.lang.Exception {

        List<Rule> rules = getLookupService().getAutoActivateRules(strategyName);
        for (Rule rule : rules) {
            activate(strategyName, rule, null);
        }
    }

    protected boolean handleIsActive(String strategyName, String ruleName) throws Exception {

        EPStatement statement = getServiceProvider(strategyName).getEPAdministrator().getStatement(ruleName);

        if (statement != null && statement.isStarted()) {
            return true;
        } else {
            return false;
        }
    }

    protected void handleDeactivate(String strategyName, String ruleName) throws Exception {

        // destroy the statement
        EPStatement statement = getServiceProvider(strategyName).getEPAdministrator().getStatement(ruleName);

        if (statement != null && statement.isStarted()) {
            statement.destroy();
            logger.debug("deactivated rule " + ruleName);
        }
    }

    protected void handleDeactivateDependingOnTarget(String strategyName, String ruleName, int targetId) throws Exception {

        if (hasServiceProvider(strategyName)) {
            EPStatement statement = getServiceProvider(strategyName).getEPAdministrator().getStatement(ruleName);
            if (statement != null && statement.isStarted() && statement.getText().contains(String.valueOf(targetId))) {
                statement.destroy();
                logger.debug("deactivated rule " + ruleName);
            }
        }
    }

    protected void handleDeactivateAll(String strategyName) throws java.lang.Exception {

        EPAdministrator administrator = getServiceProvider(strategyName).getEPAdministrator();
        administrator.getStatementNames();
        for (String statementName : administrator.getStatementNames()) {
            deactivate(strategyName, statementName);
        }
    }

    protected void handleSendEvent(String strategyName, Object obj) {

        if (simulation) {
            getServiceProvider(strategyName).getEPRuntime().sendEvent(obj);
        } else {

            // check is it is the localStrategy
            if (StrategyUtil.getStartedStrategyName().equals(strategyName)) {
                getServiceProvider(strategyName).getEPRuntime().sendEvent(obj);
            } else {
                getStrategyService().sendEvent(strategyName, obj);
            }
        }
    }

    protected void handleRouteEvent(String strategyName, Object obj) {

        // routing always goes to the local engine
        getServiceProvider(strategyName).getEPRuntime().route(obj);
    }

    protected Object handleGetLastEvent(String strategyName, String ruleName) {

        EPStatement statement = getServiceProvider(strategyName).getEPAdministrator().getStatement(ruleName);
        if (statement != null && statement.isStarted()) {
            SafeIterator<EventBean> it = statement.safeIterator();
            try {
                if (it.hasNext()) {
                    return it.next().getUnderlying();
                }
            } finally {
                it.close();
            }
        }
        return null;
    }

    protected Object handleGetLastEventProperty(String strategyName, String ruleName, String property) throws Exception {

        EPStatement statement = getServiceProvider(strategyName).getEPAdministrator().getStatement(ruleName);
        if (statement != null && statement.isStarted()) {
            SafeIterator<EventBean> it = statement.safeIterator();
            try {
                return it.next().get(property);
            } finally {
                it.close();
            }
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected List handleGetAllEvents(String strategyName, String ruleName) {

        EPStatement statement = getServiceProvider(strategyName).getEPAdministrator().getStatement(ruleName);
        List list = new ArrayList();
        if (statement != null && statement.isStarted()) {
            SafeIterator<EventBean> it = statement.safeIterator();
            try {
                while (it.hasNext()) {
                    EventBean bean = it.next();
                    Object underlaying = bean.getUnderlying();
                    list.add(underlaying);
                }
            } finally {
                it.close();
            }
        }
        return list;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected List handleGetAllEventsProperty(String strategyName, String ruleName, String property) throws Exception {

        EPStatement statement = getServiceProvider(strategyName).getEPAdministrator().getStatement(ruleName);
        List list = new ArrayList();
        if (statement != null && statement.isStarted()) {
            SafeIterator<EventBean> it = statement.safeIterator();
            try {
                while (it.hasNext()) {
                    EventBean bean = it.next();
                    Object underlaying = bean.get(property);
                    list.add(underlaying);
                }
            } finally {
                it.close();
            }
        }
        return list;
    }

    protected void handleSetInternalClock(String strategyName, boolean internal) {

        this.internalClock.put(strategyName, internal);

        if (internal) {
            sendEvent(strategyName, new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_INTERNAL));
        } else {
            sendEvent(strategyName, new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
        }

        logger.debug("set internal clock to: " + internal + " for strategy: " + strategyName);
    }

    protected boolean handleGetInternalClock(String strategyName) {

        return this.internalClock.get(strategyName);
    }

    protected void handleSetCurrentTime(CurrentTimeEvent currentTimeEvent) {

        // sent currentTime to all local engines
        for (String providerURI : EPServiceProviderManager.getProviderURIs()) {
            sendEvent(providerURI, currentTimeEvent);
        }
    }

    protected long handleGetCurrentTime(String strategyName) {

        return getServiceProvider(strategyName).getEPRuntime().getCurrentTime();
    }

    protected void handleEnableManagement(String strategyName) {

        // Indicate that the platform MBeanServer should be used
        EPServiceProvider serviceProvider = getServiceProvider(strategyName);
        ConnectorConfigPlatform platformConfig = new ConnectorConfigPlatform();

        // Configure EsperJMX endpoint
        JMXEndpointConfiguration jmxConfig = new JMXEndpointConfiguration();
        jmxConfig.setConnectorConfiguration(platformConfig);
        jmxConfig.setCreateStmtListenerMBean(true);

        // Start EsperJMX endpoint
        JMXEndpoint endpoint = new JMXEndpoint(serviceProvider, jmxConfig);
        endpoint.start();

        logger.debug("enable management for strategy: " + strategyName);
    }

    protected void handleInitCoordination(String strategyName) throws Exception {

        this.coordinators.put(strategyName, new AdapterCoordinatorImpl(getServiceProvider(strategyName), true, true));
    }

    protected void handleCoordinate(String strategyName, CSVInputAdapterSpec csvInputAdapterSpec) throws Exception {

        InputAdapter inputAdapter;
        if (csvInputAdapterSpec instanceof CsvTickInputAdapterSpec) {
            inputAdapter = new CsvTickInputAdapter(getServiceProvider(strategyName), (CsvTickInputAdapterSpec) csvInputAdapterSpec);
        } else {
            inputAdapter = new CSVInputAdapter(getServiceProvider(strategyName), csvInputAdapterSpec);
        }
        this.coordinators.get(strategyName).coordinate(inputAdapter);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void handleCoordinate(String strategyName, Collection baseObjects, String timeStampColumn) throws Exception {

        InputAdapter inputAdapter = new DBInputAdapter(getServiceProvider(strategyName), baseObjects, timeStampColumn);
        this.coordinators.get(strategyName).coordinate(inputAdapter);
    }

    protected void handleStartCoordination(String strategyName) throws Exception {

        this.coordinators.get(strategyName).start();
    }

    protected void handleSetProperty(String strategyName, String key, String value) {

        key = key.replace(".", "_");
        EPRuntime runtime = getServiceProvider(strategyName).getEPRuntime();
        if (runtime.getVariableValueAll().containsKey(key)) {
            Class<?> clazz = runtime.getVariableValue(key).getClass();
            Object castedObj = JavaClassHelper.parse(clazz, value);
            runtime.setVariableValue(key, castedObj);
        }
    }

    @SuppressWarnings("rawtypes")
    private void activate(String strategyName, Rule rule, Integer targetId) throws java.lang.Exception {

        String definition = rule.getPrioritisedDefinition();
        String name = rule.getName();

        EPAdministrator administrator = getServiceProvider(strategyName).getEPAdministrator();

        // do nothing if the statement already exists
        EPStatement oldStatement = administrator.getStatement(name);
        if (oldStatement != null && oldStatement.isStarted()) {
            return;
        }

        if (targetId != null) {
            if (!rule.isPrepared()) {
                throw new RuleServiceException("target is allowed only on prepared rules");
            } else {
                definition = definition.replace("?", String.valueOf(targetId));
            }
        }

        // create the new statement
        EPStatement newStatement = null;
        try {
            if (rule.isPattern()) {
                newStatement = administrator.createPattern(definition, name);
            } else {
                newStatement = administrator.createEPL(definition, name);
            }

        } catch (Exception e) {
            logger.error("problem activating rule: " + name, e);
            throw e;
        }

        // add the subscribers
        if (rule.getSubscriber() != null) {
            Class cl = Class.forName(rule.getSubscriber().trim());
            Object obj = cl.newInstance();
            try {
                newStatement.setSubscriber(obj);
            } catch (EPSubscriberException e) {
                logger.error("problem activating rule: " + name, e);
                throw e;
            }
        }

        // add the listeners
        if (rule.getListeners() != null) {
            String[] listeners = rule.getListeners().split("\\s");
            for (String listener : listeners) {
                Class cl = Class.forName(listener);
                Object obj = cl.newInstance();
                if (obj instanceof StatementAwareUpdateListener) {
                    newStatement.addListener((StatementAwareUpdateListener) obj);
                } else {
                    newStatement.addListener((UpdateListener) obj);
                }
            }
        }

        logger.debug("activated rule " + rule.getName() + " on service provider: " + strategyName);
    }

    private String getProviderURI(String strategyName) {

        return (strategyName == null || "".equals(strategyName)) ? StrategyImpl.BASE : strategyName.toUpperCase();
    }

    private EPServiceProvider getServiceProvider(String strategyName) {

        String providerURI = getProviderURI(strategyName);

        EPServiceProvider serviceProvider = this.serviceProviders.get(providerURI);
        if (serviceProvider == null) {
            throw new RuleServiceException("strategy " + providerURI + " is not initialized yet!");
        }

        return serviceProvider;
    }

    private boolean hasServiceProvider(String strategyName) {

        String providerURI = getProviderURI(strategyName);

        EPServiceProvider serviceProvider = this.serviceProviders.get(providerURI);

        return (serviceProvider != null);
    }

    /**
     * initialize all the variables from the Configuration
     */
    private void initVariables(String strategyName, Configuration configuration) {

        try {
            Map<String, ConfigurationVariable> variables = configuration.getVariables();
            for (Map.Entry<String, ConfigurationVariable> entry : variables.entrySet()) {
                String key = entry.getKey().replace("_", ".");
                String obj = ConfigurationUtil.getStrategyConfig(strategyName).getString(key);
                if (obj != null) {
                    Class<?> clazz = Class.forName(entry.getValue().getType());
                    Object castedObj = JavaClassHelper.parse(clazz, obj);
                    entry.getValue().setInitializationValue(castedObj);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuleServiceException(e);
        }
    }
}
