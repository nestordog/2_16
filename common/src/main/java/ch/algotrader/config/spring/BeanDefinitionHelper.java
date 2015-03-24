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

import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Helper with static utilities to create bean definitions.
 *
 * @author <a href="mailto:mterzer@algotrader.ch">Marco Terzer</a>
 *
 * @version $Revision$ $Date$
 */
final class BeanDefinitionHelper {

    public static String getRequiredAttribute(Element element, ParserContext parserContext, String attributeName) {
        final String value = element.getAttribute(attributeName);
        if (!StringUtils.hasText(value)) {
            parserContext.getReaderContext().error(attributeName + " is required for element '"
                    + parserContext.getDelegate().getLocalName(element) + "'", element);
        }
        return value;
    }
    public static String getOptionalAttribute(Element element, ParserContext parserContext, String attributeName, String defaultValue) {
        final String value = element.getAttribute(attributeName);
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
