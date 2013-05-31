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
package ch.algorader.util.spring;

import java.lang.reflect.Field;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;

/**
 * Utility class that sets annotation configured values of static entity fields.
 *
 * Example: <pre>private static @Value("${misc.portfolioDigits}") int portfolioDigits;</pre>
 *
 * This Bean needs to be configured with the {@code init} method to make sure that all static field values are assigned
 * before the first Entity instances are created.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EntityAnnotationSetter implements BeanFactoryAware {

    private ConfigurableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableBeanFactory) beanFactory;
    }

    public void init() throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {

        // get all Entities
        ClassPathScanner scanner = new ClassPathScanner(true);
        scanner.addIncludeFilter(new AbstractClassTestingTypeFilter() {
            @Override
            protected boolean match(ClassMetadata metadata) {

                // match only "Impl" but not "DaoImpl"
                return metadata.getClassName().endsWith("Impl") && !metadata.getClassName().endsWith("DaoImpl");
            }
        });

        // check package "ch.algorader.entity"
        Set<BeanDefinition> components = scanner.findCandidateComponents("ch.algorader.entity");

        for (BeanDefinition component : components) {

            Class<?> clazz = Class.forName(component.getBeanClassName());
            for (Field field : clazz.getDeclaredFields()) {

                // process all value annotations
                Value annotation = field.getAnnotation(Value.class);
                if (annotation != null) {

                    // get the value
                    String value = this.beanFactory.resolveEmbeddedValue(annotation.value());

                    // evaluate potentiel expressions
                    Object evaluated = this.beanFactory.getBeanExpressionResolver().evaluate(value, new BeanExpressionContext(this.beanFactory, null));

                    // convert if necessarry
                    Object converted = this.beanFactory.getTypeConverter().convertIfNecessary(evaluated, field.getType());

                    // make the field accessible if it is private
                    field.setAccessible(true);

                    // set the field value
                    field.set(null, converted);
                }
            }
        }
    }

    private class ClassPathScanner extends ClassPathScanningCandidateComponentProvider {

        public ClassPathScanner(boolean useDefaultFilters) {
            super(useDefaultFilters);
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {

            return beanDefinition.getMetadata().isIndependent();
        }
    }
}
