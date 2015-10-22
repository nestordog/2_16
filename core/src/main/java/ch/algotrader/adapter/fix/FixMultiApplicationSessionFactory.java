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
package ch.algotrader.adapter.fix;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.Validate;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultSessionFactory;
import quickfix.FieldConvertError;
import quickfix.FileLogFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.SLF4JLogFactory;
import quickfix.ScreenLogFactory;
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
 */
public class FixMultiApplicationSessionFactory implements SessionFactory {

    private static final String APPLICATION_FACTORY = "ApplicationFactory";

    private final Map<String, FixApplicationFactory> applicationFactoryMap;
    private final MessageStoreFactory messageStoreFactory;
    private final MessageFactory messageFactory;

    public FixMultiApplicationSessionFactory(final Map<String, FixApplicationFactory> applicationFactoryMap, final MessageStoreFactory messageStoreFactory, final MessageFactory messageFactory) {

        Validate.notNull(applicationFactoryMap, "FixApplicationFactory map may not be null");
        Validate.notNull(messageStoreFactory, "MessageStoreFactory may not be null");
        Validate.notNull(messageFactory, "MessageFactory may not be null");

        this.applicationFactoryMap = new ConcurrentHashMap<>(applicationFactoryMap);
        this.messageStoreFactory = messageStoreFactory;
        this.messageFactory = messageFactory;
    }

    @Override
    public Session create(final SessionID sessionID, final SessionSettings settings) throws ConfigError {

        final String applicationFactoryName;
        // For backward compatibility see if the session defines "ApplicationFactory" parameter.
        // If not, use session qualifier to look up FixApplicationFactory
        if (settings.isSetting(sessionID, APPLICATION_FACTORY)) {
            applicationFactoryName = settings.getSessionProperties(sessionID).getProperty(APPLICATION_FACTORY);
        } else {
            applicationFactoryName = sessionID.getSessionQualifier();
        }

        // find the application factory by its name
        FixApplicationFactory applicationFactory = this.applicationFactoryMap.get(applicationFactoryName);
        if (applicationFactory == null) {
            throw new FixApplicationException("FixApplicationFactory not found: " + applicationFactoryName);
        }
        String logImpl = "";
        try {
            logImpl = settings.getString(sessionID, "LogImpl");
        } catch (ConfigError | FieldConvertError ignore) {
        }
        if (logImpl != null) {
            logImpl = logImpl.toLowerCase(Locale.ROOT);
        } else {
            logImpl = "";
        }
        LogFactory logFactory;
        switch (logImpl) {
            case "slf4j":
                logFactory = new SLF4JLogFactory(settings);
                break;
            case "file":
                logFactory = new FileLogFactory(settings);
                break;
            case "screen":
                logFactory = new ScreenLogFactory(settings);
                break;
            case "none":
                logFactory = null;
                break;
            default:
                logFactory = new FileLogFactory(settings);
        }
        Application application = applicationFactory.create(sessionID, settings);
        DefaultSessionFactory sessionFactory = new DefaultSessionFactory(application, this.messageStoreFactory, logFactory, this.messageFactory);
        return sessionFactory.create(sessionID, settings);
    }
}

