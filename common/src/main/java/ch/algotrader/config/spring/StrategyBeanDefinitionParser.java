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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Algotrader strategy definition parser.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public final class StrategyBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {

        BeanDefinitionBuilder builder1 = BeanDefinitionBuilder.rootBeanDefinition(CustomConfigParamsFactoryBean.class);

        String name = element.getAttribute("name");
        if (!StringUtils.hasText(name)) {
            parserContext.getReaderContext().error("Name is required for element '"
                    + parserContext.getDelegate().getLocalName(element) + "'", element);
        }

        builder1.addPropertyReference("global", "configParams");

        List<String> resourceList = new ArrayList<>();
        String config = element.getAttribute("config");
        if (StringUtils.hasText(config)) {
            String[] resources = StringUtils.tokenizeToStringArray(config, ",", true, true);
            if (resources != null) {
                for (String resource: resources) {
                    if (resource.contains(":")) {
                        resourceList.add(resource);
                    } else {
                        resourceList.add("classpath:/META-INF/" + resource);
                    }
                }
            }
        } else {
            resourceList.add("classpath:/META-INF/" + name + ".properties");
        }
        builder1.addPropertyValue("resources", resourceList);
        builder1.setAbstract(true);

        BeanDefinitionHolder holder1 = new BeanDefinitionHolder(builder1.getBeanDefinition(), name + "ConfigParamsTemplate");
        registerBeanDefinition(holder1, parserContext.getRegistry());

        BeanDefinitionBuilder builder2 = BeanDefinitionBuilder.rootBeanDefinition(CustomConfigBeanFactoryBean.class);
        String configClass = element.getAttribute("configClass");
        if (!StringUtils.hasText(configClass)) {
            parserContext.getReaderContext().error("ConfigClass is required for element '"
                    + parserContext.getDelegate().getLocalName(element) + "'", element);
        }
        builder2.addPropertyValue("beanClass", configClass);
        builder2.addPropertyReference("configParams", name + "ConfigParams");
        builder2.setAbstract(true);

        BeanDefinitionHolder holder2 = new BeanDefinitionHolder(builder2.getBeanDefinition(), name + "ConfigTemplate");
        registerBeanDefinition(holder2, parserContext.getRegistry());

        String engineTemplate = element.getAttribute("engineTemplate");
        if (!StringUtils.hasText(engineTemplate)) {
            parserContext.getReaderContext().error("EngineTemplate is required for element '"
                    + parserContext.getDelegate().getLocalName(element) + "'", element);
        }

        BeanDefinitionBuilder builder3 = BeanDefinitionBuilder.childBeanDefinition(engineTemplate);
        String engineName = element.getAttribute("engineName");
        if (!StringUtils.hasText(engineName)) {
            engineName = name.toUpperCase(Locale.ROOT);
        }
        builder3.addPropertyValue("engineName", engineName);
        builder3.addPropertyReference("configParams", name + "ConfigParams");
        builder3.setAbstract(true);

        BeanDefinitionHolder holder3 = new BeanDefinitionHolder(builder3.getBeanDefinition(), name + "EngineTemplate");
        registerBeanDefinition(holder3, parserContext.getRegistry());

        String serviceTemplate = element.getAttribute("serviceTemplate");
        if (!StringUtils.hasText(serviceTemplate)) {
            parserContext.getReaderContext().error("ServiceTemplate is required for element '"
                    + parserContext.getDelegate().getLocalName(element) + "'", element);
        }
        BeanDefinitionBuilder builder4 = BeanDefinitionBuilder.childBeanDefinition(serviceTemplate);
        builder4.addPropertyReference("engine", name + "Engine");
        builder4.addPropertyReference("serviceConfig", name + "Config");
        builder4.setAbstract(true);

        BeanDefinitionHolder holder4 = new BeanDefinitionHolder(builder4.getBeanDefinition(), name + "ServiceTemplate");
        registerBeanDefinition(holder4, parserContext.getRegistry());

        return null;
    }

}
