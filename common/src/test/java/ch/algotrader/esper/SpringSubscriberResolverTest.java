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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import com.espertech.esper.client.EPStatement;

@RunWith(MockitoJUnitRunner.class)
public class SpringSubscriberResolverTest {

    @Mock
    private EPStatement statement;
    @Mock
    private ApplicationContext applicationContext;

    @Test
    public void testBasicSubscriberResolution() {

        SpringSubscriberResolver impl = new SpringSubscriberResolver(applicationContext);

        Object bean = new Object();
        Mockito.when(applicationContext.getBean("beanName")).thenReturn(bean);

        impl.resolve(statement, "beanName#beanMethod");

        Mockito.verify(statement).setSubscriber(bean, "beanMethod");
    }

    @Test
    public void testBasicSubscriberResolutionOldNotation() {

        SpringSubscriberResolver impl = new SpringSubscriberResolver(applicationContext);

        Object bean = new Object();
        Mockito.when(applicationContext.getBean("beanName")).thenReturn(bean);

        impl.resolve(statement, "beanName.beanMethod");

        Mockito.verify(statement).setSubscriber(bean, "beanMethod");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBasicSubscriberResolutionEmptyExpression() {

        SpringSubscriberResolver impl = new SpringSubscriberResolver(applicationContext);

        impl.resolve(statement, "  ");
    }

    @Test
    public void testBasicSubscriberResolutionClassName() {

        SpringSubscriberResolver impl = new SpringSubscriberResolver(applicationContext);

        impl.resolve(statement, "java.lang.StringBuilder");

        Mockito.verify(statement).setSubscriber(Mockito.any(StringBuilder.class));
    }

    @Test
    public void testBasicSubscriberResolutionServiceName() {

        SpringSubscriberResolver impl = new SpringSubscriberResolver(applicationContext);

        Object bean = new Object();
        Mockito.when(applicationContext.getBean("beanName")).thenReturn(bean);

        impl.resolve(statement, "this.that.BeanName.beanMethod");

        Mockito.verify(statement).setSubscriber(bean, "beanMethod");
    }

}
