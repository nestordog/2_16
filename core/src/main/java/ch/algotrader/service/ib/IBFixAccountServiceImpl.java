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
package ch.algotrader.service.ib;

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

import org.apache.commons.lang.Validate;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.entity.Account;
import ch.algotrader.service.AccountServiceImpl;
import ch.algotrader.service.LookupService;
import quickfix.field.FAConfigurationAction;
import quickfix.field.FARequestID;
import quickfix.field.SubMsgType;
import quickfix.field.XMLContent;
import quickfix.fix42.IBFAModification;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@ManagedResource(objectName="ch.algotrader.service.ib:name=IBFixAccountService")
public class IBFixAccountServiceImpl extends AccountServiceImpl implements IBFixAccountService {

    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();
    private GroupMap groups;

    private final FixAdapter fixAdapter;
    private final LookupService lookupService;

    public IBFixAccountServiceImpl(
            final FixAdapter fixAdapter,
            final LookupService lookupService) {

        Validate.notNull(fixAdapter, "FixAdapter is null");
        Validate.notNull(lookupService, "LookupService is null");

        this.fixAdapter = fixAdapter;
        this.lookupService = lookupService;
    }

    @Override
    public long getQuantityByMargin(String strategyName, double initialMarginPerContractInBase) {

        throw new UnsupportedOperationException();
    }

    @Override
    public long getQuantityByAllocation(String strategyName, long requestedQuantity) {

        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Gets all Account Groups of the specified Account.")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "account", description = "account") })
    public Collection<String> getGroups(final String account) {

        Validate.notEmpty(account, "Account is empty");

        try {
            requestGroups(account);

            return new ArrayList<String>(this.groups.keySet());
        } catch (Exception ex) {
            throw new IBFixAccountServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Adds an Account Group to the specified Account.")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "account", description = "account"), @ManagedOperationParameter(name = "group", description = "group"),
            @ManagedOperationParameter(name = "defaultMethod", description = "The default allocation method to be used by this Account Group."),
            @ManagedOperationParameter(name = "initialChildAccount", description = "The first Child Account to add to this Account Group.") })
    public void addGroup(final String account, final String group, final String defaultMethod, final String initialChildAccount) {

        Validate.notEmpty(account, "Account is empty");
        Validate.notEmpty(group, "Group is empty");
        Validate.notEmpty(defaultMethod, "Default method is empty");
        Validate.notEmpty(initialChildAccount, "Initial child account is empty");

        try {
            requestGroups(account);

            Group groupObject = new Group(group, defaultMethod);
            groupObject.add(initialChildAccount);

            this.groups.add(groupObject);

            postGroups(account);
        } catch (Exception ex) {
            throw new IBFixAccountServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Removes an Account Group from the specified Account.")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "account", description = "account"), @ManagedOperationParameter(name = "group", description = "group") })
    public void removeGroup(final String account, final String group) {

        Validate.notEmpty(account, "Account is empty");
        Validate.notEmpty(group, "Group is empty");

        try {
            requestGroups(account);

            this.groups.remove(group);

            postGroups(account);
        } catch (Exception ex) {
            throw new IBFixAccountServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Modifies the default allocation method of an Account Group.")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "account", description = "account"), @ManagedOperationParameter(name = "group", description = "group"),
            @ManagedOperationParameter(name = "defaultMethod", description = "defaultMethod") })
    public void setDefaultMethod(final String account, final String group, final String defaultMethod) {

        Validate.notEmpty(account, "Account is empty");
        Validate.notEmpty(group, "Group is empty");
        Validate.notEmpty(defaultMethod, "Default method is empty");

        try {
            requestGroups(account);

            Group groupObject = this.groups.get(group);

            if (group != null) {
                groupObject.setDefaultMethod(defaultMethod);
            } else {
                throw new IllegalArgumentException("group does not exist " + group);
            }

            postGroups(account);
        } catch (Exception ex) {
            throw new IBFixAccountServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Gets all Child Accounts of an Account Group")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "account", description = "account"), @ManagedOperationParameter(name = "group", description = "group") })
    public Collection<String> getChildAccounts(final String account, final String group) {

        Validate.notEmpty(account, "Account is empty");
        Validate.notEmpty(group, "Group is empty");

        try {
            requestGroups(account);

            Group groupObject = this.groups.get(group);

            if (group != null) {
                return groupObject.getAccounts();
            } else {
                throw new IllegalArgumentException("group does not exist " + group);
            }
        } catch (Exception ex) {
            throw new IBFixAccountServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Adds a Child Account to an Account Group")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "account", description = "account"), @ManagedOperationParameter(name = "group", description = "group"),
            @ManagedOperationParameter(name = "childAccount", description = "childAccount") })
    public void addChildAccount(final String account, final String group, final String childAccount) {

        Validate.notEmpty(account, "Account is empty");
        Validate.notEmpty(group, "Group is empty");
        Validate.notEmpty(childAccount, "Child account is empty");

        try {
            requestGroups(account);

            Group groupObject = this.groups.get(group);

            if (group != null) {
                groupObject.add(childAccount);
            } else {
                throw new IllegalArgumentException("group does not exist " + group);
            }

            postGroups(account);
        } catch (Exception ex) {
            throw new IBFixAccountServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Removes a Child Account from an Account Group")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "account", description = "account"), @ManagedOperationParameter(name = "group", description = "group"),
            @ManagedOperationParameter(name = "childAccount", description = "childAccount") })
    public void removeChildAccount(final String account, final String group, final String childAccount) {

        Validate.notEmpty(account, "Account is empty");
        Validate.notEmpty(group, "Group is empty");
        Validate.notEmpty(childAccount, "Child account is empty");

        try {
            requestGroups(account);

            Group groupObject = this.groups.get(group);

            if (group != null) {
                groupObject.remove(childAccount);
            } else {
                throw new IllegalArgumentException("group does not exist " + group);
            }

            postGroups(account);
        } catch (Exception ex) {
            throw new IBFixAccountServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGroups(final String account, final String xmlContent) {

        Validate.notEmpty(account, "Account is empty");
        Validate.notEmpty(xmlContent, "XML content is empty");

        try {
            JAXBContext context = JAXBContext.newInstance(GroupMap.class);
            Unmarshaller um = context.createUnmarshaller();
            this.groups = (GroupMap) um.unmarshal(new StringReader(xmlContent));

            this.lock.lock();
            try {
                this.condition.signalAll();
            } finally {
                this.lock.unlock();
            }
        } catch (Exception ex) {
            throw new IBFixAccountServiceException(ex.getMessage(), ex);
        }
    }

    private void requestGroups(String accountName) throws Exception {

        IBFAModification faModification = new IBFAModification();
        faModification.set(new SubMsgType(SubMsgType.CONFIG));
        faModification.set(new FAConfigurationAction(FAConfigurationAction.GET_GROUPS));
        faModification.set(new FARequestID(accountName));

        // since call is through JMX/RMI there is not HibernateSession
        Account account = this.lookupService.getAccountByName(accountName);
        if (account == null) {
            throw new IllegalArgumentException("account does not exist " + accountName);
        }

        this.groups = null;
        this.fixAdapter.sendMessage(faModification, account);

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
        Account account = this.lookupService.getAccountByName(accountName);
        if (account == null) {
            throw new IllegalArgumentException("account does not exist " + accountName);
        }

        this.fixAdapter.sendMessage(faModification, account);
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
