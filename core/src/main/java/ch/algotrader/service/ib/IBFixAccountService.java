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

import java.util.Collection;

import ch.algotrader.service.AccountService;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface IBFixAccountService extends AccountService {

    /**
     * Gets all Account Groups of the specified Account.
     */
    public Collection<String> getGroups(String account);

    /**
     * Adds an Account Group to the specified Account.
     * @param defaultMethod The default allocation method to be used by this Account Group.
     * @param initialChildAccount The first Child Account to add to this Account Group.
     */
    public void addGroup(String account, String group, String defaultMethod, String initialChildAccount);

    /**
     * Removes an Account Group from the specified Account.
     */
    public void removeGroup(String account, String group);

    /**
     * Modifies the default allocation method of an Account Group.
     */
    public void setDefaultMethod(String account, String group, String defaultMethod);

    /**
     * Gets all Child Accounts of an Account Group
     */
    public Collection<String> getChildAccounts(String account, String group);

    /**
     * Adds a Child Account to an Account Group
     */
    public void addChildAccount(String account, String group, String childAccount);

    /**
     * Removes a Child Account from an Account Group
     */
    public void removeChildAccount(String account, String group, String childAccount);

    /**
     * Called by the IBFixMessageHandler to process returning XML content.
     */
    public void updateGroups(String account, String xmlContent);

}
