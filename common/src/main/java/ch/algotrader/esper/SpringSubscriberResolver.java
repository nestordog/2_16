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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.context.ApplicationContext;
import org.springframework.util.PropertyPlaceholderHelper;

import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EPSubscriberException;

import ch.algotrader.config.ConfigParams;

/**
 * Spring context based subscriber resolution algorithm.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class SpringSubscriberResolver implements SubscriberResolver {

    private static final Pattern SUBSCRIBER_NOTATION = Pattern.compile("^([a-zA-Z]+[a-zA-Z0-9\\-_]*)(\\.|#)([a-zA-Z0-9_]+)$");

    private final String strategyName;
    private final ConfigParams configParams;
    private final ApplicationContext applicationContext;

    public SpringSubscriberResolver(final String strategyName, final ConfigParams configParams, final ApplicationContext applicationContext) {
        Validate.notNull(strategyName, "StrategyName is null");
        Validate.notNull(configParams, "ConfigParams is null");
        Validate.notNull(applicationContext, "ApplicationContext is null");
        this.strategyName = adjust(strategyName);
        this.configParams = configParams;
        this.applicationContext = applicationContext;
    }

    String adjust(final String s) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (!Character.isUpperCase(ch)) {
                return s;
            }
        }
        return s.toLowerCase(Locale.ROOT);
    }

    @Override
    public void resolve(final EPStatement statement, final String subscriberExpression) {

        if (StringUtils.isBlank(subscriberExpression)) {
            throw new IllegalArgumentException("Subscriber is empty");
        }

        PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("${", "}", ",", false);
        String s = placeholderHelper.replacePlaceholders(subscriberExpression, name -> {
            if (name.equalsIgnoreCase("strategyName")) {
                return strategyName;
            } else {
                return configParams.getString(name, "null");
            }
        });

        final Matcher matcher = SUBSCRIBER_NOTATION.matcher(s);
        if (matcher.matches()) {
            // New subscriber notation
            final String beanName = matcher.group(1);
            final String beanMethod = matcher.group(3);
            Object bean = this.applicationContext.getBean(beanName);
            try {
                statement.setSubscriber(bean, beanMethod);
            } catch (EPSubscriberException ex) {
                throw new EPSubscriberException("Subscriber expression '" + subscriberExpression + "' could not be resolved to a service method");
            }
        } else {
            // Assuming to be a fully qualified class name otherwise
            try {
                Class<?> cl = Class.forName(s);
                statement.setSubscriber(cl.newInstance());
            } catch (Exception e) {
                // Old notation for backward compatibility
                String serviceName = StringUtils.substringBeforeLast(s, ".");
                if (serviceName.contains(".")) {
                    serviceName = StringUtils.remove(StringUtils.remove(StringUtils.uncapitalize(StringUtils.substringAfterLast(serviceName, ".")), "Base"), "Impl");
                }
                String beanMethod = StringUtils.substringAfterLast(s, ".");
                Object bean = this.applicationContext.getBean(serviceName);
                statement.setSubscriber(bean, beanMethod);
            }
        }
    }

}
