package com.algoTrader.util;

import java.lang.reflect.Field;

import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.util.ReflectionUtils;

public class HibernateEventListener implements PostLoadEventListener, PostInsertEventListener, BeanFactoryAware {

    private static final long serialVersionUID = -4314082015943918381L;

    private ConfigurableBeanFactory beanFactory;

    @Override
    public void onPostLoad(PostLoadEvent event) {

        Object entity = event.getEntity();
        process(entity, entity.getClass());
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {

        Object entity = event.getEntity();
        process(entity, entity.getClass());
    }

    private void process(Object entity, Class<?> clazz) {

        if (clazz.getName().endsWith("Impl")) {
            for (Field field : clazz.getDeclaredFields()) {
                Value annotation = field.getAnnotation(Value.class);
                if (annotation != null) {
                    try {
                        String value = this.beanFactory.resolveEmbeddedValue(annotation.value());
                        Object evanulated = this.beanFactory.getBeanExpressionResolver().evaluate(value, new BeanExpressionContext(this.beanFactory, null));
                        Object converted = this.beanFactory.getTypeConverter().convertIfNecessary(evanulated, field.getType());
                        ReflectionUtils.makeAccessible(field);
                        field.set(entity, converted);
                    } catch (Exception ex) {
                        throw new RuntimeException("Could not autowire field: " + field, ex);
                    }
                }
            }
        }

        Class<?> superclazz = clazz.getSuperclass();
        if (superclazz != null) {
            process(entity, superclazz);
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableBeanFactory) beanFactory;
    }
}
