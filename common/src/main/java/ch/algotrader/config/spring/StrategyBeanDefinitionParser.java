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

import static ch.algotrader.config.spring.BeanDefinitionHelper.getRequiredAttribute;
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
 *
 * @version $Revision$ $Date$
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

        //resource list
        final List<String> resources = getResources(name);

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

    private static List<String> getResources(String name) {
        final String configParamsExtra = System.getProperty(SYS_PROP_CONFIG_PARAMS_EXTRA);
        if (configParamsExtra == null) {
            return Collections.singletonList("classpath:/META-INF/" + name + ".properties");
        } else {
            final List<String> resourceList = new ArrayList<>(2);
            resourceList.add("classpath:/META-INF/" + name + ".properties");
            if (configParamsExtra.contains(":")) {
                resourceList.add(configParamsExtra);
            } else {
                resourceList.add("classpath:/META-INF/" + configParamsExtra);
            }
            return resourceList;
        }
    }

    private void registerBeanDefinition(String beanName, ParserContext parserContext, BeanDefinitionBuilder beanDefinitionBuilder) {
        final BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinitionBuilder.getBeanDefinition(), beanName);
        registerBeanDefinition(holder, parserContext.getRegistry());
    }
}
