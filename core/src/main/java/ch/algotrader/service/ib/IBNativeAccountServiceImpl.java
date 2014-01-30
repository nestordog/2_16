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
package ch.algotrader.service.ib;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import ch.algotrader.adapter.ib.AccountUpdate;
import ch.algotrader.adapter.ib.Profile;
import ch.algotrader.util.MyLogger;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeAccountServiceImpl extends IBNativeAccountServiceBase {

    private static final Logger logger = MyLogger.getLogger(IBNativeAccountServiceImpl.class.getName());

    private BlockingQueue<AccountUpdate> accountUpdateQueue;
    private BlockingQueue<Set<String>> accountsQueue;
    private BlockingQueue<Profile> profilesQueue;

    public void setAccountUpdateQueue(BlockingQueue<AccountUpdate> accountUpdateQueue) {
        this.accountUpdateQueue = accountUpdateQueue;
    }

    public void setAccountsQueue(BlockingQueue<Set<String>> accountsQueue) {
        this.accountsQueue = accountsQueue;
    }

    public void setProfilesQueue(BlockingQueue<Profile> profilesQueue) {
        this.profilesQueue = profilesQueue;
    }

    @Override
    protected long handleGetQuantityByMargin(String strategyName, double initialMarginPerContractInBase) throws Exception {

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
    }

    @Override
    protected long handleGetQuantityByAllocation(String strategyName, long requestedQuantity) throws Exception {

        long quantityByAllocation = 0;
        StringBuffer buffer = new StringBuffer("quantityByAllocation:");
        for (Map.Entry<String, Double> entry : getAllocations(strategyName).entrySet()) {
            long quantity = (long) (entry.getValue() / 100.0 * requestedQuantity);
            quantityByAllocation += quantity;

            buffer.append(" " + entry.getKey() + "=" + quantity);
        }
        logger.debug(buffer.toString());

        return quantityByAllocation;
    }

    private String retrieveAccountValue(String accountName, String currency, String key) throws InterruptedException {

        getIBSession().reqAccountUpdates(true, accountName);

        while (true) {

            AccountUpdate accountUpdate = this.accountUpdateQueue.take();
            if (accountName.equals(accountUpdate.getAccountName()) && key.equals(accountUpdate.getKey())) {
                return accountUpdate.getValue();
            }
        }
    }

    private Set<String> getAccounts() throws Exception {

        getIBSession().requestFA(1);

        return this.accountsQueue.take();
    }

    private Map<String, Double> getAllocations(String strategyName) throws Exception {

        getIBSession().requestFA(2);

        while (true) {

            Profile profile = this.profilesQueue.take();
            if (strategyName.toUpperCase().equals(profile.getName())) {
                return profile.getAllocations();
            }
        }
    }
}
