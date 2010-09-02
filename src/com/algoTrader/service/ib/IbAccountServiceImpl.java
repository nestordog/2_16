package com.algoTrader.service.ib;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.InitializingBean;

import com.algoTrader.util.PropertiesUtil;
import com.ib.client.AnyWrapper;
import com.ib.client.EClientSocket;

public class IbAccountServiceImpl extends IbAccountServiceBase implements InitializingBean {

    private static boolean ibEnabled = "IB".equals(PropertiesUtil.getProperty("marketChannel"));
    private static int port = PropertiesUtil.getIntProperty("ib.port");
    private static int retrievalTimeout = PropertiesUtil.getIntProperty("ib.retrievalTimeout");

    private EClientSocket client;
    private Lock lock = new ReentrantLock();
    private Condition accountsCondition = this.lock.newCondition();
    private Condition valuesCondition = this.lock.newCondition();

    private String[] managedAccounts;
    private Map<String, Map<String, String>> allAccountValues = new HashMap<String, Map<String, String>>();

    private static int clientId = 2;

    public void afterPropertiesSet() throws Exception {

        init();
    }

    protected void handleInit() throws java.lang.Exception {

        if (!ibEnabled)
            return;

        AnyWrapper wrapper = new DefaultWrapper() {

            public void managedAccounts(String accountsList) {

                IbAccountServiceImpl.this.lock.lock();

                try {

                    IbAccountServiceImpl.this.managedAccounts = accountsList.split(",");
                    IbAccountServiceImpl.this.accountsCondition.signalAll();

                } finally {
                    IbAccountServiceImpl.this.lock.unlock();
                }
            }

            public void updateAccountValue(String key, String value, String currency, String accountName) {

                IbAccountServiceImpl.this.lock.lock();

                try {

                    Map<String, String> values = IbAccountServiceImpl.this.allAccountValues.get(accountName);
                    values.put(key, value);

                    IbAccountServiceImpl.this.valuesCondition.signalAll();

                } finally {
                    IbAccountServiceImpl.this.lock.unlock();
                }
            }
        };

        this.client = new EClientSocket(wrapper);
        this.client.eConnect(null, port, clientId);
    }

    private String retrieveAccountValue(String accountName, String currency, String key) throws InterruptedException {

        IbAccountServiceImpl.this.lock.lock();

        try {
            while (this.managedAccounts == null) {

                if (!this.accountsCondition.await(retrievalTimeout, TimeUnit.SECONDS)) {
                    throw new IbAccountServiceException("did not receive managedaccounts in time");
                }
            }

            IbAccountServiceImpl.this.allAccountValues.put(accountName, new HashMap<String, String>());

            this.client.reqAccountUpdates(true, accountName);

            while (this.allAccountValues.get(accountName) == null || this.allAccountValues.get(accountName).get(key) == null) {

                if (!this.valuesCondition.await(retrievalTimeout, TimeUnit.SECONDS)) {
                    throw new IbAccountServiceException("could not get EquityWithLoanValue for account: " + accountName);
                }
            }
        } finally {
            IbAccountServiceImpl.this.lock.unlock();
        }
        return this.allAccountValues.get(accountName).get(key);
    }

    protected double handleGetAvailableAmountDouble() throws Exception {

        double availableAmount = 0;
        for (String managedAccount : this.managedAccounts) {

            String equityWithLoanValue = retrieveAccountValue(managedAccount, "CHF", "EquityWithLoanValue");
            availableAmount += Double.parseDouble(equityWithLoanValue);
        }
        return availableAmount;
    }
}
