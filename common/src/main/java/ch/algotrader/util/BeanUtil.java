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
package ch.algotrader.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;

/**
 * Contains utility methods for populating JavaBeans properties via reflection.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class BeanUtil {

    private static final BeanUtilsBean UTILS = new BeanUtilsBean(new ConvertUtilsBean() {
        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Object convert(String value, Class clazz) {
            if (clazz.isEnum()) {
                return Enum.valueOf(clazz, value);
            } else {
                return super.convert(value, clazz);
            }
        }
    });

    /**
     *  Populates an arbitrary object with values from the specified map. Only matching fields are populated.
     */
    public static void populate(Object bean, Map<String, ?> properties) throws IllegalAccessException, InvocationTargetException {
        UTILS.populate(bean, properties);
    }

    @SuppressWarnings("unchecked")
    public static <T> T clone(T bean) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        return (T) UTILS.cloneBean(bean);
    }

    public static <T> T cloneAndPopulate(T bean, Map<String, ?> properties) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        T clone = clone(bean);
        populate(clone, properties);
        return clone;
    }

    /**
     * Returns a list of PropertyDescriptiors for the specified bean
     */
    public static PropertyDescriptor[] getPropertyDescriptors(Object bean) {
        return UTILS.getPropertyUtils().getPropertyDescriptors(bean);
    }
}
