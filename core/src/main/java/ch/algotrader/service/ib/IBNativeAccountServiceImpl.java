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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.ib.AccountUpdate;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.adapter.ib.Profile;
import ch.algotrader.service.AccountServiceImpl;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeAccountServiceImpl extends AccountServiceImpl implements IBNativeAccountService {

    private static final Logger logger = LogManager.getLogger(IBNativeAccountServiceImpl.class.getName());

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
    public long getQuantityByMargin(String strategyName, double initialMarginPerContractInBase) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        long quantityByMargin = 0;
        StringBuffer buffer = new StringBuffer("quantityByMargin:");

        try {
            for (String account : getAccounts()) {
                double availableAmount = Double.parseDouble(retrieveAccountValue(account, "CHF", "AvailableFunds"));
                long quantity = (long) (availableAmount / initialMarginPerContractInBase);
                quantityByMargin += quantity;

                buffer.append(" " + account + "=" + quantity);
            }
        } catch (NumberFormatException ex) {
            throw new IBFixAccountServiceException(ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IBFixAccountServiceException(ex);
        }

        logger.debug(buffer.toString());

        return quantityByMargin;

    }

    @Override
    public long getQuantityByAllocation(String strategyName, long requestedQuantity) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        long quantityByAllocation = 0;
        StringBuffer buffer = new StringBuffer("quantityByAllocation:");

        try {
            for (Map.Entry<String, Double> entry : getAllocations(strategyName).entrySet()) {
                long quantity = (long) (entry.getValue() / 100.0 * requestedQuantity);
                quantityByAllocation += quantity;

                buffer.append(" " + entry.getKey() + "=" + quantity);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IBFixAccountServiceException(ex);
        }

        logger.debug(buffer.toString());

        return quantityByAllocation;

    }

    private String retrieveAccountValue(String accountName, String currency, String key) throws InterruptedException {

        this.iBSession.reqAccountUpdates(true, accountName);

        while (true) {

            AccountUpdate accountUpdate = this.accountUpdateQueue.take();
            if (accountName.equals(accountUpdate.getAccountName()) && key.equals(accountUpdate.getKey())) {
                return accountUpdate.getValue();
            }
        }
    }

    private Set<String> getAccounts() throws InterruptedException {

        this.iBSession.requestFA(1);

        return this.accountsQueue.take();
    }

    private Map<String, Double> getAllocations(String strategyName) throws InterruptedException {

        this.iBSession.requestFA(2);

        while (true) {

            Profile profile = this.profilesQueue.take();
            if (strategyName.toUpperCase().equals(profile.getName())) {
                return profile.getAllocations();
            }
        }
    }
}
