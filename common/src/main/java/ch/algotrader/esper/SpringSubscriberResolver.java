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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.context.ApplicationContext;

import com.espertech.esper.client.EPStatement;

/**
 * Spring context based subscriber resolution algorithm.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class SpringSubscriberResolver implements SubscriberResolver {

    private static final Pattern SUBSCRIBER_NOTATION = Pattern.compile("^([a-zA-Z]+[a-zA-Z0-9\\-_]*)(\\.|#)([a-zA-Z0-9_]+)$");

    private final ApplicationContext applicationContext;

    public SpringSubscriberResolver(final ApplicationContext applicationContext) {
        Validate.notNull(applicationContext, "ApplicationContext is null");
        this.applicationContext = applicationContext;
    }

    @Override
    public void resolve(final EPStatement statement, final String subscriberExpression) {

        if (StringUtils.isBlank(subscriberExpression)) {
            throw new IllegalArgumentException("Subscriber is empty");
        }
        final Matcher matcher = SUBSCRIBER_NOTATION.matcher(subscriberExpression);
        if (matcher.matches()) {
            // New subscriber notation
            final String beanName = matcher.group(1);
            final String beanMethod = matcher.group(3);
            Object bean = this.applicationContext.getBean(beanName);
            statement.setSubscriber(bean, beanMethod);
        } else {
            // Assuming to be a fully qualified class name otherwise
            String fqdn = subscriberExpression;
            try {
                Class<?> cl = Class.forName(fqdn);
                statement.setSubscriber(cl.newInstance());
            } catch (Exception e) {
                // Old notation for backward compatibility
                String serviceName = StringUtils.substringBeforeLast(fqdn, ".");
                if (serviceName.contains(".")) {
                    serviceName = StringUtils.remove(StringUtils.remove(StringUtils.uncapitalize(StringUtils.substringAfterLast(serviceName, ".")), "Base"), "Impl");
                }
                String beanMethod = StringUtils.substringAfterLast(fqdn, ".");
                Object bean = this.applicationContext.getBean(serviceName);
                statement.setSubscriber(bean, beanMethod);
            }
        }
    }

}
