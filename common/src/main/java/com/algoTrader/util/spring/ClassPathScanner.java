package com.algoTrader.util.spring;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

public class ClassPathScanner extends ClassPathScanningCandidateComponentProvider {

    public ClassPathScanner(boolean useDefaultFilters) {
        super(useDefaultFilters);
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        //return (beanDefinition.getMetadata().isConcrete() && beanDefinition.getMetadata().isIndependent());
        return beanDefinition.getMetadata().isIndependent();
    }
}
