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
package ch.algotrader.configeditor.editingsupport;

import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import ch.algotrader.configeditor.IPropertySerializer;

/**
 * Eclipse extension point holding property definitions.
 *
 * @author <a href="mailto:ahihlovskiy@algotrader.ch">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
public class PropertyDefExtensionPoint {

    public static CellEditorFactory createCellEditorFactory(String propertyId) {
        try {
            Class<?> propClass = findClass(propertyId);
            if (propClass == null) {
                IConfigurationElement config = findConfig(propertyId);
                if (config != null) {
                    CellEditorFactory factory = (CellEditorFactory) config.createExecutableExtension("cellEditorFactory");
                    if (factory instanceof ISetDataType)
                        ((ISetDataType) factory).setDataType(getDataType(propertyId));
                    return factory;
                }
            } else {
                if (propClass.isEnum()) {
                    EnumCellEditorFactory factory = new EnumCellEditorFactory();
                    factory.setDataType(propertyId);
                    return factory;
                }
                if (propClass == Date.class)
                    return new DateTimeCellEditorFactory();
                if (propClass == Double.class)
                    return new DoubleCellEditorFactory();
                if (propClass == Integer.class)
                    return new IntegerCellEditorFactory();
                if (propClass == Boolean.class)
                    return new CheckboxCellEditorFactory();
            }
            return new TextCellEditorFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Object deserialize(String propertyId, String stringValue) {
        if (stringValue == null)
            return null;
        try {
            Class<?> propClass = findClass(propertyId);
            if (propClass == null)
                propClass = Class.forName(getDataType(propertyId));
            if (IPropertySerializer.class.isAssignableFrom(propClass))
                return ((IPropertySerializer) propClass.newInstance()).deserialize(stringValue);
            if (propClass == Date.class)
                return new DateTimeSerializer().deserialize(stringValue);
            if (propClass == Double.class)
                return new DoubleSerializer().deserialize(stringValue);
            if (propClass.isEnum())
                return Enum.valueOf((Class<? extends Enum>) propClass, stringValue);
            return propClass.getDeclaredConstructor(String.class).newInstance(stringValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> findClass(String className) {
        Class<?> result = null;
        try {
            result = Class.forName(className);
        } catch (Exception e) {
            // we just return null in case of errors
        }
        return result;
    }

    private static IConfigurationElement findConfig(String propertyId) {
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

    private static IConfigurationElement getConfig(String propertyId) {
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

    public static String getDataType(String propertyId) {
        Class<?> propClass = findClass(propertyId);
        if (propClass != null)
            return propertyId;
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
        try {
            Class<?> propClass = findClass(propertyId);
            if (propClass == null)
                propClass = Class.forName(getDataType(propertyId));
            if (propClass == Date.class) {
                if (value == null)
                    return "";
                return new DateTimeSerializer().serialize(value);
            }
            if (IPropertySerializer.class.isAssignableFrom(propClass))
                return ((IPropertySerializer) propClass.newInstance()).serialize(value);
            if (propClass == Double.class)
                return new DoubleSerializer().serialize(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return value.toString();
    }
}
