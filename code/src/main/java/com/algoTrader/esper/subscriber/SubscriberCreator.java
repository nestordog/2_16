package com.algoTrader.esper.subscriber;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;

import org.apache.commons.lang.StringUtils;

import com.algoTrader.ServiceLocator;

public class SubscriberCreator {

    public static Object createSubscriber(String fqdn) {

        String serviceClassName = StringUtils.substringBeforeLast(fqdn, ".");
        String serviceName = StringUtils.remove(StringUtils.remove(StringUtils.uncapitalize(StringUtils.substringAfterLast(serviceClassName, ".")), "Base"),
                "Impl");
        String serviceMethodName = StringUtils.substringAfterLast(fqdn, ".");
        String subscriberClassName = serviceClassName + StringUtils.capitalize(serviceMethodName) + "Subscriber";

        Subscriber subscriber;
        try {
            // see if the class already exists
            Class<?> cl = Class.forName(subscriberClassName);
            subscriber = (Subscriber) cl.newInstance();

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
                CtMethod updateMethod = CtNewMethod
                        .make(Modifier.PUBLIC, CtClass.voidType, "update", params, new CtClass[] {}, "return null;", subscriberClass);

                // assemble the body of the method
                String updateMethodBody = "{long startTime = System.currentTimeMillis(); " + "logger.debug(\"" + serviceMethodName + " start\"); " + "(("
                        + serviceClassName + ") getService())." + serviceMethodName + "($$); " + "logger.debug(\"" + serviceMethodName
                        + " end (\" + (System.currentTimeMillis() - startTime) + \"ms execution)\");}";

                updateMethod.setBody(updateMethodBody);
                subscriberClass.addMethod(updateMethod);

                // instanciate the subscriber
                subscriber = (Subscriber) subscriberClass.toClass().newInstance();
            } catch (Exception e2) {
                throw new RuntimeException(subscriberClassName + " could not be created", e2);
            }

        }

        // get the service and hand it to the subscriber
        Object service = ServiceLocator.commonInstance().getService(serviceName);
        subscriber.setService(service);

        return subscriber;
    }
}
