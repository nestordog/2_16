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
package com.algoTrader.service.ib;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import quickfix.field.FAConfigurationAction;
import quickfix.field.FARequestID;
import quickfix.field.SubMsgType;
import quickfix.field.XMLContent;
import quickfix.fix42.IBFAModification;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.strategy.Account;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBFixAccountServiceImpl extends IBFixAccountServiceBase {

    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();
    private GroupMap groups;

    @Override
    protected long handleGetQuantityByMargin(String strategyName, double initialMarginPerContractInBase) throws Exception {

        throw new UnsupportedOperationException();
    }

    @Override
    protected long handleGetQuantityByAllocation(String strategyName, long requestedQuantity) throws Exception {

        throw new UnsupportedOperationException();
    }

    @Override
    protected Collection<String> handleGetGroups(String accountName) throws Exception {

        requestGroups(accountName);

        return new ArrayList<String>(this.groups.keySet());
    }

    @Override
    protected void handleAddGroup(String accountName, String groupName, String defaultMethod, String initialChildAccount) throws Exception {

        requestGroups(accountName);

        Group group = new Group(groupName, defaultMethod);
        group.add(initialChildAccount);

        this.groups.add(group);

        postGroups(accountName);
    }

    @Override
    protected void handleRemoveGroup(String accountName, String groupName) throws Exception {

        requestGroups(accountName);

        this.groups.remove(groupName);

        postGroups(accountName);
    }

    @Override
    protected void handleSetDefaultMethod(String accountName, String groupName, String defaultMethod) throws Exception {

        requestGroups(accountName);

        Group group = this.groups.get(groupName);

        if (group != null) {
            group.setDefaultMethod(defaultMethod);
        } else {
            throw new IllegalArgumentException("group does not exist " + groupName);
        }

        postGroups(accountName);
    }

    @Override
    protected Collection<String> handleGetChildAccounts(String accountName, String groupName) throws Exception {

        requestGroups(accountName);

        Group group = this.groups.get(groupName);

        if (group != null) {
            return group.getAccounts();
        } else {
            throw new IllegalArgumentException("group does not exist " + groupName);
        }
    }

    @Override
    protected void handleAddChildAccount(String accountName, String groupName, String childAccount) throws Exception {

        requestGroups(accountName);

        Group group = this.groups.get(groupName);

        if (group != null) {
            group.add(childAccount);
        } else {
            throw new IllegalArgumentException("group does not exist " + groupName);
        }

        postGroups(accountName);
    }

    @Override
    protected void handleRemoveChildAccount(String accountName, String groupName, String childAccount) throws Exception {

        requestGroups(accountName);

        Group group = this.groups.get(groupName);

        if (group != null) {
            group.remove(childAccount);
        } else {
            throw new IllegalArgumentException("group does not exist " + groupName);
        }

        postGroups(accountName);
    }

    /**
     * called by the IBFixMessageHandler
     */
    @Override
    protected void handleUpdateGroups(String account, String xmlContent) throws Exception {

        JAXBContext context = JAXBContext.newInstance(GroupMap.class);
        Unmarshaller um = context.createUnmarshaller();
        this.groups = (GroupMap) um.unmarshal(new StringReader(xmlContent));

        this.lock.lock();
        try {
            this.condition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    private void requestGroups(String accountName) throws Exception {

        IBFAModification faModification = new IBFAModification();
        faModification.set(new SubMsgType(SubMsgType.CONFIG));
        faModification.set(new FAConfigurationAction(FAConfigurationAction.GET_GROUPS));
        faModification.set(new FARequestID(accountName));

        // since call is through JMX/RMI there is not HibernateSession
        Account account = ServiceLocator.instance().getLookupService().getAccountByName(accountName);
        if (account == null) {
            throw new IllegalArgumentException("account does not exist " + accountName);
        }

        this.groups = null;

        getFixClient().sendMessage(faModification, account);

        this.lock.lock();
        try {
            while (this.groups == null) {
                this.condition.await();
            }
        } finally {
            this.lock.unlock();
        }
    }

    private void postGroups(String accountName) throws Exception {

        IBFAModification faModification = new IBFAModification();
        faModification.set(new SubMsgType(SubMsgType.CONFIG));
        faModification.set(new FAConfigurationAction(FAConfigurationAction.REPLACE_GROUPS));
        faModification.set(new FARequestID(accountName));

        JAXBContext context = JAXBContext.newInstance(GroupMap.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter writer = new StringWriter();
        m.marshal(this.groups, writer);

        faModification.set(new XMLContent(writer.toString()));

        // since call is through JMX/RMI there is not HibernateSession
        Account account = ServiceLocator.instance().getLookupService().getAccountByName(accountName);
        if (account == null) {
            throw new IllegalArgumentException("account does not exist " + accountName);
        }

        getFixClient().sendMessage(faModification, account);
    }

    @XmlRootElement(name = "ListOfGroups")
    public static class GroupMap extends HashMap<String, Group> {

        private static final long serialVersionUID = -8942056572059143696L;

        @XmlElement(name = "Group")
        public Collection<Group> getGroups() {
            return new ArrayList<Group>(this.values());
        }

        public void setGroups(Collection<Group> groups) {
            for (Group group : groups) {
                this.put(group.getName(), group);
            }
        }

        public void add(Group group) {
            this.put(group.getName(), group);
        }
    }

    @XmlRootElement
    public static class Group extends ArrayList<String> {

        private static final long serialVersionUID = 6344242495952978485L;

        private String name;
        private String defaultMethod;

        public Group() {
        }

        public Group(String name, String defaultMethod) {
            this.name = name;
            this.defaultMethod = defaultMethod;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDefaultMethod() {
            return this.defaultMethod;
        }

        public void setDefaultMethod(String defaultMethod) {
            this.defaultMethod = defaultMethod;
        }

        @XmlElementWrapper(name = "ListOfAccts")
        @XmlElement(name = "Acct")
        public List<String> getAccounts() {
            return this;
        }
    }
}
