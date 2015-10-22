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
package ch.algotrader.config.spring;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.childBeanDefinition;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.algotrader.service.groups.StrategyContextInitializer;
import ch.algotrader.service.groups.StrategyGroup;

/**
 * AlgoTrader strategy group definition parser.
 * <p>
 * Example input:
<pre>{@code
    <at:strategyGroup id="simple">
        <at:strategyItem name="box-narrow" weight="0.2"/>
        <at:strategyItem name="box-wide" weight="0.8"/>
    </at:strategyGroup>
}</pre>
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public final class StrategyGroupBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {

        String id = getRequiredAttribute(element, parserContext, "id");
        // iterate strategyItem nodes
        final NodeList childNodes = element.getChildNodes();
        final Map<String, Double> nameToWeight = new LinkedHashMap<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node node = childNodes.item(i);

            if ("strategyItem".equalsIgnoreCase(node.getLocalName()) && node instanceof Element) {
                final Element item = (Element) node;

                //required attributes
                final String name = getRequiredAttribute(item, parserContext, "name");
                final double weight = getWeightAttribute(item, parserContext, "weight");
                nameToWeight.put(name, weight);

                //remember beans associated with this item
                final ManagedList<RuntimeBeanReference> beans = new ManagedList<RuntimeBeanReference>();
                beans.add(new RuntimeBeanReference(name + "ConfigParams"));
                beans.add(new RuntimeBeanReference(name + "Config"));
                beans.add(new RuntimeBeanReference(name + "Engine"));
                beans.add(new RuntimeBeanReference(name + "Service"));

                //register bean: Engine
                registerBeanDefinition(name + "Engine", parserContext, childBeanDefinition(name + "EngineTemplate")
                        .addPropertyValue("strategyName", name));
                //register bean: ConfigParams
                registerBeanDefinition(name + "ConfigParams", parserContext, childBeanDefinition(name + "ConfigParamsTemplate"));
                //register bean: Config
                registerBeanDefinition(name + "Config", parserContext, childBeanDefinition(name + "ConfigTemplate"));
                //register bean: Service
                registerBeanDefinition(name + "Service", parserContext, childBeanDefinition(name + "ServiceTemplate"));

                //register bean: StrategyContextInitializer for services implementing StrategyContextAware
                registerBeanDefinition(name + "StrategyContextInitializer", parserContext, rootBeanDefinition(StrategyContextInitializer.class)
                        .addConstructorArgValue(name + "Service")       //serviceBeanName
                        .addConstructorArgReference(name + "Service")   //serviceToInitialize
                        .addConstructorArgValue(name)                   //passed to StrategyContextAware.setStrategyName(String)
                        .addConstructorArgValue(weight)                 //passed to StrategyContextAware.setWeight(double)
                        .addConstructorArgReference(name + "Engine")    //passed to StrategyContextAware.setEngine(Engine)
                        .addConstructorArgReference(name + "Config")    //passed to StrategyContextAware.setConfig(Object)
                        .setInitMethodName("initStrategyContext"));
            }
        }
        if (nameToWeight.isEmpty()) {
            parserContext.getReaderContext().error("No strategy items found for element '"
                    + parserContext.getDelegate().getLocalName(element) + "'", element);
        }

        registerBeanDefinition(id, parserContext, rootBeanDefinition(StrategyGroup.class)
                .addConstructorArgValue(nameToWeight));

        return null;
    }

    private double getWeightAttribute(Element element, ParserContext parserContext, String attributeName) {
        final String value = getRequiredAttribute(element, parserContext, attributeName);
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            parserContext.getReaderContext().error(attributeName + " with value <" + value
                    + "> cannot be parsed into a double value for element '"
                    + parserContext.getDelegate().getLocalName(element) + "', e=" + e, element, e);
            return Double.NaN;
        }
    }

    private void registerBeanDefinition(String beanName, ParserContext parserContext, BeanDefinitionBuilder beanDefinitionBuilder) {
        final BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinitionBuilder.getBeanDefinition(), beanName);
        registerBeanDefinition(holder, parserContext.getRegistry());
    }

    private static String getRequiredAttribute(Element element, ParserContext parserContext, String attributeName) {
        final String value = element.getAttribute(attributeName);
        if (!StringUtils.hasText(value)) {
            parserContext.getReaderContext().error(attributeName + " is required for element '"
                    + parserContext.getDelegate().getLocalName(element) + "'", element);
        }
        return value;
    }

}
