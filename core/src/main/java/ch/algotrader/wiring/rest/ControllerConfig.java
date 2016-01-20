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

package ch.algotrader.wiring.rest;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.algotrader.cache.CacheManager;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.entity.security.Security;
import ch.algotrader.rest.CacheRestController;
import ch.algotrader.rest.ConfigRestController;
import ch.algotrader.rest.LookupRestController;
import ch.algotrader.rest.MarketDataRestController;
import ch.algotrader.rest.MetaDataRestController;
import ch.algotrader.rest.OrderRestController;
import ch.algotrader.rest.PositionRestController;
import ch.algotrader.rest.RuntimeRestController;
import ch.algotrader.rest.TransactionRestController;
import ch.algotrader.rest.index.SecurityIndexer;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.PositionService;
import ch.algotrader.service.TransactionService;

@Configuration
@EnableWebMvc
public class ControllerConfig extends WebMvcConfigurerAdapter {

    @Autowired(required = false) @Qualifier("staticResourceRoot")
    private String staticResourceRoot;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LookupService lookupService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private MarketDataService marketDataService;
    @Autowired
    private ConfigParams configParams;

    @Override
    public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {

        converters.add(new MappingJackson2HttpMessageConverter(this.objectMapper));
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {

        if (this.staticResourceRoot != null) {
            registry.addResourceHandler("/**", "/")
                    .addResourceLocations(this.staticResourceRoot)
                    .setCachePeriod(24 * 60 * 60);
        }
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("/index.html");
    }

    @Bean(name = "metaDataRestController")
    public MetaDataRestController createMetaDataRestController() {
        return new MetaDataRestController();
    }

    @Bean(name = "lookupRestController")
    public LookupRestController createLookupRestController() {

        SecurityIndexer securityIndexer = new SecurityIndexer();
        Collection<Security> allSecurities = this.lookupService.getAllSecurities();
        securityIndexer.init(allSecurities);
        return new LookupRestController(this.lookupService, securityIndexer);
    }

    @Bean(name = "orderRestController")
    public OrderRestController createOrderRestController() {

        return new OrderRestController(this.orderService);
    }

    @Bean(name = "marketDataRestController")
    public MarketDataRestController createMarketDataRestController() {

        return new MarketDataRestController(this.marketDataService);
    }

    @Bean(name = "configRestController")
    public ConfigRestController createConfigRestController() {

        return new ConfigRestController(this.configParams);
    }

    @Bean(name = "positionRestController")
    public PositionRestController createPositionRestController(
            final PositionService positionService) {

        return new PositionRestController(positionService);
    }

    @Bean(name = "transactionRestController")
    public TransactionRestController createTransactionRestController(
            final TransactionService transactionService,
            final LookupService lookupService) {

        return new TransactionRestController(transactionService, lookupService);
    }

    @Bean(name = "cacheRestController")
    public CacheRestController createCacheRestController(
            final CacheManager cacheManager) {

        return new CacheRestController(cacheManager);
    }

    @Bean(name = "runtimeRestController")
    public RuntimeRestController createRuntimeRestController() {

        return new RuntimeRestController();
    }

}
