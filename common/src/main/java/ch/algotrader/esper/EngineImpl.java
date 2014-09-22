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
package ch.algotrader.esper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import ch.algotrader.ServiceLocator;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.esper.annotation.Condition;
import ch.algotrader.esper.annotation.Listeners;
import ch.algotrader.esper.annotation.RunTimeOnly;
import ch.algotrader.esper.annotation.SimulationOnly;
import ch.algotrader.esper.annotation.Subscriber;
import ch.algotrader.esper.callback.ClosePositionCallback;
import ch.algotrader.esper.callback.OpenPositionCallback;
import ch.algotrader.esper.callback.TickCallback;
import ch.algotrader.esper.callback.TimerCallback;
import ch.algotrader.esper.callback.TradeCallback;
import ch.algotrader.esper.io.CustomSender;
import ch.algotrader.esper.subscriber.SubscriberCreator;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.collection.CollectionUtil;
import ch.algotrader.util.metric.MetricsUtil;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.ConfigurationEngineDefaults.Threading;
import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.client.ConfigurationVariable;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPOnDemandQueryResult;
import com.espertech.esper.client.EPPreparedStatement;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.SafeIterator;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.annotation.Name;
import com.espertech.esper.client.deploy.DeploymentInformation;
import com.espertech.esper.client.deploy.EPDeploymentAdmin;
import com.espertech.esper.client.deploy.Module;
import com.espertech.esper.client.deploy.ModuleItem;
import com.espertech.esper.client.deploy.ParseException;
import com.espertech.esper.client.soda.AnnotationPart;
import com.espertech.esper.client.soda.EPStatementObjectModel;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;
import com.espertech.esper.core.deploy.EPLModuleUtil;
import com.espertech.esper.core.service.EPServiceProviderImpl;
import com.espertech.esper.core.service.EPServiceProviderSPI;
import com.espertech.esper.epl.annotation.AnnotationUtil;
import com.espertech.esper.epl.core.EngineImportService;
import com.espertech.esper.epl.spec.AnnotationDesc;
import com.espertech.esper.epl.spec.StatementSpecMapper;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esperio.AdapterCoordinator;
import com.espertech.esperio.AdapterCoordinatorImpl;
import com.espertech.esperio.CoordinatedAdapter;

/**
 * Esper based implementation of an {@link Engine}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EngineImpl extends AbstractEngine {

    private static final Logger logger = MyLogger.getLogger(EngineImpl.class.getName());
    private static final String newline = System.getProperty("line.separator");
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_kkmmss");

    private EPServiceProvider serviceProvider;
    private AdapterCoordinator coordinator;

    private ThreadLocal<AtomicBoolean> processing = new ThreadLocal<AtomicBoolean>() {
        @Override
        protected AtomicBoolean initialValue() {
            return new AtomicBoolean(false);
        }
    };

    /**
     * Initializes a new Engine by the given {@code engineName}.
     * The following steps are exectued:
     * <ul>
     * <li>{@code corresponding esper-xxx.cfg.xml} files are loaded from the classpath</li>
     * <li>Esper variables are initilized</li>
     * <li>Esper threading is configured</li>
     * <li>The {@link Strategy} itself is configured as an Esper variable {@code engineStrategy}</li>
     * <li>Esper Time is set to zero</li>
     * </ul>
     */
    public EngineImpl(String engineName) {

        this.engineName = engineName;

        Configuration configuration = new Configuration();

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:/META-INF/esper-**.cfg.xml");
            for (Resource resource : resources) {
                if (StrategyImpl.BASE.equals(engineName)) {

                    // for Base only load esper-common.cfg.xml and esper-core.cfg.xml
                    if (resource.toString().contains("esper-common.cfg.xml") || resource.toString().contains("esper-core.cfg.xml")) {
                        configuration.configure(resource.getURL());
                    }
                } else {

                    // for Strategies to not load esper-core.cfg.xml
                    if (!resource.toString().contains("esper-core.cfg.xml")) {
                        configuration.configure(resource.getURL());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("problem loading esper config", e);
        }

        initVariables(configuration);

        CommonConfig commonConfig = ConfigLocator.instance().getCommonConfig();
        // outbound threading for BASE
        if (StrategyImpl.BASE.equals(engineName) && !commonConfig.isSimulation()) {

            ConfigParams configParams = ConfigLocator.instance().getConfigParams();
            Threading threading = configuration.getEngineDefaults().getThreading();

            threading.setThreadPoolOutbound(true);
            threading.setThreadPoolOutboundNumThreads(configParams.getInteger("misc.outboundThreads"));
        }

        Strategy strategy = ServiceLocator.instance().getLookupService().getStrategyByName(engineName);
        configuration.getVariables().get("engineStrategy").setInitializationValue(strategy);

        this.serviceProvider = EPServiceProviderManager.getProvider(engineName, configuration);

        // must send time event before first schedule pattern
        this.serviceProvider.getEPRuntime().sendEvent(new CurrentTimeEvent(0));

        this.internalClock = false;

        logger.debug("initialized service provider: " + engineName);
    }

    @Override
    public boolean isDestroyed() {
        return serviceProvider.isDestroyed();
    }

    @Override
    public void destroy() {

        this.serviceProvider.destroy();

        logger.debug("destroyed service provider: " + this.engineName);
    }

    @Override
    public void deployStatement(String moduleName, String statementName) {

        deployStatement(moduleName, statementName, null, new Object[] {}, null);
    }

    @Override
    public void deployStatement(String moduleName, String statementName, String alias, Object[] params) {

        deployStatement(moduleName, statementName, alias, params, null);
    }

    @Override
    public void deployStatement(String moduleName, String statementName, String alias, Object[] params, Object callback) {

        deployStatement(moduleName, statementName, alias, params, callback, false);
    }

    @Override
    public void deployStatement(String moduleName, String statementName, String alias, Object[] params, Object callback, boolean force) {

        // check if statement with same name exists
        if (!force) {
            EPStatement existingStatement = this.serviceProvider.getEPAdministrator().getStatement(statementName);
            if (existingStatement != null && existingStatement.isStarted()) {
                logger.warn(statementName + " is already deployed and started");
                return;
            }
        }

        Module module = getModule(moduleName);

        // go through all statements in the module
        EPStatement newStatement = null;
        for (ModuleItem moduleItem : module.getItems()) {

            if (isEligibleStatement(this.serviceProvider, moduleItem, statementName)) {

                newStatement = startStatement(moduleItem, this.serviceProvider, alias, params, callback);

                // break iterating over the statements
                break;

            } else {
                continue;
            }
        }

        if (newStatement == null) {
            logger.warn("statement " + statementName + " was not found");
        } else {
            logger.debug("deployed statement " + newStatement.getName());
        }
    }

    @Override
    public void deployAllModules() {

        deployInitModules();
        deployRunModules();
    }

    @Override
    public void deployInitModules() {

        Strategy strategy = ServiceLocator.instance().getLookupService().getStrategyByName(this.engineName);
        if (strategy == null) {
            throw new IllegalStateException("strategy " + this.engineName + " was not found in database");
        }
        String initModules = strategy.getInitModules();
        if (initModules != null) {
            String[] modules = initModules.split(",");
            for (String module : modules) {
                deployModule(module.trim());
            }
        }
    }

    @Override
    public void deployRunModules() {

        Strategy strategy = ServiceLocator.instance().getLookupService().getStrategyByName(this.engineName);
        if (strategy == null) {
            throw new IllegalStateException("strategy " + this.engineName + " was not found in database");
        }
        String runModules = strategy.getRunModules();
        if (runModules != null) {
            String[] modules = runModules.split(",");
            for (String module : modules) {
                deployModule(module.trim());
            }
        }
    }

    @Override
    public void deployModule(String moduleName) {

        Module module = getModule(moduleName);

        for (ModuleItem moduleItem : module.getItems()) {

            if (isEligibleStatement(this.serviceProvider, moduleItem, null)) {

                startStatement(moduleItem, this.serviceProvider, null, new Object[] {}, null);

            } else {
                continue;
            }
        }

        logger.debug("deployed module " + moduleName);
    }

    @Override
    public boolean isDeployed(final String statementNameRegex) {

        // find the first statement that matches the given statementName regex
        String[] statementNames = findStatementNames(statementNameRegex);

        if (statementNames.length == 0) {
            return false;
        } else if (statementNames.length > 1) {
            logger.error("more than one statement matches: " + statementNameRegex);
        }

        // get the statement
        EPAdministrator administrator = this.serviceProvider.getEPAdministrator();
        EPStatement statement = administrator.getStatement(statementNames[0]);

        if (statement != null && statement.isStarted()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void undeployStatement(String statementName) {

        // destroy the statement
        EPStatement statement = this.serviceProvider.getEPAdministrator().getStatement(statementName);

        if (statement != null && statement.isStarted()) {
            statement.destroy();
            logger.debug("undeployed statement " + statementName);
        }
    }

    @Override
    public void restartStatement(String statementName) {

        // destroy the statement
        EPStatement statement = this.serviceProvider.getEPAdministrator().getStatement(statementName);

        if (statement != null && statement.isStarted()) {
            statement.stop();
            statement.start();
            logger.debug("restarted statement " + statementName);
        }
    }

    @Override
    public void undeployModule(String moduleName) {

        EPAdministrator administrator = this.serviceProvider.getEPAdministrator();
        EPDeploymentAdmin deployAdmin = administrator.getDeploymentAdmin();
        for (DeploymentInformation deploymentInformation : deployAdmin.getDeploymentInformation()) {
            if (deploymentInformation.getModule().getName().equals(moduleName)) {
                try {
                    deployAdmin.undeploy(deploymentInformation.getDeploymentId());
                } catch (Exception e) {
                    throw new RuntimeException("module " + moduleName + " could no be undeployed", e);
                }
            }
        }

        logger.debug("undeployed module " + moduleName);
    }

    @Override
    public void addEventType(String eventTypeName, String eventClassName) {

        ConfigurationOperations configuration = this.serviceProvider.getEPAdministrator().getConfiguration();
        if (configuration.getEventType(eventTypeName) == null) {
            configuration.addEventType(eventTypeName, eventClassName);
        }
    }

    @Override
    public void sendEvent(Object obj) {

        long startTime = System.nanoTime();

        // if the engine is currently processing and event route the new event
        if (this.processing.get().get()) {
            this.serviceProvider.getEPRuntime().route(obj);
        } else {
            this.processing.get().set(true);
            this.serviceProvider.getEPRuntime().sendEvent(obj);
            this.processing.get().set(false);
        }

        MetricsUtil.accountEnd("EsperManager." + obj.getClass(), startTime);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List executeQuery(String query) {

        List<Object> objects = new ArrayList<Object>();
        EPOnDemandQueryResult result = this.serviceProvider.getEPRuntime().executeQuery(query);
        for (EventBean row : result.getArray()) {
            Object object = row.getUnderlying();
            objects.add(object);
        }
        return objects;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object executeSingelObjectQuery(String query) {

        List<Object> events = executeQuery(query);
        if (events.size() == 0) {
            return null;
        } else if (events.size() == 1) {
            return events.get(0);
        } else {
            throw new IllegalArgumentException("query returned more than one object");
        }
    }

    @Override
    public Object getLastEvent(String statementName) {

        EPStatement statement = this.serviceProvider.getEPAdministrator().getStatement(statementName);
        if (statement == null) {
            throw new IllegalStateException("statement " + statementName + " does not exist");
        } else if (!statement.isStarted()) {
            throw new IllegalStateException("statement " + statementName + " is not started");
        } else {
            SafeIterator<EventBean> it = statement.safeIterator();
            try {
                if (it.hasNext()) {
                    return it.next().getUnderlying();
                } else {
                    return null;
                }
            } finally {
                it.close();
            }
        }
    }

    @Override
    public Object getLastEventProperty(String statementName, String propertyName) {

        EPStatement statement = this.serviceProvider.getEPAdministrator().getStatement(statementName);
        if (statement == null) {
            throw new IllegalStateException("statement " + statementName + " does not exist");
        } else if (!statement.isStarted()) {
            throw new IllegalStateException("statement " + statementName + " is not started");
        } else {
            SafeIterator<EventBean> it = statement.safeIterator();
            try {
                if (it.hasNext()) {
                    return it.next().get(propertyName);
                } else {
                    return null;
                }
            } finally {
                it.close();
            }
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List getAllEvents(String statementName) {

        EPStatement statement = this.serviceProvider.getEPAdministrator().getStatement(statementName);
        if (statement == null) {
            throw new IllegalStateException("statement " + statementName + " does not exist");
        } else if (!statement.isStarted()) {
            throw new IllegalStateException("statement " + statementName + " is not started");
        } else {
            List<Object> list = new ArrayList<Object>();
            SafeIterator<EventBean> it = statement.safeIterator();
            try {
                while (it.hasNext()) {
                    EventBean bean = it.next();
                    Object underlying = bean.getUnderlying();
                    list.add(underlying);
                }
                return list;
            } finally {
                it.close();
            }
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List getAllEventsProperty(String statementName, String property) {

        EPStatement statement = this.serviceProvider.getEPAdministrator().getStatement(statementName);
        if (statement == null) {
            throw new IllegalStateException("statement " + statementName + " does not exist");
        } else if (!statement.isStarted()) {
            throw new IllegalStateException("statement " + statementName + " is not started");
        } else {
            List<Object> list = new ArrayList<Object>();
            SafeIterator<EventBean> it = statement.safeIterator();
            try {
                while (it.hasNext()) {
                    EventBean bean = it.next();
                    Object underlying = bean.get(property);
                    list.add(underlying);
                }
                return list;
            } finally {
                it.close();
            }
        }
    }

    @Override
    public void setInternalClock(boolean internalClock) {

        super.setInternalClock(internalClock);

        if (internalClock) {
            sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_INTERNAL));
            EPServiceProviderImpl provider = (EPServiceProviderImpl) this.serviceProvider;
            provider.getTimerService().enableStats();
        } else {
            sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
            EPServiceProviderImpl provider = (EPServiceProviderImpl) this.serviceProvider;
            provider.getTimerService().disableStats();
        }

        setVariableValue("internal_clock", internalClock);

        logger.debug("set internal clock to: " + internalClock);
    }

    @Override
    public long getCurrentTimeInMillis() {

        return this.serviceProvider.getEPRuntime().getCurrentTime();
    }

    @Override
    public Date getCurrentTime() {

        return new Date(this.serviceProvider.getEPRuntime().getCurrentTime());
    }

    @Override
    public void initCoordination() {

        this.coordinator = new AdapterCoordinatorImpl(this.serviceProvider, true, true);

        ((AdapterCoordinatorImpl) this.coordinator).setSender(new CustomSender());
    }

    @Override
    public void coordinate(CoordinatedAdapter inputAdapter) {

        inputAdapter.setEPService(this.serviceProvider);

        this.coordinator.coordinate(inputAdapter);
    }

    @Override
    public void startCoordination() {

        this.coordinator.start();
    }

    @Override
    public void setVariableValue(String variableName, Object value) {

        variableName = variableName.replace(".", "_");
        EPRuntime runtime = this.serviceProvider.getEPRuntime();
        if (runtime.getVariableValueAll().containsKey(variableName)) {
            runtime.setVariableValue(variableName, value);
            logger.debug("set variable " + variableName + " to value " + value);
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setVariableValueFromString(String variableName, String value) {

        variableName = variableName.replace(".", "_");
        EPRuntime runtime = this.serviceProvider.getEPRuntime();
        if (runtime.getVariableValueAll().containsKey(variableName)) {
            Class clazz = runtime.getVariableValue(variableName).getClass();

            Object castedObj = null;
            if (clazz.isEnum()) {
                castedObj = Enum.valueOf(clazz, value);
            } else {
                castedObj = JavaClassHelper.parse(clazz, value);
            }
            runtime.setVariableValue(variableName, castedObj);
            logger.debug("set variable " + variableName + " to value " + value);
        }
    }

    @Override
    public Object getVariableValue(String variableName) {

        variableName = variableName.replace(".", "_");
        EPRuntime runtime = this.serviceProvider.getEPRuntime();
        return runtime.getVariableValue(variableName);
    }

    @Override
    public void addTradeCallback(Collection<Order> orders, TradeCallback callback) {

        // get the securityIds sorted asscending and check that all orders are from the same strategy
        final Order firstOrder = CollectionUtil.getFirstElement(orders);
        Set<Integer> sortedSecurityIds = new TreeSet<Integer>(CollectionUtils.collect(orders, new Transformer<Order, Integer>() {

            @Override
            public Integer transform(Order order) {
                if (!order.getStrategy().equals(firstOrder.getStrategy())) {
                    throw new IllegalArgumentException("cannot addTradeCallback for orders of different strategies");
                }
                return order.getSecurity().getId();
            }
        }));

        if (sortedSecurityIds.size() < orders.size()) {
            throw new IllegalArgumentException("cannot addTradeCallback for multiple orders on the same security");
        }

        // get the statement alias based on all security ids
        String alias = "ON_TRADE_COMPLETED_" + StringUtils.join(sortedSecurityIds, "_") + "_" + firstOrder.getStrategy().getName();

        if (isDeployed(alias)) {

            logger.warn(alias + " is already deployed");
        } else {

            Object[] params = new Object[] { sortedSecurityIds.size(), sortedSecurityIds, firstOrder.getStrategy().getName(), firstOrder.isAlgoOrder() };
            deployStatement("prepared", "ON_TRADE_COMPLETED", alias, params, callback);
        }
    }

    @Override
    public void addFirstTickCallback(Collection<Security> securities, TickCallback callback) {

        // create a list of unique security ids
        Set<Integer> securityIds = new TreeSet<Integer>();
        securityIds.addAll(CollectionUtils.collect(securities, new Transformer<Security, Integer>() {
            @Override
            public Integer transform(Security security) {
                return security.getId();
            }
        }));

        String alias = "ON_FIRST_TICK_" + StringUtils.join(securityIds, "_");

        if (isDeployed(alias)) {

            logger.warn(alias + " is already deployed");
        } else {

            int[] securityIdsArray = ArrayUtils.toPrimitive(securityIds.toArray(new Integer[0]));
            deployStatement("prepared", "ON_FIRST_TICK", alias, new Object[] { securityIds.size(), securityIdsArray }, callback);
        }
    }

    @Override
    public void addOpenPositionCallback(int securityId, OpenPositionCallback callback) {

        String alias = "ON_OPEN_POSITION_" + securityId;

        if (isDeployed(alias)) {

            logger.warn(alias + " is already deployed");
        } else {

            deployStatement("prepared", "ON_OPEN_POSITION", alias, new Object[] { securityId }, callback);
        }
    }

    @Override
    public void addClosePositionCallback(int securityId, ClosePositionCallback callback) {

        String alias = "ON_CLOSE_POSITION_" + securityId;

        if (isDeployed(alias)) {

            logger.warn(alias + " is already deployed");
        } else {

            deployStatement("prepared", "ON_CLOSE_POSITION", alias, new Object[] { securityId }, callback);
        }
    }

    @Override
    public void addTimerCallback(Date dateTime, String name, TimerCallback callback) {

        String alias = "ON_TIMER_" + format.format(dateTime) + (name != null ? "_" + name : "");

        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTime);

        Object[] params = { alias, cal.get(Calendar.MINUTE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.SECOND), cal.get(Calendar.YEAR) };

        deployStatement("prepared", "ON_TIMER", alias, params, callback, true);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initVariables(Configuration configuration) {

        ConfigParams configParams = ConfigLocator.instance().getConfigParams();
        try {
            Map<String, ConfigurationVariable> variables = configuration.getVariables();
            for (Map.Entry<String, ConfigurationVariable> entry : variables.entrySet()) {
                String variableName = entry.getKey().replace("_", ".");
                String value = configParams.getString(variableName);
                if (value != null) {
                    Class clazz = Class.forName(entry.getValue().getType());
                    Object castedObj = null;
                    if (clazz.isEnum()) {
                        castedObj = Enum.valueOf(clazz, value);
                    } else if (clazz == BigDecimal.class) {
                        castedObj = new BigDecimal(value);
                    } else {
                        castedObj = JavaClassHelper.parse(clazz, value);
                    }
                    entry.getValue().setInitializationValue(castedObj);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private EPStatement startStatement(ModuleItem moduleItem, EPServiceProvider serviceProvider, String alias, Object[] params, Object callback) {

        // create the statement and set the prepared statement params if a prepared statement
        EPStatement statement;
        String expression = moduleItem.getExpression();
        EPAdministrator administrator = serviceProvider.getEPAdministrator();
        if (expression.contains("?")) {
            EPPreparedStatement prepared = administrator.prepareEPL(expression);
            for (int i = 0; i < params.length; i++) {
                prepared.setObject(i + 1, params[i]);
            }
            if (alias != null) {
                statement = administrator.create(prepared, alias);
            } else {
                statement = administrator.create(prepared);
            }
        } else {
            if (alias != null) {
                statement = administrator.createEPL(expression, alias);
            } else {
                statement = administrator.createEPL(expression);
            }
        }

        // process annotations
        Annotation[] annotations = statement.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Subscriber) {

                Subscriber subscriber = (Subscriber) annotation;
                try {
                    Object obj = getSubscriber(subscriber.className());
                    statement.setSubscriber(obj);
                } catch (Exception e) {
                    throw new RuntimeException("subscriber " + subscriber.className() + " could not be created for statement " + statement.getName(), e);
                }

            } else if (annotation instanceof Listeners) {

                Listeners listeners = (Listeners) annotation;
                for (String className : listeners.classNames()) {
                    try {
                        Class<?> cl = Class.forName(className);
                        Object obj = cl.newInstance();
                        if (obj instanceof StatementAwareUpdateListener) {
                            statement.addListener((StatementAwareUpdateListener) obj);
                        } else {
                            statement.addListener((UpdateListener) obj);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("listener " + className + " could not be created for statement " + statement.getName(), e);
                    }
                }
            }
        }

        // attach the callback if supplied
        // will override the Subscriber defined in Annotations
        // in live trading stop the statement before attaching (and restart afterwards)
        // to make sure that the subscriber receives the first event
        if (callback != null) {
            CommonConfig commonConfig = ConfigLocator.instance().getCommonConfig();
            if (commonConfig.isSimulation()) {
                statement.setSubscriber(callback);
            } else {
                statement.stop();
                statement.setSubscriber(callback);
                statement.start();
            }
        }
        return statement;
    }

    private Module getModule(String moduleName) {

        try {

            String fileName = "module-" + moduleName + ".epl";
            InputStream stream = getClass().getResourceAsStream("/" + fileName);
            if (stream == null) {
                throw new IllegalArgumentException(fileName + " does not exist");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            // process loads
            try {
                StringWriter buffer = new StringWriter();
                String strLine;
                while ((strLine = reader.readLine()) != null) {
                    if (!strLine.startsWith("load")) {
                        buffer.append(strLine);
                        buffer.append(newline);
                    } else {
                        String argument = StringUtils.substringAfter(strLine, "load").trim();
                        String moduleBaseName = argument.split("\\.")[0];
                        String statementName = argument.split("\\.")[1].split(";")[0];
                        Module module = EPLModuleUtil.readResource("module-" + moduleBaseName + ".epl");
                        for (ModuleItem item : module.getItems()) {
                            if (item.getExpression().contains("@Name('" + statementName + "')")) {
                                buffer.append(item.getExpression());
                                buffer.append(";");
                                buffer.append(newline);
                                break;
                            }
                        }
                    }
                }

                return EPLModuleUtil.parseInternal(buffer.toString(), fileName);

            } finally {
                reader.close();
            }

        } catch (ParseException e) {
            throw new RuntimeException(moduleName + " could not be deployed", e);
        } catch (IOException e) {
            throw new RuntimeException(moduleName + " could not be deployed", e);
        }
    }

    private boolean isEligibleStatement(EPServiceProvider serviceProvider, ModuleItem item, String statementName) {

        if (item.isCommentOnly()) {
            return false;
        }

        String expression = item.getExpression().replace("?", "1"); // replace ? to prevent error during compile
        EPStatementObjectModel objectModel = serviceProvider.getEPAdministrator().compileEPL(expression);
        List<AnnotationPart> annotationParts = objectModel.getAnnotations();
        List<AnnotationDesc> annotationDescs = StatementSpecMapper.mapAnnotations(annotationParts);

        EngineImportService engineImportService = ((EPServiceProviderSPI) serviceProvider).getEngineImportService();
        Annotation[] annotations = AnnotationUtil.compileAnnotations(annotationDescs, engineImportService, item.getExpression());

        CommonConfig commonConfig = ConfigLocator.instance().getCommonConfig();
        ConfigParams configParams = ConfigLocator.instance().getConfigParams();

        String excludeStatements = configParams.getString("misc.moduleDeployExcludeStatements");
        Set<String> moduleDeployExcludeStatements = new HashSet<String>();
        if (excludeStatements != null) {
            moduleDeployExcludeStatements.addAll(Arrays.asList(excludeStatements.split(",")));
        }

        for (Annotation annotation : annotations) {
            if (annotation instanceof Name) {

                Name name = (Name) annotation;
                if (statementName != null && !statementName.equals(name.value())) {
                    return false;
                } else if (moduleDeployExcludeStatements.contains(name.value())) {
                    return false;
                }

            } else if (annotation instanceof RunTimeOnly && commonConfig.isSimulation()) {

                return false;

            } else if (annotation instanceof SimulationOnly && !commonConfig.isSimulation()) {

                return false;

            } else if (annotation instanceof Condition) {

                Condition condition = (Condition) annotation;
                String key = condition.key();
                if (!configParams.getBoolean(key)) {
                    return false;
                }
            }
        }

        return true;
    }

    private String[] findStatementNames(final String statementNameRegex) {

        EPAdministrator administrator = this.serviceProvider.getEPAdministrator();

        // find the first statement that matches the given statementName regex
        return CollectionUtils.select(Arrays.asList(administrator.getStatementNames()), new Predicate<String>() {

            @Override
            public boolean evaluate(String statement) {
                return statement.matches(statementNameRegex);
            }
        }).toArray(new String[] {});
    }

    private Object getSubscriber(String fqdn) throws ClassNotFoundException {

        // try to see if the fqdn represents a class
        try {
            Class<?> cl = Class.forName(fqdn);
            return cl.newInstance();
        } catch (Exception e) {
            // do nothin
        }

        // otherwise the fqdn represents a method, in this case treate a subscriber
        return SubscriberCreator.createSubscriber(fqdn);
    }

}
