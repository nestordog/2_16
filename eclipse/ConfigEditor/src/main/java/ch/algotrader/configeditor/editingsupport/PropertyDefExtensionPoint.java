/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH. The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation, disassembly or reproduction of this material is strictly forbidden unless
 * prior written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH Badenerstrasse 16 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.configeditor.editingsupport;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;

import ch.algotrader.configeditor.IPropertySerializer;

public class PropertyDefExtensionPoint {

    public static CellEditorFactory createCellEditorFactory(String propertyId) throws InvalidRegistryObjectException, CoreException {
        IConfigurationElement config = getConfig(propertyId);
        if (config != null) {
            CellEditorFactory factory = (CellEditorFactory) config.createExecutableExtension("cellEditorFactory");
            if (factory instanceof ISetDataType)
                ((ISetDataType) factory).setDataType(getDataType(propertyId));
            return factory;
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Object deserialize(String propertyId, String stringValue) {
        if (stringValue == null)
            return null;
        try {
            Class<?> dataClass = Class.forName(getDataType(propertyId));
            if (IPropertySerializer.class.isAssignableFrom(dataClass))
                return ((IPropertySerializer) dataClass.newInstance()).deserialize(stringValue);
            if (dataClass.isEnum())
                return Enum.valueOf((Class<? extends Enum>) dataClass, stringValue);
            return dataClass.getDeclaredConstructor(String.class).newInstance(stringValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static IConfigurationElement getConfig(String propertyId) {
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IConfigurationElement[] extensions = reg.getConfigurationElementsFor("ch.algotrader.ConfigEditor.PropertyDef");
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement element = extensions[i];
            if (element.getAttribute("id").equals(propertyId)) {
                return element;
            }
        }
        throw new RuntimeException(MessageFormat.format("Property ''{0}'' is not defined", propertyId));
    }

    public static String getRegex(String propertyId) {
        IConfigurationElement config = getConfig(propertyId);
        if (config != null)
            return config.getAttribute("regex");
        return null;
    }

    public static String getRegexErrorMessage(String propertyId, String value) {
        String regexErrorMessage;
        IConfigurationElement config = getConfig(propertyId);
        assert config != null;
        regexErrorMessage = config.getAttribute("regexErrorMessage");
        if (regexErrorMessage == null)
            regexErrorMessage = "User input ''{0}'' does not satisfy pattern {1}";
        return MessageFormat.format(regexErrorMessage, value, config.getAttribute("regex"));
    }

    public static String getDataType(String propertyId) {
        IConfigurationElement config = getConfig(propertyId);
        if (config != null) {
            String result = config.getAttribute("dataType");
            if (result == null)
                throw new RuntimeException(MessageFormat.format("Property ''{0}'' does not provide data type", propertyId));
            return result;
        }
        return null;
    }

    public static String serialize(String propertyId, Object value) {
        if (value == null)
            return "null";
        try {
            Class<?> dataClass = Class.forName(getDataType(propertyId));
            if (IPropertySerializer.class.isAssignableFrom(dataClass))
                return ((IPropertySerializer) dataClass.newInstance()).serialize(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return value.toString();
    }
}
