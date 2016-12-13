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
package ch.algotrader.esper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import com.espertech.esper.client.EPStatement;

import ch.algotrader.config.ConfigParams;
import ch.algotrader.config.NoopConfigProvider;

@RunWith(MockitoJUnitRunner.class)
public class SpringServiceResolverTest {

    @Mock
    private EPStatement statement;
    @Mock
    private ApplicationContext applicationContext;

    private ConfigParams configParams;
    private SpringServiceResolver impl;

    @Before
    public void setup() {
        configParams = new ConfigParams(new NoopConfigProvider());
        impl = new SpringServiceResolver("myStrategy", configParams, applicationContext);
    }

    @Test
    public void testBasicSubscriberResolution() {

        Object bean = new Object();
        Mockito.when(applicationContext.getBean("beanName")).thenReturn(bean);

        impl.resolve(statement, "beanName#beanMethod");

        Mockito.verify(statement).setSubscriber(bean, "beanMethod");
    }

    @Test
    public void testBasicSubscriberResolutionWithPlaceholderExpansion() {

        Object bean = new Object();
        Mockito.when(applicationContext.getBean("myStrategyService")).thenReturn(bean);

        impl.resolve(statement, "${strategyName}Service#doStuff");

        Mockito.verify(statement).setSubscriber(bean, "doStuff");
    }

    @Test
    public void testBasicSubscriberResolutionOldNotation() {

        Object bean = new Object();
        Mockito.when(applicationContext.getBean("beanName")).thenReturn(bean);

        impl.resolve(statement, "beanName.beanMethod");

        Mockito.verify(statement).setSubscriber(bean, "beanMethod");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBasicSubscriberResolutionEmptyExpression() {

        impl.resolve(statement, "  ");
    }

    @Test
    public void testBasicSubscriberResolutionClassName() {

        impl.resolve(statement, "java.lang.StringBuilder");

        Mockito.verify(statement).setSubscriber(Mockito.any(StringBuilder.class));
    }

    @Test
    public void testBasicSubscriberResolutionServiceName() {

        Object bean = new Object();
        Mockito.when(applicationContext.getBean("beanName")).thenReturn(bean);

        impl.resolve(statement, "this.that.BeanName.beanMethod");

        Mockito.verify(statement).setSubscriber(bean, "beanMethod");
    }

}
