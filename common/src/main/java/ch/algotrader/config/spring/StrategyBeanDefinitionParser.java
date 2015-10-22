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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * AlgoTrader strategy definition parser.
 * <p>
 * Example input:
<pre>{@code
<at:strategy name="boxNarrow"
             configClass="ch.algotrader.strategy.box.BoxConfig"
             engineTemplate="boxEngineTemplate"
             serviceTemplate="boxServiceTemplate" />
}</pre>
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public final class StrategyBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String SYS_PROP_CONFIG_PARAMS_EXTRA = "config.params.extra";

    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        //required attributes
        final String name = getRequiredAttribute(element, parserContext, "name");
        final String configClass = getRequiredAttribute(element, parserContext, "configClass");
        final String engineTemplate = getRequiredAttribute(element, parserContext, "engineTemplate");
        final String serviceTemplate = getRequiredAttribute(element, parserContext, "serviceTemplate");
        final String configResource = getOptionalAttribute(element, "configResource");

        //resource list
        final List<String> resources = configResource != null ? getResources(configResource) : getResources(name + ".properties");

        //register beans
        registerBeanDefinition(name + "ConfigParamsTemplate", parserContext, rootBeanDefinition(CustomConfigParamsFactoryBean.class)
                .addPropertyReference("global", "configParams")
                .addPropertyValue("resources", resources)
                .setAbstract(true));
        registerBeanDefinition(name + "ConfigTemplate", parserContext, rootBeanDefinition(CustomConfigBeanFactoryBean.class)
                .addPropertyValue("beanClass", configClass)
                .addPropertyReference("configParams", name + "ConfigParams")
                .setAbstract(true));
        registerBeanDefinition(name + "EngineTemplate", parserContext, childBeanDefinition(engineTemplate)
                .addPropertyReference("configParams", name + "ConfigParams")
                .setAbstract(true));
        registerBeanDefinition(name + "ServiceTemplate", parserContext, childBeanDefinition(serviceTemplate)
                .setAbstract(true));

        return null;
    }

    private static List<String> getResources(final String resourceName) {
        final String configParamsExtra = System.getProperty(SYS_PROP_CONFIG_PARAMS_EXTRA);
        if (configParamsExtra == null) {
            return Collections.singletonList("classpath:/" + resourceName);
        } else {
            final List<String> resourceList = new ArrayList<>(2);
            resourceList.add("classpath:/" + resourceName);
            if (configParamsExtra.contains(":")) {
                resourceList.add(configParamsExtra);
            } else {
                resourceList.add("classpath:/" + configParamsExtra);
            }
            return resourceList;
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

    private static String getOptionalAttribute(Element element, String attributeName) {
        final String value = element.getAttribute(attributeName);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value;
    }
}
