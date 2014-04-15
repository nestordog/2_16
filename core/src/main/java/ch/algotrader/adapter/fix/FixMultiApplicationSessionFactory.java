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
package ch.algotrader.adapter.fix;

import java.util.Collection;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang.Validate;
import org.springframework.context.ApplicationContext;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultSessionFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.Session;
import quickfix.SessionFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;

/**
 * Creates a {@link Session} and {@link Application} using the specified {@link FixApplicationFactory} according to the following steps:
 * <ul>
 * <li>lookup the {@link FixApplicationFactory} by its name</li>
 * <li>create an {@link Application}</li>
 * <li>create a {@link DefaultSessionFactory}</li>
 * <li>create a {@link Session}</li>
 * </ul>
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FixMultiApplicationSessionFactory implements SessionFactory {

    private static final String APPLICATION_FACTORY = "ApplicationFactory";

    private final ApplicationContext applicationContext;
    private final MessageStoreFactory messageStoreFactory;
    private final LogFactory logFactory;
    private final MessageFactory messageFactory;

    public FixMultiApplicationSessionFactory(ApplicationContext applicationContext, MessageStoreFactory messageStoreFactory, LogFactory logFactory, MessageFactory messageFactory) {

        Validate.notNull(applicationContext, "ApplicationContext may not be null");
        Validate.notNull(messageStoreFactory, "MessageStoreFactory may not be null");
        Validate.notNull(logFactory, "LogFactory may not be null");
        Validate.notNull(messageFactory, "MessageFactory may not be null");

        this.applicationContext = applicationContext;
        this.messageStoreFactory = messageStoreFactory;
        this.logFactory = logFactory;
        this.messageFactory = messageFactory;
    }

    @Override
    public Session create(SessionID sessionID, SessionSettings settings) throws ConfigError {

        // get all FixApplicationFactories
        Collection<FixApplicationFactory> applicationFactories = this.applicationContext.getBeansOfType(FixApplicationFactory.class).values();

        final String applicationFactoryName;
        if (settings.isSetting(sessionID, APPLICATION_FACTORY)) {
            applicationFactoryName = settings.getSessionProperties(sessionID).getProperty(APPLICATION_FACTORY);
        } else {
            throw new IllegalStateException(APPLICATION_FACTORY + " setting not defined in fix config file");
        }

        // find the application factory by its name
        FixApplicationFactory applicationFactory = CollectionUtils.find(applicationFactories, new Predicate<FixApplicationFactory>(){
            @Override
            public boolean evaluate(FixApplicationFactory applicationFactory) {
                return applicationFactoryName.equals(applicationFactory.getName());
            }});

        Validate.notNull(applicationFactory, "no FixApplicationFactory found for name " + applicationFactoryName);

        Application application = applicationFactory.create(sessionID, settings);
        DefaultSessionFactory sessionFactory = new DefaultSessionFactory(application, this.messageStoreFactory, this.logFactory, this.messageFactory);
        return sessionFactory.create(sessionID, settings);
    }
}

