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
package ch.algotrader.event;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import ch.algotrader.wiring.client.remote.JMSMessagingWiring;
import ch.algotrader.wiring.common.CommonConfigWiring;
import ch.algotrader.wiring.common.EngineManagerWiring;
import ch.algotrader.wiring.common.EventDispatchWiring;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class StrategyEventDumper {

    public static void main(String... args) throws Exception {

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(CommonConfigWiring.class, EventDispatchWiring.class, EngineManagerWiring.class, JMSMessagingWiring.class);
        applicationContext.getDefaultListableBeanFactory().registerSingleton("strategyQueue", new ActiveMQQueue("BOX.QUEUE"));
        applicationContext.refresh();

        Runtime.getRuntime().addShutdownHook(new Thread(applicationContext::close));

        DefaultMessageListenerContainer marketDataMessageListenerContainer = applicationContext.getBean("marketDataMessageListenerContainer",
                DefaultMessageListenerContainer.class);
        marketDataMessageListenerContainer.setMessageSelector("true");
        DefaultMessageListenerContainer genericMessageListenerContainer = applicationContext.getBean("genericMessageListenerContainer",
                DefaultMessageListenerContainer.class);
        genericMessageListenerContainer.setMessageSelector("true");

        EventListenerRegistry eventListenerRegistry = applicationContext.getBean(EventListenerRegistry.class);
        eventListenerRegistry.register(event -> System.out.println(event.getClass() + ": " + event), Object.class);

        System.out.println("Dumping events");

        Thread.sleep(Long.MAX_VALUE);
    }

}
