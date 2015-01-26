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
package ch.algotrader.util.spring;

import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.rewrite.RewriteAppender;
import org.apache.log4j.rewrite.RewritePolicy;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManagerImpl;

/**
* Log4J log interceptor that rewrites log entries when executing in simulation mode.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/

public class Log4JRewriter implements ApplicationListener<ContextRefreshedEvent> {

    private final EngineManagerImpl engineManager;
    private final CommonConfig commonConfig;

    private final AtomicBoolean postProcessed;

    public Log4JRewriter(final EngineManagerImpl engineManager, final CommonConfig commonConfig) {
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

        Logger rootLogger = RootLogger.getRootLogger();

        RewriteAppender rewriteAppender = new RewriteAppender();
        Enumeration it = rootLogger.getAllAppenders();
        while (it.hasMoreElements()) {
            Appender appender = (Appender) it.nextElement();
            rootLogger.removeAppender(appender);
            rewriteAppender.addAppender(appender);
        }

        rootLogger.addAppender(rewriteAppender);

        rewriteAppender.setRewritePolicy(new EngineTimePolicy());
    }

    class EngineTimePolicy implements RewritePolicy {

        @Override
        public LoggingEvent rewrite(final LoggingEvent source) {

            // find the Engine with the earliest time
            long latestTime = Long.MAX_VALUE;
            for (Engine engine : engineManager.getEngines()) {

                if (!engine.isDestroyed()) {
                    long engineTime = engine.getCurrentTimeInMillis();
                    if (engineTime < latestTime) {
                        latestTime = engineTime;
                    }
                }
            }
            return new LoggingEvent(
                    source.getFQNOfLoggerClass(),
                    source.getLogger(),
                    latestTime < Long.MAX_VALUE ? latestTime : source.getTimeStamp(),
                    source.getLevel(),
                    source.getMessage(),
                    source.getThreadName(),
                    source.getThrowableInformation(),
                    source.getNDC(),
                    source.getLocationInformation(),
                    source.getProperties());
        }
    }

}
