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

package ch.algotrader;

import java.util.Collection;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.context.support.AbstractApplicationContext;

import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.lifecycle.LifecycleManager;
import ch.algotrader.service.CalendarService;
import ch.algotrader.service.ChartProvidingService;
import ch.algotrader.service.CombinationService;
import ch.algotrader.service.FutureService;
import ch.algotrader.service.HistoricalDataService;
import ch.algotrader.service.LazyLoaderService;
import ch.algotrader.service.LocalLookupService;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.ManagementService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.MeasurementService;
import ch.algotrader.service.OptionService;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.PortfolioChartService;
import ch.algotrader.service.PortfolioService;
import ch.algotrader.service.PositionService;
import ch.algotrader.service.PropertyService;
import ch.algotrader.service.ReferenceDataService;
import ch.algotrader.service.SubscriptionService;

/**
 * Locates and provides all available application services.
 */
public class ServiceLocator {

    // The default bean reference factory ID, referencing beanRefFactory.
    private static final String DEFAULT_BEAN_REFERENCE_ID = "beanRefFactory";

    // The different bean reference types
    public static final String LOCAL_BEAN_REFERENCE_LOCATION = "Local";
    public static final String EMBEDDED_BEAN_REFERENCE_LOCATION = "Embedded";
    public static final String SERVER_BEAN_REFERENCE_LOCATION = "Server";
    public static final String CLIENT_BEAN_REFERENCE_LOCATION = "Client";
    public static final String SIMULATION_BEAN_REFERENCE_LOCATION = "Simulation";

    // The bean factory reference instance.
    private volatile BeanFactoryReference beanFactoryReference;//volatile for Double Check Locking

    // The bean factory reference location.
    private String beanFactoryReferenceLocation;

    /**
     * Initializes the Spring application context from
     * the given <code>beanFactoryReferenceLocation</code>.  If <code>null</code>
     * is specified for the <code>beanFactoryReferenceLocation</code>
     * then the default application context will be used.
     *
     * @param beanFactoryReferenceLocationIn the location of the beanRefFactory reference.
     */
    public synchronized void init(final String beanFactoryReferenceLocationIn) {

        this.beanFactoryReferenceLocation = beanFactoryReferenceLocationIn;
        this.beanFactoryReference = null;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                ServiceLocator serviceLocator = ServiceLocator.instance();
                if (serviceLocator.isInitialized()) {

                    serviceLocator.shutdown();
                }
            }
        });
    }

    /**
     * Gets the Spring ApplicationContext.
     * @return beanFactoryReference.getFactory()
     */
    public ApplicationContext getContext() {

        if (this.beanFactoryReference == null) {

            synchronized (this) {
                if (this.beanFactoryReference == null) {
                    if (this.beanFactoryReferenceLocation == null) {
                        this.beanFactoryReferenceLocation = SERVER_BEAN_REFERENCE_LOCATION;
                    }

                    String location = DEFAULT_BEAN_REFERENCE_ID + this.beanFactoryReferenceLocation + ".xml";
                    BeanFactoryLocator beanFactoryLocator = ContextSingletonBeanFactoryLocator.getInstance(location);
                    this.beanFactoryReference = beanFactoryLocator.useBeanFactory(DEFAULT_BEAN_REFERENCE_ID);

                    // set the profiles
                    ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) this.beanFactoryReference.getFactory();
                    applicationContext.getEnvironment().addActiveProfile(this.beanFactoryReferenceLocation.toLowerCase());
                }
            }
        }

        return (ApplicationContext) this.beanFactoryReference.getFactory();
    }

    /**
     * checks weather the Spring application context has been initialized
     */
    public boolean isInitialized() {

        return this.beanFactoryReference != null;
    }

    /**
     * Shuts down the ServiceLocator and releases any used resources.
     */
    public synchronized void shutdown() {

        ((AbstractApplicationContext) this.getContext()).close();
        if (this.beanFactoryReference != null) {
            this.beanFactoryReference.release();
            this.beanFactoryReference = null;
        }
    }

    /**
     * Gets an instance of the given service.
     * @param serviceName
     * @param clazz
     * @return getContext().getBean(serviceName, clazz)
     */
    public <T> T getService(String serviceName, Class<T> clazz) {
        return getContext().getBean(serviceName, clazz);
    }

    /**
     * Gets an instance of the given service.
     * @param serviceName
     * @return getContext().getBean(serviceName)
     */
    public Object getService(String serviceName) {

        return getContext().getBean(serviceName);
    }

    /**
     * Checks wheather the the given service exists
     * @param serviceName
     */
    public boolean containsService(String serviceName) {

        return getContext().containsBean(serviceName);
    }

    /**
     * gets all services of the given type
     * @param clazz
     */
    public <T> Collection<T> getServices(Class<T> clazz) {

        return getContext().getBeansOfType(clazz).values();
    }

    /**
     * The shared instance of this ServiceLocator.
     */
    private static final ServiceLocator instance = new ServiceLocator();

    /**
     * Gets the shared instance of this Class
     *
     * @return the shared service locator instance.
     */
    public static ServiceLocator instance() {
        return instance;
    }


    /**
     * Gets an instance of {@link FutureService}.
     * @return FutureService from getContext().getBean("futureService")
     */
    public FutureService getFutureService() {
        return getContext().getBean("futureService", FutureService.class);
    }

    /**
     * Gets an instance of {@link ManagementService}.
     * @return ManagementService from getContext().getBean("managementService")
     */
    public ManagementService getManagementService() {
        return getContext().getBean("managementService", ManagementService.class);
    }

    /**
     * Gets an instance of {@link LazyLoaderService}.
     * @return LazyLoaderService from getContext().getBean("lazyLoaderService")
     */
    public LazyLoaderService getLazyLoaderService() {
        return getContext().getBean("lazyLoaderService", LazyLoaderService.class);
    }

    /**
     * Gets an instance of {@link PositionService}.
     * @return PositionService from getContext().getBean("positionService")
     */
    public PositionService getPositionService() {
        return getContext().getBean("positionService", PositionService.class);
    }

    /**
     * Gets an instance of {@link HistoricalDataService}.
     * @return HistoricalDataService from getContext().getBean("historicalDataService")
     */
    public HistoricalDataService getHistoricalDataService() {
        return getContext().getBean("historicalDataService", HistoricalDataService.class);
    }

    /**
     * Gets an instance of {@link ReferenceDataService}.
     * @return ReferenceDataService from getContext().getBean("referenceDataService")
     */
    public ReferenceDataService getReferenceDataService() {
        return getContext().getBean("referenceDataService", ReferenceDataService.class);
    }

    /**
     * Gets an instance of {@link OptionService}.
     * @return OptionService from getContext().getBean("optionService")
     */
    public OptionService getOptionService() {
        return getContext().getBean("optionService", OptionService.class);
    }

    /**
     * Gets an instance of {@link LookupService}.
     * @return LookupService from getContext().getBean("lookupService")
     */
    public LookupService getLookupService() {
        return getContext().getBean("lookupService", LookupService.class);
    }

    /**
     * Gets an instance of {@link LocalLookupService}.
     * @return LocalLookupService from getContext().getBean("localLookupService")
     */
    public LocalLookupService getLocalLookupService() {
        return getContext().getBean("localLookupService", LocalLookupService.class);
    }

    /**
     * Gets an instance of {@link MarketDataService}.
     * @return MarketDataService from getContext().getBean("marketDataService")
     */
    public MarketDataService getMarketDataService() {
        return getContext().getBean("marketDataService", MarketDataService.class);
    }

    /**
     * Gets an instance of {@link OrderService}.
     * @return OrderService from getContext().getBean("orderService")
     */
    public OrderService getOrderService() {
        return getContext().getBean("orderService", OrderService.class);
    }

    /**
     * Gets an instance of {@link CombinationService}.
     * @return CombinationService from getContext().getBean("combinationService")
     */
    public CombinationService getCombinationService() {
        return getContext().getBean("combinationService", CombinationService.class);
    }

    /**
     * Gets an instance of {@link SubscriptionService}.
     * @return SubscriptionService from getContext().getBean("subscriptionService")
     */
    public SubscriptionService getSubscriptionService() {
        return getContext().getBean("subscriptionService", SubscriptionService.class);
    }

    /**
     * Gets an instance of {@link MeasurementService}.
     * @return MeasurementService from getContext().getBean("measurementService")
     */
    public MeasurementService getMeasurementService() {
        return getContext().getBean("measurementService", MeasurementService.class);
    }

    /**
     * Gets an instance of {@link PropertyService}.
     * @return PropertyService from getContext().getBean("propertyService")
     */
    public PropertyService getPropertyService() {
        return getContext().getBean("propertyService", PropertyService.class);
    }

    /**
     * Gets an instance of {@link PortfolioService}.
     * @return PortfolioService from getContext().getBean("portfolioService")
     */
    public PortfolioService getPortfolioService() {
        return getContext().getBean("portfolioService", PortfolioService.class);
    }

    /**
     * Gets an instance of {@link ChartProvidingService}.
     * @return ChartProvidingService from getContext().getBean("chartProvidingService")
     */
    public ChartProvidingService getChartProvidingService() {
        return getContext().getBean("chartProvidingService", ChartProvidingService.class);
    }

    /**
     * Gets an instance of {@link PortfolioChartService}.
     * @return PortfolioChartService from getContext().getBean("portfolioChartService")
     */
    public PortfolioChartService getPortfolioChartService() {
        return getContext().getBean("portfolioChartService", PortfolioChartService.class);
    }

    /**
     * Gets an instance of {@link CalendarService}.
     * @return CalendarService from getContext().getBean("calendarService")
     */
    public CalendarService getCalendarService() {
        return getContext().getBean("calendarService", CalendarService.class);
    }

    /**
     * Gets an instance of {@link ch.algotrader.esper.EngineManagerImpl}.
     * @return EngineManager from getContext().getBean("engineManager")
     */
    public EngineManager getEngineManager() {
        return getContext().getBean("engineManager", EngineManager.class);
    }

    /**
     * Gets an instance of {@link ch.algotrader.event.dispatch.EventDispatcher}.
     * @return EngineManager from getContext().getBean("eventDispatcher")
     */
    public EventDispatcher getEventDispatcher() {
        return getContext().getBean("eventDispatcher", EventDispatcher.class);
    }

    /**
     * Gets an instance of {@link ch.algotrader.lifecycle.LifecycleManager}.
     * @return LifecycleManager from getContext().getBean("lifecycleManager")
     */
    public LifecycleManager getLifecycleManager() {
        return getContext().getBean("lifecycleManager", LifecycleManager.class);
    }

    public void runServer() {
        init(SERVER_BEAN_REFERENCE_LOCATION);
        getLifecycleManager().runServer();
    }

    public void runEmbedded() {
        init(EMBEDDED_BEAN_REFERENCE_LOCATION);
        getLifecycleManager().runEmbedded();
    }

    public void runStrategy() {
        init(CLIENT_BEAN_REFERENCE_LOCATION);
        getLifecycleManager().runStrategy();
    }

    public void runServices() {
        init(LOCAL_BEAN_REFERENCE_LOCATION);
        getLifecycleManager().runServices();
    }

}
