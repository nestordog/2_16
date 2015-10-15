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
package ch.algotrader.starter;

import java.lang.reflect.Method;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.StringTokenizer;

import ch.algotrader.ServiceLocator;
import ch.algotrader.util.DateTimeLegacy;

/**
 * Generic Starter Class that can be used to invoke any service that has String, Integer, Double or Date based parameters
 * <p>
 * Usage: {@code serviceName1:methodName1:param(s) serviceName2:methodName2:param(s)}
 * <p>
 * Example: {@code accountService:restorePortfolioValues:07.01.12:03.12.12}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class GenericStarter {

    public static void main(String[] args) throws Exception {

        ServiceLocator serviceLocator = ServiceLocator.instance();
        serviceLocator.init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        try {
            for (String arg : args) {
                invoke(serviceLocator, arg);
            }
        } finally {
            serviceLocator.shutdown();
        }
    }

    public static Object invoke(final ServiceLocator serviceLocator, final String arg) {

        if (arg == null) {
            throw new IllegalArgumentException("you must specify service and method");
        }

        StringTokenizer tokenizer = new StringTokenizer(arg, ":");

        int len = tokenizer.countTokens();
        if (len < 2) {
            throw new IllegalArgumentException("you must specifiy service and method");
        }

        String serviceName = tokenizer.nextToken();
        String methodName = tokenizer.nextToken();

        Object service = serviceLocator.getService(serviceName);

        Object result = null;
        boolean found = false;
        for (Method method : service.getClass().getMethods()) {
            if (method.getName().equals(methodName)) {
                found = true;

                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != len - 2) {
                    throw new IllegalArgumentException("number of parameters does not match");
                }

                Object[] params = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    String param = tokenizer.nextToken();
                    if (parameterType.equals(String.class)) {
                        params[i] = param;
                    } else if (parameterType.equals(int.class) || parameterType.equals(Integer.class)) {
                        params[i] = Integer.valueOf(param);
                    } else if (parameterType.equals(double.class) || parameterType.equals(Double.class)) {
                        params[i] = Double.valueOf(param);
                    } else if (parameterType.equals(Date.class)) {
                        try {
                            params[i] = DateTimeLegacy.parseAsLocalDateTime(param);
                        } catch (DateTimeParseException e) {
                            try {
                                params[i] = DateTimeLegacy.parseAsLocalDate(param);
                            } catch (DateTimeParseException e1) {
                                throw new IllegalStateException(e1);
                            }
                        }
                    }
                }

                try {
                    result = method.invoke(service, params);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }

                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("method does not exist");
        }

        return result;
    }
}
