package com.algoTrader.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.util.spring.ClassPathScanner;

public class ServiceUtil {

    @SuppressWarnings("unchecked")
    public static <T> Set<T> getServicesByInterface(Class<T> interfaceClass) {

        // use the scanner to get all Service Interfaces
        ClassPathScanner scanner = new ClassPathScanner(true);
        scanner.addIncludeFilter(new AbstractClassTestingTypeFilter() {
            @Override
            protected boolean match(ClassMetadata metadata) {
                if (metadata.getClassName().endsWith("Service")) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        // only look in service package
        Set<BeanDefinition> components = scanner.findCandidateComponents("com.algoTrader.service");

        Set<T> services = new HashSet<T>();
        for (BeanDefinition component : components) {

            try {
                // check if class is implementing the interfaceClass
                Class<? extends T> clazz = (Class<? extends T>) Class.forName(component.getBeanClassName());
                if (interfaceClass.isAssignableFrom(clazz)) {

                    // get the beanName
                    String beanName = StringUtils.uncapitalize(StringUtils.substringAfterLast(component.getBeanClassName(), "."));

                    // get the service
                    services.add(ServiceLocator.instance().getService(beanName, clazz));
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return services;
    }
}
