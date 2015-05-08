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

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * DataSource configurations.
 *
 * @version $Revision$ $Date$
 */
@Configuration
public class DataSourceWiring {

    @Profile("singleDataSource")
    @Bean(name = "dataSource")
    public DataSource createSingleDataSource(
            @Value("${dataSource.driver}") final String driver,
            @Value("${dataSource.user}") final String user,
            @Value("${dataSource.password}") final String password,
            @Value("${dataSource.url}") final String url) throws Exception {

        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setDriverClassName(driver);
        driverManagerDataSource.setUsername(user);
        driverManagerDataSource.setPassword(password);
        driverManagerDataSource.setUrl(url);

        return driverManagerDataSource;
    }

    @Profile("embeddedDataSource")
    @Bean(name = "dataSource")
    public DataSource createEmbeddedDataSource(final ApplicationContext applicationContext) throws Exception {

        EmbeddedDatabaseFactory databaseFactory = new EmbeddedDatabaseFactory();
        databaseFactory.setDatabaseType(EmbeddedDatabaseType.H2);

        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScript(applicationContext.getResource("/db/h2/h2.sql"));
        databasePopulator.addScripts(applicationContext.getResources("classpath*:/db/h2/h2-*.sql"));
        databaseFactory.setDatabasePopulator(databasePopulator);

        return databaseFactory.getDatabase();
    }

    @Profile("pooledDataSource")
    @Bean(name = "dataSource")
    public DataSource createPooledDataSource(
            @Value("${dataSource.driver}") final String driver,
            @Value("${dataSource.user}") final String user,
            @Value("${dataSource.password}") final String password,
            @Value("${dataSource.url}") final String url) throws Exception {

        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        comboPooledDataSource.setDriverClass(driver);
        comboPooledDataSource.setUser(user);
        comboPooledDataSource.setPassword(password);
        comboPooledDataSource.setJdbcUrl(url);
        comboPooledDataSource.setIdleConnectionTestPeriod(300);
        comboPooledDataSource.setMaxIdleTime(1800);

        return comboPooledDataSource;
    }

}
