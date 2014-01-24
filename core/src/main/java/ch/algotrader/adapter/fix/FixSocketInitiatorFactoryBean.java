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
package ch.algotrader.adapter.fix;

import org.quickfixj.jmx.JmxExporter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import quickfix.CompositeLogFactory;
import quickfix.DefaultMessageFactory;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.SLF4JLogFactory;
import quickfix.SessionFactory;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;

/**
 * Factory bean for {@link SocketInitiator}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision: 6665 $ $Date: 2014-01-11 18:17:51 +0100 (Sa, 11 Jan 2014) $
 */
public class FixSocketInitiatorFactoryBean implements FactoryBean<SocketInitiator>, ApplicationContextAware, DisposableBean {

    private SessionSettings settings;
    private ApplicationContext applicationContext;
    private SocketInitiator socketInitiator;

    public void setSessionSettings(SessionSettings settings) {
        this.settings = settings;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public SocketInitiator getObject() throws Exception {

        MessageStoreFactory messageStoreFactory = new FileStoreFactory(this.settings);

//        Log4FIX log4Fix = Log4FIX.createForLiveUpdates(this.settings);
//        LogFactory logFactory = new CompositeLogFactory(new LogFactory[] { new SLF4JLogFactory(this.settings), new FileLogFactory(this.settings), log4Fix.getLogFactory() });
//        log4Fix.show();

        LogFactory logFactory = new CompositeLogFactory(new LogFactory[] { new SLF4JLogFactory(this.settings), new FileLogFactory(this.settings) });

        MessageFactory messageFactory = new DefaultMessageFactory();

        SessionFactory sessionFactory = new FixMultiApplicationSessionFactory(this.applicationContext, messageStoreFactory, logFactory, messageFactory);

        this.socketInitiator = new SocketInitiator(sessionFactory, this.settings);

        JmxExporter exporter = new JmxExporter();
        exporter.register(this.socketInitiator);

        this.socketInitiator.start();

        return this.socketInitiator;
    }

    @Override
    public Class<?> getObjectType() {

        return SocketInitiator.class;
    }

    @Override
    public boolean isSingleton() {

        return true;
    }

    @Override
    public void destroy() throws Exception {

        this.socketInitiator.stop();
    }
}
