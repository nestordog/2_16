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
package ch.algotrader.configeditor;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * Eclipse extension point holding property definitions.
 *
 * @author <a href="mailto:ahihlovskiy@algotrader.ch">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
public class PropertyDefExtensionPoint {

    public static IConfigurationElement findConfig(String propertyId) {
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IConfigurationElement[] extensions = reg.getConfigurationElementsFor("ch.algotrader.config-editor.PropertyDef");
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement element = extensions[i];
            if (element.getAttribute("id").equals(propertyId)) {
                return element;
            }
        }
        return null;
    }

    public static IConfigurationElement getConfig(String propertyId) {
        IConfigurationElement result = findConfig(propertyId);
        if (result == null)
            throw new RuntimeException(MessageFormat.format("Property ''{0}'' is not defined", propertyId));
        return result;
    }

    public static String getRegex(String propertyId) {
        IConfigurationElement config = findConfig(propertyId);
        if (config != null)
            return config.getAttribute("regex");
        return null;
    }

    public static String getRegexErrorMessage(String propertyId, String value) {
        String regex = "";
        String regexErrorMessage = null;
        IConfigurationElement config = findConfig(propertyId);
        if (config != null) {
            regex = config.getAttribute("regex");
            regexErrorMessage = config.getAttribute("regexErrorMessage");
        }
        if (regexErrorMessage == null)
            regexErrorMessage = "User input ''{0}'' does not satisfy pattern {1}";
        return MessageFormat.format(regexErrorMessage, value, regex);
    }
}
