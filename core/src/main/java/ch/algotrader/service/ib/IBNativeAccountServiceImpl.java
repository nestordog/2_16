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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.ib.AccountUpdate;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.adapter.ib.Profile;
import ch.algotrader.service.ServiceException;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeAccountServiceImpl implements IBNativeAccountService {

    private final BlockingQueue<AccountUpdate> accountUpdateQueue;
    private final BlockingQueue<Set<String>> accountsQueue;
    private final BlockingQueue<Profile> profilesQueue;

    private final IBSession iBSession;

    public IBNativeAccountServiceImpl(final BlockingQueue<AccountUpdate> accountUpdateQueue,
            final BlockingQueue<Set<String>> accountsQueue,
            final BlockingQueue<Profile> profilesQueue,
            final IBSession iBSession) {

        Validate.notNull(accountUpdateQueue, "AccountUpdateQueue is null");
        Validate.notNull(accountsQueue, "AccountsQueue is null");
        Validate.notNull(profilesQueue, "ProfilesQueue is null");
        Validate.notNull(iBSession, "IBSession is null");

        this.accountUpdateQueue = accountUpdateQueue;
        this.accountsQueue = accountsQueue;
        this.profilesQueue = profilesQueue;
        this.iBSession = iBSession;
    }

    @Override
    public String retrieveAccountValue(String accountName, String currency, String key)  {

        try {
            this.iBSession.reqAccountUpdates(true, accountName);

            while (true) {

                AccountUpdate accountUpdate = this.accountUpdateQueue.take();
                if (accountName.equals(accountUpdate.getAccountName()) && key.equals(accountUpdate.getKey()) && currency.equals(accountUpdate.getCurrency())) {
                    return accountUpdate.getValue();
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ServiceException(ex);
        }
    }

    @Override
    public Set<String> getAccounts() {

        try {
            this.iBSession.requestFA(1);

            return this.accountsQueue.take();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ServiceException(ex);
        }
    }

    @Override
    public Map<String, Double> getAllocations(String profileName) {

        try {
            this.iBSession.requestFA(2);

            while (true) {

                Profile profile = this.profilesQueue.take();
                if (profileName.toUpperCase().equals(profile.getName())) {
                    return profile.getAllocations();
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ServiceException(ex);
        }
    }
}
