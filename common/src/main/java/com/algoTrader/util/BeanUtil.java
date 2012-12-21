package com.algoTrader.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;

public class BeanUtil {

    private static BeanUtilsBean beanUtilsBean = new BeanUtilsBean(new ConvertUtilsBean() {
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

    public static void populate(Object bean, Map<?, ?> properties) throws IllegalAccessException, InvocationTargetException {
        beanUtilsBean.populate(bean, properties);
    }
}
