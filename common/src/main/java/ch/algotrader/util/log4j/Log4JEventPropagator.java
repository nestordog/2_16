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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.enumeration.OperationMode;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.event.listener.LifecycleEventListener;
import ch.algotrader.vo.LifecycleEventVO;

/**
* Log4J log filter that propagates log events to internal message consumers.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*/
public class Log4JEventPropagator implements LifecycleEventListener, ApplicationListener<ContextRefreshedEvent> {

    private final InternalLogAppender internalLogAppender;
    private final CommonConfig commonConfig;
    private final AtomicBoolean postProcessed;

    public Log4JEventPropagator(final EventDispatcher eventDispatcher, final CommonConfig commonConfig) {
        this.internalLogAppender = new InternalLogAppender(Level.ERROR, Level.WARN, eventDispatcher);
        this.commonConfig = commonConfig;
        this.postProcessed = new AtomicBoolean(false);
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {

        if (this.postProcessed.compareAndSet(false, true)) {

            if (!this.commonConfig.isSimulation()) {

                addInternalAppender();
            }
        }
    }

    @Override
    public void onChange(final LifecycleEventVO event) {

        if (event.getOperationMode() == OperationMode.REAL_TIME) {

            switch (event.getPhase()) {
                case START:
                    this.internalLogAppender.enable();
                    break;
                case EXIT:
                    this.internalLogAppender.disable();
                    break;
            }
        }
    }

    private void addInternalAppender() {

        LoggerContext coreLoggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = coreLoggerContext.getConfiguration();
        configuration.getRootLogger().addAppender(internalLogAppender, Level.INFO, null);
    }

}
