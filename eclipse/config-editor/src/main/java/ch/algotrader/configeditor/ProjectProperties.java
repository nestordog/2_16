package ch.algotrader.configeditor;

import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.core.IJavaProject;

import ch.algotrader.configeditor.editingsupport.CellEditorFactory;
import ch.algotrader.configeditor.editingsupport.CheckboxCellEditorFactory;
import ch.algotrader.configeditor.editingsupport.DateTimeCellEditorFactory;
import ch.algotrader.configeditor.editingsupport.DateTimeSerializer;
import ch.algotrader.configeditor.editingsupport.DoubleCellEditorFactory;
import ch.algotrader.configeditor.editingsupport.DoubleSerializer;
import ch.algotrader.configeditor.editingsupport.EnumCellEditorFactory;
import ch.algotrader.configeditor.editingsupport.ISetDataType;
import ch.algotrader.configeditor.editingsupport.IntegerCellEditorFactory;
import ch.algotrader.configeditor.editingsupport.TextCellEditorFactory;

public class ProjectProperties {

    private final IJavaProject javaProject;
    private ClassLoader projectClassLoader;

    ProjectProperties(IJavaProject javaProject) {
        this.javaProject = javaProject;
        projectClassLoader = null;
    }

    public CellEditorFactory createCellEditorFactory(String propertyId) {
        try {
            Class<?> propClass = findClass(propertyId);
            if (propClass == null) {
                IConfigurationElement config = PropertyDefExtensionPoint.findConfig(propertyId);
                if (config != null) {
                    CellEditorFactory factory = (CellEditorFactory) config.createExecutableExtension("cellEditorFactory");
                    if (factory instanceof ISetDataType)
                        ((ISetDataType) factory).setDataType(findClass(getDataType(propertyId)));
                    return factory;
                }
            } else {
                if (propClass.isEnum()) {
                    EnumCellEditorFactory factory = new EnumCellEditorFactory();
                    factory.setDataType(propClass);
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
    public Object deserialize(String propertyId, String stringValue) {
        if (stringValue == null)
            return null;
        try {
            Class<?> propClass = findClass(getDataType(propertyId));
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

    public Class<?> findClass(String className) {
        Class<?> result = null;
        if (className.contains("."))
            try {
                result = Class.forName(className);
            } catch (Exception e) {
                try {
                    if (projectClassLoader == null)
                        projectClassLoader = ProjectUtils.getClassLoader(javaProject);
                    result = projectClassLoader.loadClass(className);
                } catch (Exception e2) {
                    // we just return null in case of class loading errors
                }
            }
        return result;
    }

    public String getDataType(String propertyId) {
        Class<?> propClass = findClass(propertyId);
        if (propClass != null)
            return propertyId;
        IConfigurationElement config = PropertyDefExtensionPoint.getConfig(propertyId);
        if (config != null) {
            String result = config.getAttribute("dataType");
            if (result == null)
                throw new RuntimeException(MessageFormat.format("Property ''{0}'' does not provide data type", propertyId));
            return result;
        }
        return null;
    }

    public String serialize(String propertyId, Object value) {
        try {
            Class<?> propClass = findClass(getDataType(propertyId));
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
