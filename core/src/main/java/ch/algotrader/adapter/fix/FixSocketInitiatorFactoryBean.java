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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
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

    private final SessionSettings settings;
    private final MessageStoreFactory messageStoreFactory;
    private final LogFactory logFactory;

    private volatile ApplicationContext applicationContext;
    private volatile SocketInitiator socketInitiator;

    public FixSocketInitiatorFactoryBean(final SessionSettings settings, final MessageStoreFactory messageStoreFactory, final LogFactory logFactory) {
        Validate.notNull(settings, "SessionSettings is null");

        this.settings = settings;
        this.messageStoreFactory = messageStoreFactory != null ? messageStoreFactory : new FileStoreFactory(settings);
        this.logFactory = logFactory != null ? logFactory : new CompositeLogFactory(new LogFactory[] { new SLF4JLogFactory(this.settings), new FileLogFactory(this.settings) });
    }

    public FixSocketInitiatorFactoryBean(final SessionSettings settings, final MessageStoreFactory messageStoreFactory) {
        this(settings, messageStoreFactory, null);
    }

    public FixSocketInitiatorFactoryBean(final SessionSettings settings) {
        this(settings, null, null);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public SocketInitiator getObject() throws Exception {

        // get all FixApplicationFactories
        Map<String, FixApplicationFactory> applicationFactoryMap = this.applicationContext.getBeansOfType(FixApplicationFactory.class);
        Map<String, FixApplicationFactory> applicationFactoryMapByName = new HashMap<>(applicationFactoryMap.size());
        for (Map.Entry<String, FixApplicationFactory> entry: applicationFactoryMap.entrySet()) {

            FixApplicationFactory applicationFactory = entry.getValue();
            applicationFactoryMapByName.put(applicationFactory.getName(), applicationFactory);
        }
        SessionFactory sessionFactory = new FixMultiApplicationSessionFactory(applicationFactoryMapByName, this.messageStoreFactory, this.logFactory, new DefaultMessageFactory());
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
