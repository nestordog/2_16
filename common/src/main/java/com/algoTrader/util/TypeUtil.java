/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.reflect.FieldUtils;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TypeUtil {

   private static final Set<Object> immediates =
           new HashSet<Object>(Arrays.asList(new Object[]{

                   Boolean.class,
                   Double.class,
                   Float.class,
                   Integer.class,
                   Long.class,
                   Short.class,
                   Character.class,
                   Byte.class,

                   Boolean.TYPE,
                   Double.TYPE,
                   Float.TYPE,
                   Integer.TYPE,
                   Long.TYPE,
                   Short.TYPE,
                   Character.TYPE,
                   Byte.TYPE,

                   String.class,
                   BigDecimal.class,
                   Date.class,
                   Class.class}));

    public static boolean isSimpleAttribute(Field field) {
        return immediates.contains(field.getType()) || Enum.class.isAssignableFrom(field.getType());
    }

    public static List<Field> getAllFields(Class<?> type) {

        List<Field> fields = new ArrayList<Field>();

        while (true) {

            for (Field field : type.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    setAccessible(field);
                    fields.add(field);
                }
            }

            type = type.getSuperclass();
            if (type == Object.class || type == null) {
                break;
            }
        }

        return fields;
    }

    public static void copyAllFields(Object source, Object target) {

        for (Field field : TypeUtil.getAllFields(source.getClass())) {

            try {
                Object targetValue = FieldUtils.readField(field, target, true);
                FieldUtils.writeField(field, source, targetValue, true);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static void setAccessible(final AccessibleObject object) {

        if (object.isAccessible())
            return;

        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                object.setAccessible(true);
                return null;
            }
        });
    }
}
