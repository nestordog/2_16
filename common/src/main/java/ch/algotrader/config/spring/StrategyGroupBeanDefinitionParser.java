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
package ch.algotrader.config.spring;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.algotrader.service.groups.StrategyGroup;

/**
 * Algotrader strategy group definition parser.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public final class StrategyGroupBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {

        String id = element.getAttribute("id");
        if (!StringUtils.hasText(id)) {
            parserContext.getReaderContext().error("Id is required for element '"
                    + parserContext.getDelegate().getLocalName(element) + "'", element);
        }

        ManagedMap<RuntimeBeanReference, String> paramMap = new ManagedMap<>();
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);

            if ("strategyItem".equalsIgnoreCase(node.getLocalName()) && node instanceof Element) {

                Element item = (Element) node;
                String name = item.getAttribute("name");
                if (!StringUtils.hasText(name)) {
                    parserContext.getReaderContext().error("Name is required for element '"
                            + parserContext.getDelegate().getLocalName(item) + "'", element);
                }
                String weight = item.getAttribute("weight");
                if (!StringUtils.hasText(weight)) {
                    parserContext.getReaderContext().error("Weight is required for element '"
                            + parserContext.getDelegate().getLocalName(item) + "'", element);
                }
                paramMap.put(new RuntimeBeanReference(name + "Service"), weight);

                BeanDefinitionBuilder builder1 = BeanDefinitionBuilder.childBeanDefinition(name + "ConfigParamsTemplate");
                BeanDefinitionHolder holder1 = new BeanDefinitionHolder(builder1.getBeanDefinition(), name + "ConfigParams");
                registerBeanDefinition(holder1, parserContext.getRegistry());

                BeanDefinitionBuilder builder2 = BeanDefinitionBuilder.childBeanDefinition(name + "ConfigTemplate");
                BeanDefinitionHolder holder2 = new BeanDefinitionHolder(builder2.getBeanDefinition(), name + "Config");
                registerBeanDefinition(holder2, parserContext.getRegistry());

                BeanDefinitionBuilder builder3 = BeanDefinitionBuilder.childBeanDefinition(name + "EngineTemplate");
                BeanDefinitionHolder holder3 = new BeanDefinitionHolder(builder3.getBeanDefinition(), name + "Engine");
                registerBeanDefinition(holder3, parserContext.getRegistry());

                BeanDefinitionBuilder builder4 = BeanDefinitionBuilder.childBeanDefinition(name + "ServiceTemplate");
                BeanDefinitionHolder holder4 = new BeanDefinitionHolder(builder4.getBeanDefinition(), name + "Service");
                registerBeanDefinition(holder4, parserContext.getRegistry());
            }
        }
        if (paramMap.isEmpty()) {
            parserContext.getReaderContext().error("No strategy items found for element '"
                    + parserContext.getDelegate().getLocalName(element) + "'", element);
        }

        BeanDefinitionBuilder builder5 = BeanDefinitionBuilder.rootBeanDefinition(StrategyGroup.class);
        builder5.addConstructorArgValue(paramMap);
        BeanDefinitionHolder holder5 = new BeanDefinitionHolder(builder5.getBeanDefinition(), id);
        registerBeanDefinition(holder5, parserContext.getRegistry());

        return null;
    }

}
