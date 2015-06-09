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
package ch.algotrader.wiring.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Hibernate configuration.
 */
@Configuration
public class HibernateWiring {

    @Bean(name = "hibernateProperties")
    public Properties createHibernateProperties() {

        Properties properties = new Properties();

        properties.setProperty("hibernate.default_batch_fetch_size", "16");
        properties.setProperty("hibernate.cache.use_second_level_cache", "true");
        properties.setProperty("hibernate.cache.use_query_cache", "true");
        properties.setProperty("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
        properties.setProperty("hibernate.cache.query_cache_factory", "org.hibernate.cache.internal.StandardQueryCacheFactory");
        // properties.setProperty("hibernate.hbm2ddl.auto", "update");
        // properties.setProperty("hibernate.show_sql", "true");
        return properties;
    }

    @Bean(name = "sessionFactory")
    public SessionFactory createSessionFactory(final DataSource dataSource, final Properties hibernateProperties, final ApplicationContext applicationContext) throws Exception {

        LocalSessionFactoryBean localSessionFactoryBean = new LocalSessionFactoryBean();

        localSessionFactoryBean.setDataSource(dataSource);

        List<Resource> resources = new ArrayList<>();
        resources.addAll(Arrays.asList(applicationContext.getResources("classpath*:ch/**/*.hbm.xml")));
        resources.addAll(Arrays.asList(applicationContext.getResources("classpath*:com/**/*.hbm.xml")));
        resources.addAll(Arrays.asList(applicationContext.getResources("classpath*:META-INF/**/*.hbm.xml")));

        localSessionFactoryBean.setMappingLocations(resources.toArray(new Resource[resources.size()]));

        localSessionFactoryBean.setHibernateProperties(hibernateProperties);

        localSessionFactoryBean.afterPropertiesSet();

        return localSessionFactoryBean.getObject();
    }

    @Bean(name = "defaultAdvisorAutoProxyCreator")
    public DefaultAdvisorAutoProxyCreator createDefaultAdvisorAutoProxyCreator() {

        return new DefaultAdvisorAutoProxyCreator();
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager createTransactionManager(final SessionFactory sessionFactory) throws Exception {

        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory);

        return transactionManager;
    }

    @Bean(name = "txTemplate")
    public TransactionTemplate createTXTemplate(final PlatformTransactionManager transactionManager) throws Exception {

        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(transactionManager);

        return transactionTemplate;
    }

}
