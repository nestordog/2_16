package com.algoTrader.util;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.collection.AbstractPersistentCollection;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import com.algoTrader.service.LazyLoaderService;

@Aspect
public class LazyLoaderAspect {

    private static Logger logger = MyLogger.getLogger(LazyLoaderAspect.class.getName());

    private LazyLoaderService lazyLoaderService;
    private HashSet<Serializable> classes = new HashSet<Serializable>();

    public void setLazyLoaderService(LazyLoaderService lazyLoaderService) {
        this.lazyLoaderService = lazyLoaderService;
    }

    @Around("getCollection()")
    public Object lazyLoadCollection(ProceedingJoinPoint pjp) throws Throwable {

        Object result = pjp.proceed();

        if (result instanceof AbstractPersistentCollection) {

            AbstractPersistentCollection col = (AbstractPersistentCollection) result;
            if (!col.wasInitialized() && col.getSession() == null && !this.classes.contains(col.getKey())) {

                Object target = pjp.getTarget();
                this.classes.add(col.getKey());

                try {
                    AbstractPersistentCollection newCol = this.lazyLoaderService.lazyLoadCollection(target, pjp.toShortString(), col);

                    // set the returned collection to the target (since target is not beeing modified through RMI)
                    MethodSignature signature = (MethodSignature) pjp.getStaticPart().getSignature();
                    Method method = target.getClass().getMethod("set" + signature.getName().substring(3), signature.getReturnType());
                    method.invoke(target, newCol);

                    logger.debug("loaded collection: " + pjp.toShortString());

                    return newCol;
                } catch (Exception ex) {
                    logger.error("problem loading: " + pjp.toShortString(), ex);
                } finally {
                    this.classes.remove(col.getKey());
                }
            }
        }
        return result;
    }

    @Around("getProxy()")
    public Object lazyLoadProxy(ProceedingJoinPoint pjp) throws Throwable {

        Object result = pjp.proceed();

        if (result instanceof HibernateProxy) {

            HibernateProxy proxy = (HibernateProxy) result;

            LazyInitializer initializer = proxy.getHibernateLazyInitializer();
            if (initializer.isUninitialized() && initializer.getSession() == null) {

                Object target = pjp.getTarget();

                try {

                    Object newProxy = this.lazyLoaderService.lazyLoadProxy(target, pjp.toShortString(), proxy);

                    // set the returned collection to the target (since target is not beeing modified through RMI)
                    MethodSignature signature = (MethodSignature) pjp.getStaticPart().getSignature();
                    Method method = target.getClass().getMethod("set" + signature.getName().substring(3), signature.getReturnType());
                    method.invoke(target, newProxy);

                    logger.debug("loaded proxy: " + pjp.toShortString());

                    return newProxy;
                } catch (Exception ex) {
                    logger.error("problem loading: " + pjp.toShortString(), ex);
                }
            }
        }
        return result;
    }

    @Pointcut("call(java.util.Collection com.algoTrader.entity.*.get*(..)) && within(com.algoTrader..*)")
    public void getCollection() {
    }

    @Pointcut("call(com.algoTrader.entity.* com.algoTrader.entity.*.get*(..)) && within(com.algoTrader..*)")
    public void getProxy() {
    }
}
