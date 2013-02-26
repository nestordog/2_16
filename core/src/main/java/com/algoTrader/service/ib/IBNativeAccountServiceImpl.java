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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

import com.algoTrader.util.MyLogger;

public class IBNativeAccountServiceImpl extends IBNativeAccountServiceBase implements DisposableBean {

    private static final long serialVersionUID = -9010045320078819079L;

    private static Logger logger = MyLogger.getLogger(IBNativeAccountServiceImpl.class.getName());

    private @Value("${ib.faEnabled}") boolean faEnabled;

    private @Value("${ib.retrievalTimeout}") int retrievalTimeout;

    private IBClient client;
    private IBDefaultMessageHandler messageHandler;

    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();

    private Map<String, Map<String, String>> allAccountValues;
    private Set<String> accounts;
    private Map<String, Map<String, Double>> profiles;

    private static int clientId = 1;

    @Override
    protected void handleInit() throws java.lang.Exception {

        this.messageHandler = new IBDefaultMessageHandler(clientId) {

            @Override
            public void updateAccountValue(String key, String value, String currency, String accountName) {

                IBNativeAccountServiceImpl.this.lock.lock();

                try {

                    Map<String, String> values = IBNativeAccountServiceImpl.this.allAccountValues.get(accountName);
                    values.put(key, value);

                    IBNativeAccountServiceImpl.this.condition.signalAll();

                } finally {
                    IBNativeAccountServiceImpl.this.lock.unlock();
                }
            }

            @Override
            public void receiveFA(int faDataType, String xml) {

                IBNativeAccountServiceImpl.this.lock.lock();

                try {

                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(new InputSource(new StringReader(xml)));

                    if (faDataType == 1) {

                        // parse the document using XPath
                        NodeIterator iterator = XPathAPI.selectNodeIterator(document, "//Group[name='AllClients']/ListOfAccts/String");

                        // get accounts
                        Node node;
                        IBNativeAccountServiceImpl.this.accounts = new HashSet<String>();
                        while ((node = iterator.nextNode()) != null) {
                            IBNativeAccountServiceImpl.this.accounts.add(node.getFirstChild().getNodeValue());
                        }

                    } else if (faDataType == 2) {

                        // parse the document using XPath
                        NodeIterator profileIterator = XPathAPI.selectNodeIterator(document, "//AllocationProfile");

                        Node profileNode;
                        IBNativeAccountServiceImpl.this.profiles = new HashMap<String, Map<String, Double>>();
                        while ((profileNode = profileIterator.nextNode()) != null) {
                            String name = XPathAPI.selectSingleNode(profileNode, "name/text()").getNodeValue();

                            // get allocations
                            NodeIterator allocationIterator = XPathAPI.selectNodeIterator(profileNode, "ListOfAllocations/Allocation");
                            Map<String, Double> allocations = new HashMap<String, Double>();
                            Node allocationNode;
                            while ((allocationNode = allocationIterator.nextNode()) != null) {
                                String account = XPathAPI.selectSingleNode(allocationNode, "acct/text()").getNodeValue();
                                String amount = XPathAPI.selectSingleNode(allocationNode, "amount/text()").getNodeValue();
                                allocations.put(account, Double.valueOf(amount));
                            }
                            IBNativeAccountServiceImpl.this.profiles.put(name, allocations);
                        }
                    }

                    IBNativeAccountServiceImpl.this.condition.signalAll();

                } catch (Exception e) {
                    IBNativeAccountServiceImpl.logger.error("error parsing fa document", e);
                } finally {
                    IBNativeAccountServiceImpl.this.lock.unlock();
                }
            }

            @Override
            public void connectionClosed() {

                super.connectionClosed();

                IBNativeAccountServiceImpl.this.client.connect();
            }
        };

        this.client = getIBClientFactory().getClient(clientId, this.messageHandler);

        this.allAccountValues = new HashMap<String, Map<String, String>>();
        this.accounts = new HashSet<String>();
        this.profiles = new HashMap<String, Map<String, Double>>();

        this.client.connect();
    }

    private String retrieveAccountValue(String accountName, String currency, String key) throws InterruptedException {

        IBNativeAccountServiceImpl.this.lock.lock();

        try {

            IBNativeAccountServiceImpl.this.allAccountValues.put(accountName, new HashMap<String, String>());

            this.client.reqAccountUpdates(true, accountName);

            while (this.allAccountValues.get(accountName) == null || this.allAccountValues.get(accountName).get(key) == null) {

                if (!this.condition.await(this.retrievalTimeout, TimeUnit.SECONDS)) {
                    throw new IBAccountServiceException("could not get EquityWithLoanValue for account: " + accountName);
                }
            }
        } finally {
            IBNativeAccountServiceImpl.this.lock.unlock();
        }
        return this.allAccountValues.get(accountName).get(key);
    }

    private Set<String> getAccounts() throws Exception {

        if (this.accounts.size() == 0) {

            IBNativeAccountServiceImpl.this.lock.lock();

            try {

                this.client.requestFA(1);

                while (this.accounts.size() == 0) {

                    if (!this.condition.await(this.retrievalTimeout, TimeUnit.SECONDS)) {
                        throw new IBAccountServiceException("could not get FA ");
                    }
                }
            } finally {
                IBNativeAccountServiceImpl.this.lock.unlock();
            }
        }
        return this.accounts;
    }

    private Map<String, Double> getAllocations(String strategyName) throws Exception {

        if (this.profiles.size() == 0) {

            IBNativeAccountServiceImpl.this.lock.lock();

            try {

                this.client.requestFA(2);

                while (this.profiles.size() == 0) {

                    if (!this.condition.await(this.retrievalTimeout, TimeUnit.SECONDS)) {
                        throw new IBAccountServiceException("could not get FA ");
                    }
                }
            } finally {
                IBNativeAccountServiceImpl.this.lock.unlock();
            }
        }

        return this.profiles.get(strategyName.toUpperCase());
    }

    @Override
    protected long handleGetQuantityByMargin(String strategyName, double initialMarginPerContractInBase) throws Exception {

        // if financial advisor is enabled, we have to get the number of Contracts per account
        // in order to avoid fractions
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

        // if financial advisor is enabled, we have to get the number of Contracts per account
        // in order to avoid fractions
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

    @Override
    public void destroy() throws Exception {

        if (this.client != null) {
            this.client.disconnect();
        }
    }
}
