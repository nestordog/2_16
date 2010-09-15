package com.algoTrader.service.ib;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;
import com.ib.client.AnyWrapper;
import com.ib.client.EClientSocket;

public class IbAccountServiceImpl extends IbAccountServiceBase implements InitializingBean {

    private static Logger logger = MyLogger.getLogger(IbAccountServiceImpl.class.getName());

    private static boolean simulation = PropertiesUtil.getBooleanProperty("simulation");
    private static boolean ibEnabled = "IB".equals(PropertiesUtil.getProperty("marketChannel"));

    private static int port = PropertiesUtil.getIntProperty("ib.port");
    private static int retrievalTimeout = PropertiesUtil.getIntProperty("ib.retrievalTimeout");
    private static String[] accounts = PropertiesUtil.getProperty("ib.accounts").split("\\s");

    private EClientSocket client;
    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();

    private Map<String, Map<String, String>> allAccountValues = new HashMap<String, Map<String, String>>();

    private static int clientId = 2;

    public void afterPropertiesSet() throws Exception {

        init();
    }

    protected void handleInit() throws java.lang.Exception {

        if (!ibEnabled || simulation)
            return;

        AnyWrapper wrapper = new DefaultWrapper() {

            public void updateAccountValue(String key, String value, String currency, String accountName) {

                IbAccountServiceImpl.this.lock.lock();

                try {

                    Map<String, String> values = IbAccountServiceImpl.this.allAccountValues.get(accountName);
                    values.put(key, value);

                    IbAccountServiceImpl.this.condition.signalAll();

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

            IbAccountServiceImpl.this.allAccountValues.put(accountName, new HashMap<String, String>());

            this.client.reqAccountUpdates(true, accountName);

            while (this.allAccountValues.get(accountName) == null || this.allAccountValues.get(accountName).get(key) == null) {

                if (!this.condition.await(retrievalTimeout, TimeUnit.SECONDS)) {
                    throw new IbAccountServiceException("could not get EquityWithLoanValue for account: " + accountName);
                }
            }
        } finally {
            IbAccountServiceImpl.this.lock.unlock();
        }
        return this.allAccountValues.get(accountName).get(key);
    }

    protected long handleGetNumberOfContractsByMargin(double initialMarginPerContract) throws Exception {

        long numberOfContractsByMargin = 0;
        for (String account : accounts) {
            double availableAmount = Double.parseDouble(retrieveAccountValue(account, "CHF", "AvailableFunds"));
            long numberOfContracts = (long) (availableAmount / initialMarginPerContract);
            numberOfContractsByMargin += numberOfContracts;

            logger.debug("assign " + numberOfContracts + " to account " + account);
        }
        return numberOfContractsByMargin;
    }
}
