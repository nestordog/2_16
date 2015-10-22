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
package ch.algotrader.dao;

import java.util.Collection;
import java.util.List;

import ch.algotrader.entity.Account;

/**
 * DAO for {@link ch.algotrader.entity.Account} objects.
 *
 * @see ch.algotrader.entity.Account
 */
public interface AccountDao extends ReadWriteDao<Account> {

    /**
     * Finds an Account by the specified name.
     * @param name
     * @return Account
     */
    public Account findByName(String name);

    /**
     * Finds all Accounts for the specified order type
     * @param name
     * @return Account
     */
    public List<Account> findByByOrderServiceType(String name);

    /**
     * Finds all active sessions for the specified order type
     * Finds an Account by the specified external account.
     * @param extAccount
     * @return Account
     */
    public Account findByExtAccount(String extAccount);

    /**
     * Finds all active Accounts for the specified order type
     * @param orderServiceType
     * @return Collection<String>
     */
    public Collection<String> findActiveSessionsByOrderServiceType(String orderServiceType);

}