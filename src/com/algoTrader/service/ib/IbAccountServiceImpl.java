package com.algoTrader.service.ib;

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;

public class IbAccountServiceImpl extends IbAccountServiceBase implements InitializingBean {

    private static Logger logger = MyLogger.getLogger(IbAccountServiceImpl.class.getName());

    private static boolean simulation = PropertiesUtil.getBooleanProperty("simulation");
    private static boolean ibEnabled = "IB".equals(PropertiesUtil.getProperty("marketChannel"));

    private static int retrievalTimeout = PropertiesUtil.getIntProperty("ib.retrievalTimeout");

    private DefaultClientSocket client;
    private DefaultWrapper wrapper;

    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();

    private Map<String, Map<String, String>> allAccountValues;
    private String fa;

    private static int clientId = 2;

    public void afterPropertiesSet() throws Exception {

        init();
    }

    protected void handleInit() throws java.lang.Exception {

        if (!ibEnabled || simulation)
            return;

        this.wrapper = new DefaultWrapper() {

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

            public void receiveFA(int faDataType, String xml) {

                IbAccountServiceImpl.this.lock.lock();

                try {

                    IbAccountServiceImpl.this.fa = xml;

                    IbAccountServiceImpl.this.condition.signalAll();

                } finally {
                    IbAccountServiceImpl.this.lock.unlock();
                }
            }

            public void connectionClosed() {

                super.connectionClosed();

                connect();
            }
        };

        this.client = new DefaultClientSocket(this.wrapper);

        connect();
    }

    private void connect() {

        this.allAccountValues = new HashMap<String, Map<String, String>>();

        this.client.connect(clientId);
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

    private Set<String> getAccounts() throws Exception {

        IbAccountServiceImpl.this.lock.lock();

        try {

            IbAccountServiceImpl.this.fa = null;

            this.client.requestFA(1);

            while (this.fa == null) {

                if (!this.condition.await(retrievalTimeout, TimeUnit.SECONDS)) {
                    throw new IbAccountServiceException("could not get FA ");
                }
            }
        } finally {
            IbAccountServiceImpl.this.lock.unlock();
        }

        // get the xml-document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(this.fa)));

        // parse the document using XPath
        NodeIterator iterator = XPathAPI.selectNodeIterator(document, "//Group[name='AllClients']/ListOfAccts/String");
        Node node;
        Set<String> accounts = new HashSet<String>();
        while ((node = iterator.nextNode()) != null) {
            accounts.add(node.getFirstChild().getNodeValue());
        }

        return accounts;
    }

    protected long handleGetNumberOfContractsByMargin(double initialMarginPerContract) throws Exception {

        long numberOfContractsByMargin = 0;
        for (String account : getAccounts()) {
            double availableAmount = Double.parseDouble(retrieveAccountValue(account, "CHF", "AvailableFunds"));
            long numberOfContracts = (long) (availableAmount / initialMarginPerContract);
            numberOfContractsByMargin += numberOfContracts;

            logger.debug("assign " + numberOfContracts + " to account " + account);
        }
        return numberOfContractsByMargin;
    }
}
