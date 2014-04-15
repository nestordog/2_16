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

import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.util.spring.Configuration;

/**
 * Locates and provides all available application services.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractServiceLocator {

    // The default bean reference factory ID, referencing beanRefFactory.
    private static final String DEFAULT_BEAN_REFERENCE_ID = "beanRefFactory";

    // The different bean reference types
    public static final String LOCAL_BEAN_REFERENCE_LOCATION = "Local";
    public static final String SINGLE_BEAN_REFERENCE_LOCATION = "Single";
    public static final String SERVER_BEAN_REFERENCE_LOCATION = "Server";
    public static final String CLIENT_BEAN_REFERENCE_LOCATION = "Client";
    public static final String SIMULATION_BEAN_REFERENCE_LOCATION = "Simulation";

    // The bean factory reference instance.
    private BeanFactoryReference beanFactoryReference;

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
    }


    /**
     * Gets the Spring ApplicationContext.
     * @return beanFactoryReference.getFactory()
     */
    public synchronized ApplicationContext getContext() {

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
     * Gets the Configuration
     * @return getContext().getBean("configuration")
     */
    public final Configuration getConfiguration() {

        return getContext().getBean("configuration", Configuration.class);
    }

    /**
     * Gets an instance of the given service.
     * @param serviceName
     * @param clazz
     * @return getContext().getBean(serviceName, clazz)
     */
    public final <T> T getService(String serviceName, Class<T> clazz) {
        return getContext().getBean(serviceName, clazz);
    }

    /**
     * Gets an instance of the given service.
     * @param serviceName
     * @return getContext().getBean(serviceName)
     */
    public final Object getService(String serviceName) {

        return getContext().getBean(serviceName);
    }

    /**
     * Checks wheather the the given service exists
     * @param serviceName
     */
    public final boolean containsService(String serviceName) {

        return getContext().containsBean(serviceName);
    }

    /**
     * gets all services of the given type
     * @param clazz
     */
    public final <T> Collection<T> getServices(Class<T> clazz) {

        return getContext().getBeansOfType(clazz).values();
    }

    /**
     * gets all service names
     */
    public final String[] getServiceNames() {

        return getContext().getBeanDefinitionNames();
    }

    /**
     * calls the init method of all services that implement the {@link InitializingServiceI} interface
     */
    public final void initInitializingServices() {

        for (InitializingServiceI service : getServices(InitializingServiceI.class)) {
            service.init();
        }
    }
}
