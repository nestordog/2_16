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
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.service.CalendarService;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.OptionService;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.PortfolioService;
import ch.algotrader.service.PositionService;

/**
 * Spring context based subscriber resolution algorithm.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class SpringServiceResolver implements ServiceResolver {

    private static final Pattern SUBSCRIBER_NOTATION = Pattern.compile("^([a-zA-Z]+[a-zA-Z0-9\\-_]*)(\\.|#)([a-zA-Z0-9_]+)$");
    private static final String SERVER_ENGINE = "SERVER";

    private final String strategyName;
    private final ConfigParams configParams;
    private final ApplicationContext applicationContext;

    public SpringServiceResolver(final String strategyName, final ConfigParams configParams, final ApplicationContext applicationContext) {
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
                return this.strategyName;
            } else {
                return this.configParams.getString(name, "null");
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
                throw new SubscriberResolutionException("Subscriber expression '" + subscriberExpression +
                        "' could not be resolved to a service method", ex);
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

    @Override
    public void resolveServices(Engine engine) {

        LookupService lookupService = this.applicationContext.getBean("lookupService", LookupService.class);
        PortfolioService portfolioService = this.applicationContext.getBean("portfolioService", PortfolioService.class);
        CalendarService calendarService = this.applicationContext.getBean("calendarService", CalendarService.class);
        OrderService orderService = this.applicationContext.getBean("orderService", OrderService.class);
        PositionService positionService = this.applicationContext.getBean("positionService", PositionService.class);
        MarketDataService marketDataService = this.applicationContext.getBean("marketDataService", MarketDataService.class);
        OptionService optionService = this.applicationContext.getBean("optionService", OptionService.class);

        engine.setVariableValue("lookupService", lookupService);
        engine.setVariableValue("portfolioService", portfolioService);
        engine.setVariableValue("calendarService", calendarService);
        engine.setVariableValue("orderService", orderService);
        engine.setVariableValue("positionService", positionService);
        engine.setVariableValue("marketDataService", marketDataService);
        engine.setVariableValue("optionService", optionService);
        Strategy strategy = lookupService.getStrategyByName(engine.getStrategyName());
        if (strategy != null) {
            engine.setVariableValue("engineStrategy", strategy);
        }

        if (engine.getStrategyName() == SERVER_ENGINE) {
            engine.setVariableValue("transactionService", this.applicationContext.getBean("transactionService"));
            engine.setVariableValue("forexService", this.applicationContext.getBean("forexService"));
            engine.setVariableValue("simpleOrderService", this.applicationContext.getBean("simpleOrderService"));
            engine.setVariableValue("algoOrderService", this.applicationContext.getBean("algoOrderService"));
        }
    }

}
