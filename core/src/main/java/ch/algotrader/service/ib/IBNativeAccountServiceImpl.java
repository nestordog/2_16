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
import org.apache.log4j.Logger;

import ch.algotrader.adapter.ib.AccountUpdate;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.adapter.ib.Profile;
import ch.algotrader.service.AccountServiceImpl;
import ch.algotrader.util.MyLogger;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeAccountServiceImpl extends AccountServiceImpl implements IBNativeAccountService {

    private static final Logger logger = MyLogger.getLogger(IBNativeAccountServiceImpl.class.getName());

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

        try {
            long quantityByMargin = 0;
            StringBuffer buffer = new StringBuffer("quantityByMargin:");
            for (String account : getAccounts()) {
                double availableAmount = Double.parseDouble(retrieveAccountValue(account, "CHF", "AvailableFunds"));
                long quantity = (long) (availableAmount / initialMarginPerContractInBase);
                quantityByMargin += quantity;

                buffer.append(" " + account + "=" + quantity);
            }
            logger.debug(buffer.toString());

            return quantityByMargin;
        } catch (Exception ex) {
            throw new IBFixAccountServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public long getQuantityByAllocation(String strategyName, long requestedQuantity) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            long quantityByAllocation = 0;
            StringBuffer buffer = new StringBuffer("quantityByAllocation:");
            for (Map.Entry<String, Double> entry : getAllocations(strategyName).entrySet()) {
                long quantity = (long) (entry.getValue() / 100.0 * requestedQuantity);
                quantityByAllocation += quantity;

                buffer.append(" " + entry.getKey() + "=" + quantity);
            }
            logger.debug(buffer.toString());

            return quantityByAllocation;
        } catch (Exception ex) {
            throw new IBFixAccountServiceException(ex.getMessage(), ex);
        }
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

    private Set<String> getAccounts() throws Exception {

        this.iBSession.requestFA(1);

        return this.accountsQueue.take();
    }

    private Map<String, Double> getAllocations(String strategyName) throws Exception {

        this.iBSession.requestFA(2);

        while (true) {

            Profile profile = this.profilesQueue.take();
            if (strategyName.toUpperCase().equals(profile.getName())) {
                return profile.getAllocations();
            }
        }
    }
}
