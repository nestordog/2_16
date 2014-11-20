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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import ch.algotrader.ServiceLocator;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.util.MyLogger;
import ch.algotrader.vo.GenericEventVO;
import ch.algotrader.vo.StatementMetricVO;

/**
 * Singleton class to locate {@link Engine Engines}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EngineLocator {

    private static final Logger logger = MyLogger.getLogger(EngineLocator.class.getName());
    private static final EngineLocator instance = new EngineLocator();
    private static final CommonConfig CONFIG = ConfigLocator.instance().getCommonConfig();

    private Map<String, Engine> engines = new HashMap<String, Engine>();

    private Map<String, JmsTemplate> templates = new HashMap<String, JmsTemplate>();

    public static final EngineLocator instance() {

        return instance;
    }

    public Engine initEngine(String engineName) {

        Engine engine = new EngineImpl(engineName);
        this.engines.put(engineName, engine);
        return engine;
    }

    public Engine initServerEngine() {

        return initEngine(StrategyImpl.SERVER);
    }

    public Collection<Engine> getEngines() {

        return this.engines.values();
    }

    public Set<String> getEngineNames() {

        return this.engines.keySet();
    }

    public Engine getEngine(String engineName) {

        return this.engines.get(engineName);
    }

    public Engine getServerEngine() {

        return getEngine(StrategyImpl.SERVER);
    }

    public boolean hasEngine(String engineName) {

        return this.engines.containsKey(engineName);
    }

    public boolean hasServerEngine() {

        return hasEngine(StrategyImpl.SERVER);
    }

    public void destroyEngine(String engineName) {

        if (this.engines.containsKey(engineName)) {
            this.engines.remove(engineName).destroy();
        }
    }

    public void setEngine(String engineName, Engine engine) {

        this.engines.put(engineName, engine);
    }

    /**
     * Sends an Event to the corresponding Esper Engine.
     * Use this method for situations where the corresponding Engine might be running localy ore remote,
     * otherwise use {@link Engine#sendEvent}.
     *
     */
    public void sendEvent(String engineName, Object obj) {

        if (CONFIG.isSimulation() || CONFIG.isEmbedded()) {

            if (hasEngine(engineName)) {
                getEngine(engineName).sendEvent(obj);
            }

        } else {

            // check if it is the localStrategy
            if (CONFIG.getStartedStrategyName().equals(engineName)) {

                getEngine(engineName).sendEvent(obj);

            } else {

                // sent to the strategy queue
                getTemplate("strategyTemplate").convertAndSend(engineName + ".QUEUE", obj);
            }
        }
    }

    /**
     * Sends a MarketDataEvent into the corresponding Esper Engine.
     * In Live-Trading the {@code marketDataTemplate} will be used.
     */
    public void sendMarketDataEvent(final MarketDataEvent marketDataEvent) {

        if (CONFIG.isSimulation() || CONFIG.isEmbedded()) {
            for (Subscription subscription : marketDataEvent.getSecurity().getSubscriptions()) {
                if (!subscription.getStrategyInitialized().getName().equals(StrategyImpl.SERVER)) {
                    String strategyName = subscription.getStrategy().getName();
                    if (hasEngine(strategyName)) {
                        getEngine(strategyName).sendEvent(marketDataEvent);
                    }
                }
            }

        } else {

            // send using the jms template
            getTemplate("marketDataTemplate").convertAndSend(marketDataEvent, new MessagePostProcessor() {

                @Override
                public Message postProcessMessage(Message message) throws JMSException {

                    // add securityId Property
                    message.setIntProperty("securityId", marketDataEvent.getSecurity().getId());
                    return message;
                }
            });
        }
    }

    /**
     * Sends a MarketDataEvent into the corresponding Esper Engine.
     * In Live-Trading the {@code genericTemplate} will be used.
     */
    public void sendGenericEvent(final GenericEventVO event) {

        if (CONFIG.isSimulation() || CONFIG.isEmbedded()) {
            for (Engine engine : this.engines.values()) {
                String engineName = engine.getName();
                if (engineName.equals(event.getStrategyName())) {
                    engine.sendEvent(event);
                }
            }

        } else {

            // send using the jms template
            getTemplate("genericTemplate").convertAndSend(event, new MessagePostProcessor() {

                @Override
                public Message postProcessMessage(Message message) throws JMSException {

                    // add class Property
                    message.setStringProperty("clazz", event.getClass().getName());
                    return message;
                }
            });
        }
    }

    /**
     * Sends an event to all local Esper Engines
     */
    public void sendEventToAllEngines(Object obj) {

        for (Engine engine : this.engines.values()) {
            engine.sendEvent(obj);
        }
    }

    /**
     * Prints all statement metrics.
     */
    @SuppressWarnings("unchecked")
    public void logStatementMetrics() {

        for (Engine engine : this.engines.values()) {

            if (engine.isDeployed("METRICS")) {

                List<StatementMetricVO> metrics = engine.getAllEvents("METRICS");

                // consolidate ON_TRADE_COMPLETED and ON_FIRST_TICK
                for (final String statementName : new String[] { "ON_TRADE_COMPLETED", "ON_FIRST_TICK" }) {

                    // select metrics where the statementName startsWith
                    Collection<StatementMetricVO> selectedMetrics = CollectionUtils.select(metrics, new Predicate<StatementMetricVO>() {
                        @Override
                        public boolean evaluate(StatementMetricVO metric) {
                            return metric.getStatementName() != null && metric.getStatementName().startsWith(statementName);
                        }
                    });

                    // add cpuTime, wallTime and numInput
                    if (selectedMetrics.size() > 0) {

                        long cpuTime = 0;
                        long wallTime = 0;
                        long numInput = 0;
                        for (StatementMetricVO metric : selectedMetrics) {

                            cpuTime += metric.getCpuTime();
                            wallTime += metric.getWallTime();
                            numInput += metric.getNumInput();

                            // remove the original metric
                            metrics.remove(metric);
                        }
                        ;

                        // add a consolidated metric
                        metrics.add(new StatementMetricVO(engine.getName(), statementName, cpuTime, wallTime, numInput));
                    }
                }

                for (StatementMetricVO metric : metrics) {
                    logger.info(metric.getEngineURI() + "." + metric.getStatementName() + ": " + metric.getWallTime() + " millis " + metric.getNumInput() + " events");
                }
            }
        }
    }

    /**
     * Resets all statement metrics.
     */
    public void resetStatementMetrics() {

        for (Engine engine : this.engines.values()) {
            engine.restartStatement("METRICS");
        }
    }

    /**
     * manual lookup of templates since they are only available if applicationContext-jms.xml is active
     */
    private JmsTemplate getTemplate(String name) {

        if (!this.templates.containsKey(name)) {
            this.templates.put(name, ServiceLocator.instance().getService(name, JmsTemplate.class));
        }

        return this.templates.get(name);
    }
}
