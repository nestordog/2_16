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
package ch.algotrader.dao;

import java.util.Collection;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.hibernate.AbstractDao;
import ch.algotrader.hibernate.NamedParam;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Repository // Required for exception translation
public class AccountDaoImpl extends AbstractDao<Account> implements AccountDao {

    public AccountDaoImpl(final SessionFactory sessionFactory) {
        super(AccountImpl.class, sessionFactory);
    }

    @Override
    public Account findByName(final String name) {

        Validate.notEmpty(name, "Account name is empty");

        return findUniqueCaching("Account.findByName", QueryType.BY_NAME, new NamedParam("name", name));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> findActiveSessionsByOrderServiceType(OrderServiceType orderServiceType) {

        Validate.notNull(orderServiceType, "OrderServiceType is null");

        return (Collection<String>) findObjects(null, "Account.findActiveSessionsByOrderServiceType", QueryType.BY_NAME, new NamedParam("orderServiceType", orderServiceType));
    }

}
