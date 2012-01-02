package com.algoTrader.util;

import java.lang.reflect.Field;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;

public class EntityAnnotationSetter implements BeanFactoryAware {

    private ConfigurableBeanFactory beanFactory;

    public void init() throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {

        // get all Entities
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
        provider.addIncludeFilter(new AbstractClassTestingTypeFilter() {
            @Override
            protected boolean match(ClassMetadata metadata) {
                return metadata.getClassName().endsWith("Impl") && !metadata.getClassName().endsWith("DaoImpl");
            }
        });

        Set<BeanDefinition> components = provider.findCandidateComponents("com.algoTrader.entity");

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

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableBeanFactory) beanFactory;
    }
}
