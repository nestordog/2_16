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
package com.algoTrader.esper.subscriber;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;

import org.apache.commons.lang.StringUtils;

import com.algoTrader.ServiceLocator;

/**
 * Generate Subscriber classes on the fly using <a href="http://www.javassist.org/">Javaassist</a>.
 * Use the following syntax to call a spring service method directly:
 * <pre>
 * {@literal @}Subscriber(className='com.algoTrader.service.PositionService.setMargins')
 * </pre>
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SubscriberCreator {

    public static Object createSubscriber(String fqdn) {

        String serviceClassName = StringUtils.substringBeforeLast(fqdn, ".");
        String serviceName = StringUtils.remove(StringUtils.remove(StringUtils.uncapitalize(StringUtils.substringAfterLast(serviceClassName, ".")), "Base"), "Impl");
        String serviceMethodName = StringUtils.substringAfterLast(fqdn, ".");
        String subscriberClassName = serviceClassName + StringUtils.capitalize(serviceMethodName) + "Subscriber";

        Subscriber subscriber;
        try {
            // see if the class already exists
            Class<?> subscriberClazz = Class.forName(subscriberClassName);
            subscriber = (Subscriber) subscriberClazz.newInstance();

            // get the service and hand it to the subscriber
            Class<?> serviceClazz = Class.forName(serviceClassName);
            Object service = ServiceLocator.instance().getService(serviceName, serviceClazz);
            subscriber.setService(service);

        } catch (Exception e) {

            // otherwise create the class
            try {
                ClassPool pool = ClassPool.getDefault();
                pool.insertClassPath(new ClassClassPath(SubscriberCreator.class));

                // get the serviceClass & method
                CtClass serviceClass = pool.get(serviceClassName);
                CtMethod serviceMethod = serviceClass.getDeclaredMethod(serviceMethodName);

                // make the subscriber class
                CtClass subscriberClass = pool.makeClass(subscriberClassName);
                subscriberClass.setSuperclass(pool.get(Subscriber.class.getName()));

                // create the "update" method
                CtClass[] params = serviceMethod.getParameterTypes();
                CtMethod updateMethod = CtNewMethod.make(Modifier.PUBLIC, CtClass.voidType, "update", params, new CtClass[] {}, "return null;", subscriberClass);

                // assemble the body of the method
                //@formatter:off
                String updateMethodBody =
                        "{" +
                        "   org.apache.log4j.Logger logger = com.algoTrader.util.MyLogger.getLogger(\"" + serviceClassName + "\"); " +
                        "    logger.debug(\"" + serviceMethodName + " start\"); " +
                        "    long startTime = System.nanoTime(); " +
                        "    (("+ serviceClassName + ") getService())." + serviceMethodName + "($$); " +
                        "    logger.debug(\"" + serviceMethodName + " end\");" +
                        "    com.algoTrader.util.metric.MetricsUtil.accountEnd(\"" + StringUtils.substringAfterLast(subscriberClassName, ".") + "\", startTime);" +
                        "}";
                //@formatter:on

                updateMethod.setBody(updateMethodBody);
                subscriberClass.addMethod(updateMethod);

                // instanciate the subscriber
                subscriber = (Subscriber) subscriberClass.toClass().newInstance();

                // get the service and hand it to the subscriber
                Class<?> serviceClazz = Class.forName(serviceClassName);
                Object service = ServiceLocator.instance().getService(serviceName, serviceClazz);
                subscriber.setService(service);

            } catch (Exception e2) {
                throw new RuntimeException(subscriberClassName + " could not be created", e2);
            }
        }

        return subscriber;
    }
}
