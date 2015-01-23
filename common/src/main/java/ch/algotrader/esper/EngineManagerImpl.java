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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.vo.GenericEventVO;
import ch.algotrader.vo.StatementMetricVO;

/**
* {@link ch.algotrader.esper.EngineManager} implementation.
*
* @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
*
* @version $Revision$ $Date$
*/
public class EngineManagerImpl implements EngineManager {

    private static final Logger LOGGER = Logger.getLogger(EngineManagerImpl.class);

    private static final String SERVER_ENGINE = "SERVER";

    private final CommonConfig commonConfig;
    private final Map<String, Engine> engineMap;
    private final Map<String, JmsTemplate> templateMap;

    public EngineManagerImpl(final CommonConfig commonConfig, final Map<String, Engine> engineMap, final Map<String, JmsTemplate> templateMap) {
        this.commonConfig = commonConfig;
        this.engineMap = new ConcurrentHashMap<>(engineMap);
        this.templateMap = new ConcurrentHashMap<>(templateMap);
    }

    @Override
    public Date getCurrentEPTime() {
        Engine engine = getEngine(this.commonConfig.getStartedStrategyName());
        return !engine.isInternalClock() ? engine.getCurrentTime() : new Date();
    }

    @Override
    public boolean hasEngine(final String engineName) {

        Validate.notEmpty(engineName, "Engine name is empty");
        return this.engineMap.containsKey(engineName);
    }

    @Override
    public Engine getEngine(final String engineName) {

        Validate.notEmpty(engineName, "Engine name is empty");
        Engine engine = this.engineMap.get(engineName);
        if (engine == null) {
            throw new IllegalStateException("Unknown engine: " + engineName);
        }
        return engine;
    }

    @Override
    public Engine getServerEngine() {

        return getEngine(SERVER_ENGINE);
    }

    private JmsTemplate getTemplate(final String templateName) {

        Validate.notEmpty(templateName, "JMS template name is empty");
        JmsTemplate jmsTemplate = this.templateMap.get(templateName);
        if (jmsTemplate == null) {
            throw new IllegalStateException("JMS template: " + templateName);
        }
        return jmsTemplate;
    }

    @Override
    public void destroyEngine(final String engineName) {

        Engine engine = this.engineMap.get(engineName);
        if (engine != null) {
            engine.destroy();
        }
    }

    public Collection<Engine> getEngines() {
        return this.engineMap.values();
    }

    @Override
    public void sendEvent(final String engineName, final Object obj) {

        if (this.commonConfig.isSimulation() || this.commonConfig.isEmbedded()) {

            if (hasEngine(engineName)) {
                getEngine(engineName).sendEvent(obj);
            }

        } else {

            // check if it is the localStrategy
            if (this.commonConfig.getStartedStrategyName().equals(engineName)) {

                getEngine(engineName).sendEvent(obj);

            } else {

                // sent to the strategy queue
                getTemplate("strategyTemplate").convertAndSend(engineName + ".QUEUE", obj);
            }
        }
    }

    @Override
    public void sendMarketDataEvent(final MarketDataEvent marketDataEvent) {

        if (this.commonConfig.isSimulation() || this.commonConfig.isEmbedded()) {
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

    @Override
    public void sendGenericEvent(final GenericEventVO event) {

        if (this.commonConfig.isSimulation() || this.commonConfig.isEmbedded()) {
            for (Map.Entry<String, Engine> entry: this.engineMap.entrySet()) {
                Engine engine = entry.getValue();
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

    @Override
    public void sendEventToAllEngines(Object obj) {

        for (Map.Entry<String, Engine> entry: this.engineMap.entrySet()) {
            Engine engine = entry.getValue();
            engine.sendEvent(obj);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void logStatementMetrics() {

        for (Map.Entry<String, Engine> entry: this.engineMap.entrySet()) {
            Engine engine = entry.getValue();

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
                    LOGGER.info(metric.getEngineURI() + "." + metric.getStatementName() + ": " + metric.getWallTime() + " millis " + metric.getNumInput() + " events");
                }
            }
        }
    }

    @Override
    public void resetStatementMetrics() {

        for (Map.Entry<String, Engine> entry: this.engineMap.entrySet()) {
            Engine engine = entry.getValue();
            engine.restartStatement("METRICS");
        }
    }

}
