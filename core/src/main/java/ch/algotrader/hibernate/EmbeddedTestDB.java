/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.hibernate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

/**
 * Embedded test H2 database.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class EmbeddedTestDB {

    public static final EmbeddedTestDB DATABASE;

    static {
        try {

            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            List<Resource> resources = new ArrayList<>();
            resources.addAll(Arrays.asList(resolver.getResources("classpath*:ch/**/*.hbm.xml")));
            resources.addAll(Arrays.asList(resolver.getResources("classpath*:com/**/*.hbm.xml")));
            resources.addAll(Arrays.asList(resolver.getResources("classpath*:META-INF/**/*.hbm.xml")));

            DATABASE = new EmbeddedTestDB(resources.toArray(new Resource[resources.size()]), null, false);
        } catch (Exception ex) {

            throw new RuntimeException(ex);

        }
    }

    private final EmbeddedDatabase database;

    private final SessionFactory sessionFactory;

    public EmbeddedTestDB(final Resource[] mappings, final ResourceDatabasePopulator dbPopulator, final boolean generateSchema) throws Exception {

        EmbeddedDatabaseFactory dbFactory = new EmbeddedDatabaseFactory();
        dbFactory.setDatabaseType(EmbeddedDatabaseType.H2);
        dbFactory.setDatabaseName("testdb;MODE=MYSQL;DATABASE_TO_UPPER=FALSE");
        dbFactory.setDatabasePopulator(dbPopulator);

        this.database = dbFactory.getDatabase();

        LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();
        factoryBean.setDataSource(this.database);
        factoryBean.setMappingLocations(mappings);

        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.use_query_cache", "false");
        // properties.setProperty("hibernate.show_sql", "true");
        // properties.setProperty("hibernate.format_sql", "true");

        if (generateSchema) {

            properties.setProperty("hibernate.hbm2ddl.auto", "create");
        }

        factoryBean.setHibernateProperties(properties);

        factoryBean.afterPropertiesSet();

        this.sessionFactory = factoryBean.getObject();
    }

    public EmbeddedTestDB(final Resource... mappings) throws Exception {

        this(mappings, null, true);
    }

    public SessionFactory getSessionFactory() {

        return this.sessionFactory;
    }

    public DataSource getDataSource() {

        return this.database;
    }

    public void shutdown() {

        this.sessionFactory.close();

        this.database.shutdown();
    }

}
