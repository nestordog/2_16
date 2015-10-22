/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.util.log4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.esper.EngineManager;

/**
* Log4J log interceptor that rewrites log entries when executing in simulation mode.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class Log4JRewriter implements ApplicationListener<ContextRefreshedEvent> {

    private final EngineManager engineManager;
    private final CommonConfig commonConfig;

    private final AtomicBoolean postProcessed;

    public Log4JRewriter(final EngineManager engineManager, final CommonConfig commonConfig) {
        this.engineManager = engineManager;
        this.commonConfig = commonConfig;
        this.postProcessed = new AtomicBoolean(false);
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {

        if (this.postProcessed.compareAndSet(false, true)) {

            if (this.commonConfig.isSimulation()) {

                interceptLogAppenders();
            }
        }
    }

    private void interceptLogAppenders() {

        LoggerContext coreLoggerContext = (LoggerContext) LogManager.getContext(false);

        Configuration configuration = coreLoggerContext.getConfiguration();
        Map<String, Appender> appenderMap = new HashMap<>(configuration.getAppenders());
        configuration.getAppenders().clear();

        RewritePolicy rewritePolicy = new EngineTimeRewritePolicy(this.engineManager);
        for (Map.Entry<String, Appender> entry: appenderMap.entrySet()) {
            EngineTimeRewritingAppender rewritingAppender = new EngineTimeRewritingAppender(entry.getValue(), rewritePolicy);
            configuration.addAppender(rewritingAppender);
            rewritingAppender.start();
        }

        Map<String, LoggerConfig> loggerConfigMap = configuration.getLoggers();
        for (Map.Entry<String, LoggerConfig> entry: loggerConfigMap.entrySet()) {
            LoggerConfig loggerConfig = entry.getValue();
            List<AppenderRef> appenderRefs = loggerConfig.getAppenderRefs();
            for (AppenderRef appenderRef: appenderRefs) {
                loggerConfig.removeAppender(appenderRef.getRef());
                Appender appender = configuration.getAppender(appenderRef.getRef());
                if (appender != null) {
                    loggerConfig.addAppender(appender, appenderRef.getLevel(), appenderRef.getFilter());
                }
            }
        }
    }

}
