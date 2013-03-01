/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.service.fix;

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
 * Creates a new {@link quickfix.Application} for each session using the
 * specified ApplicationFactory.
 */
/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FixMultiApplicationSessionFactory implements SessionFactory {

    private final FixApplicationFactory applicationFactory;
    private final MessageStoreFactory messageStoreFactory;
    private final LogFactory logFactory;
    private final MessageFactory messageFactory;

    public FixMultiApplicationSessionFactory(FixApplicationFactory applicationFactory, MessageStoreFactory messageStoreFactory, LogFactory logFactory, MessageFactory messageFactory) {

        this.applicationFactory = applicationFactory;
        this.messageStoreFactory = messageStoreFactory;
        this.logFactory = logFactory;
        this.messageFactory = messageFactory;
    }

    @Override
    public Session create(SessionID sessionID, SessionSettings settings) throws ConfigError {

        Application application = this.applicationFactory.create(sessionID);
        return new DefaultSessionFactory(application, this.messageStoreFactory, this.logFactory, this.messageFactory).create(sessionID, settings);
    }
}

