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
package ch.algotrader.adapter.ib;

import java.io.EOFException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EWrapperMsgGenerator;
import com.ib.client.Execution;

import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.HistoricalDataServiceException;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.ib.IBNativeMarketDataService;
import ch.algotrader.util.PriceUtil;

/**
 * Esper specific MessageHandler.
 * Relevant events are sent into the AlgoTrader Server Esper Engine.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public final class DefaultIBMessageHandler extends AbstractIBMessageHandler {

    private static final Logger logger = LogManager.getLogger(DefaultIBMessageHandler.class.getName());
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private int clientId;
    private IBSessionLifecycle fixSessionStateHolder;
    private IBIdGenerator iBIdGenerator;

    private LookupService lookupService;
    private IBNativeMarketDataService marketDataService;

    private BlockingQueue<Bar> historicalDataQueue;

    private BlockingQueue<AccountUpdate> accountUpdateQueue;
    private BlockingQueue<Set<String>> accountsQueue;
    private BlockingQueue<Profile> profilesQueue;

    private BlockingQueue<ContractDetails> contractDetailsQueue;
    private Engine serverEngine;

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void setSessionLifecycle(IBSessionLifecycle fixSessionStateHolder) {
        this.fixSessionStateHolder = fixSessionStateHolder;
    }

    public void setiBIdGenerator(IBIdGenerator iBIdGenerator) {
        this.iBIdGenerator = iBIdGenerator;
    }

    public void setLookupService(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    public void setiBNativeMarketDataService(IBNativeMarketDataService iBNativeMarketDataService) {
        this.marketDataService = iBNativeMarketDataService;
    }

    public void setHistoricalDataQueue(BlockingQueue<Bar> historicalDataQueue) {
        this.historicalDataQueue = historicalDataQueue;
    }

    public void setAccountUpdateQueue(BlockingQueue<AccountUpdate> accountUpdateQueue) {
        this.accountUpdateQueue = accountUpdateQueue;
    }

    public void setAccountsQueue(BlockingQueue<Set<String>> accountsQueue) {
        this.accountsQueue = accountsQueue;
    }

    public void setProfilesQueue(BlockingQueue<Profile> profilesQueue) {
        this.profilesQueue = profilesQueue;
    }

    public void setContractDetailsQueue(BlockingQueue<ContractDetails> contractDetailsQueue) {
        this.contractDetailsQueue = contractDetailsQueue;
    }

    public void setServerEngine(Engine serverEngine) {
        this.serverEngine = serverEngine;
    }

    @Override
    public void execDetails(final int reqId, final Contract contract, final Execution execution) {

        // ignore FA transfer execution reports
        if (execution.m_execId.startsWith("F-") || execution.m_execId.startsWith("U+")) {
            return;
        }

        String intId = String.valueOf(execution.m_orderId);

        // get the order from the OpenOrderWindow
        Order order = this.lookupService.getOpenOrderByIntId(intId);
        if (order == null) {
            logger.error("order could not be found " + intId + " for execution " + contract + " " + execution);
            return;
        }

        // get the fields
        Date extDateTime = IBUtil.getExecutionDateTime(execution);
        Side side = IBUtil.getSide(execution);
        long quantity = execution.m_shares;
        BigDecimal price = PriceUtil.normalizePrice(order, execution.m_price);
        String extExecId = execution.m_execId;

        // assemble the fill
        Fill fill = new Fill();
        fill.setDateTime(new Date());
        fill.setExtDateTime(extDateTime);
        fill.setSide(side);
        fill.setQuantity(quantity);
        fill.setPrice(price);
        fill.setExtId(extExecId);

        // associate the fill with the order
        fill.setOrder(order);

        logger.debug(EWrapperMsgGenerator.execDetails(reqId, contract, execution));

        this.serverEngine.sendEvent(fill);
    }

    @Override
    public void orderStatus(final int orderId, final String statusString, final int filled, final int remaining, final double avgFillPrice, final int permId,
            final int parentId, final double lastFillPrice, final int clientId, final String whyHeld) {

        // get the order from the OpenOrderWindow
        Order order = this.lookupService.getOpenOrderByIntId(String.valueOf(orderId));

        if (order != null) {

            // get the fields
            Status status = IBUtil.getStatus(statusString, filled);
            long filledQuantity = filled;
            long remainingQuantity = remaining;
            String extId = String.valueOf(permId);

            // assemble the IBOrderStatus
            IBOrderStatus orderStatus = new IBOrderStatus(status, filledQuantity, remainingQuantity, avgFillPrice, lastFillPrice, extId, order);

            logger.debug(EWrapperMsgGenerator.orderStatus(orderId, statusString, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld));

            this.serverEngine.sendEvent(orderStatus);
        }
    }

    @Override
    public void tickPrice(final int tickerId, final int field, final double price, final int canAutoExecute) {

        if(logger.isTraceEnabled()) {
            logger.trace(EWrapperMsgGenerator.tickPrice(tickerId, field, price, canAutoExecute));
        }

        TickPrice o = new TickPrice(Integer.toString(tickerId), field, price, canAutoExecute);
        this.serverEngine.sendEvent(o);
    }

    @Override
    public void tickSize(final int tickerId, final int field, final int size) {

        if(logger.isTraceEnabled()) {
            logger.trace(EWrapperMsgGenerator.tickSize(tickerId, field, size));
        }

        TickSize o = new TickSize(Integer.toString(tickerId), field, size);
        this.serverEngine.sendEvent(o);
    }

    @Override
    public void tickString(final int tickerId, final int tickType, final String value) {

        if(logger.isTraceEnabled()) {
            logger.trace(EWrapperMsgGenerator.tickString(tickerId, tickType, value));
        }

        TickString o = new TickString(Integer.toString(tickerId), tickType, value);
        this.serverEngine.sendEvent(o);
    }

    @Override
    public void historicalData(int requestId, String dateString, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {

        Bar bar = Bar.Factory.newInstance();
        if (dateString.startsWith("finished")) {

            this.historicalDataQueue.offer(bar);

            return;
        }

        Date date = null;
        try {
            date = dateTimeFormat.parse(dateString);
        } catch (ParseException e) {
            try {
                date = dateFormat.parse(dateString);
            } catch (ParseException e1) {
                throw new RuntimeException(e1);
            }
        }

        bar.setDateTime(date);
        bar.setOpen(BigDecimal.valueOf(open));
        bar.setHigh(BigDecimal.valueOf(high));
        bar.setLow(BigDecimal.valueOf(low));
        bar.setClose(BigDecimal.valueOf(close));
        bar.setVol(volume);

        this.historicalDataQueue.offer(bar);

        if (hasGaps) {

            // @formatter:off
            String message = "bar with gaps " + dateString +
                    " open=" + open +
                    " high=" + high +
                    " low=" + low +
                    " close=" + close +
                    " volume=" + volume +
                    " count=" + count +
                    " WAP=" + WAP +
                    " hasGaps=" + hasGaps;
            // @formatter:on

            logger.error(message, new HistoricalDataServiceException(message));
        }
    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {

        this.accountUpdateQueue.offer(new AccountUpdate(key, value, currency, accountName));
    }

    @Override
    public void receiveFA(int faDataType, String xml) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            if (faDataType == 1) {

                // parse the document using XPath
                NodeIterator iterator = XPathAPI.selectNodeIterator(document, "//Group[name='AllClients']/ListOfAccts/String");

                // get accounts
                Node node;
                Set<String> accounts = new HashSet<String>();
                while ((node = iterator.nextNode()) != null) {
                    accounts.add(node.getFirstChild().getNodeValue());
                }

                this.accountsQueue.offer(accounts);

            } else if (faDataType == 2) {

                // parse the document using XPath
                NodeIterator profileIterator = XPathAPI.selectNodeIterator(document, "//AllocationProfile");

                Node profileNode;
                while ((profileNode = profileIterator.nextNode()) != null) {
                    String name = XPathAPI.selectSingleNode(profileNode, "name/text()").getNodeValue();
                    Profile profile = new Profile(name);

                    // get allocations
                    NodeIterator allocationIterator = XPathAPI.selectNodeIterator(profileNode, "ListOfAllocations/Allocation");

                    Node allocationNode;
                    while ((allocationNode = allocationIterator.nextNode()) != null) {
                        String account = XPathAPI.selectSingleNode(allocationNode, "acct/text()").getNodeValue();
                        String amount = XPathAPI.selectSingleNode(allocationNode, "amount/text()").getNodeValue();

                        profile.putAllocation(account, Double.valueOf(amount));
                    }

                    this.profilesQueue.offer(profile);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {

        this.contractDetailsQueue.offer(contractDetails);
    }

    @Override
    public void contractDetailsEnd(int reqId) {

        this.contractDetailsQueue.offer(new ContractDetails());
    }

    @Override
    public void connectionClosed() {

        this.fixSessionStateHolder.disconnect();
    }

    @Override
    public void error(Exception e) {

        // we get EOFException and SocketException when TWS is closed
        if (!(e instanceof EOFException || e instanceof SocketException)) {
            logger.error("ib error", e);
        }
    }

    @Override
    public void error(int id, int code, String errorMsg) {

        String message = "client: " + this.clientId + " id: " + id + " code: " + code + " " + errorMsg.replaceAll("\n", " ");

        switch (code) {

        // order related error messages will usually come along with a orderStatus=Inactive
        // which will lead to a cancellation of the GenericOrder. If there is no orderStatus=Inactive
        // coming along, the GenericOrder has to be cancelled by us (potentially creating a "fake" OrderStatus)

            case 104:

                // Can't modify a filled order.
                // do nothing, we modified the order just a little bit too late
                logger.warn(message);
                break;

            case 162:

                // Historical market data Service error message.
                if (this.historicalDataQueue != null) {
                    this.historicalDataQueue.offer(Bar.Factory.newInstance());
                }
                logger.warn(message);
                break;

            case 165:

                // Historical data farm is connected
                logger.debug(message);
                break;

            case 200:

                // No security definition has been found for the request
                orderRejected(id, errorMsg);
                logger.error(message);
                break;

            case 201:

                if ("Cannot cancel the filled order".equals(errorMsg)) {

                    // Cannot cancel the filled order
                    // do nothing, we cancelled the order just a little bit too late
                    logger.warn(message);

                } else {

                    // The exchange is closed
                    // The account does not have trading permissions for this product
                    // No Trading Permission
                    // The maximum order size of xxx is exceeded
                    // The maximum order value of xxx is exceeded
                    // No clearing rule found
                    // etc.
                    orderRejected(id, errorMsg);
                    logger.error(message);
                }
                break;


            case 202:

                // Order cancelled
                if ("Order Canceled - reason:".equals(errorMsg)) {
                    // do nothing, since we cancelled the order ourself
                    logger.debug(message);
                } else {
                    orderRejected(id, errorMsg);
                    logger.error(message);
                }
                break;

            case 399:

                // Order Message: Warning: Your order size is below the EUR 20000 IdealPro minimum and will be routed as an odd lot order.
                // do nothing, this is ok for small FX Orders
                logger.info(message);
                break;

            case 434:

                // The order size cannot be zero
                // This happens in a closing order using PctChange where the percentage is
                // small enough to round to zero for each individual client account
                orderRejected(id, errorMsg);
                logger.info(message);
                break;

            case 502:

                // Couldn't connect to TWS
                this.fixSessionStateHolder.disconnect();
                logger.debug(message);
                break;

            case 1100:

                // Connectivity between IB and TWS has been lost.
                this.fixSessionStateHolder.logoff();
                logger.debug(message);
                break;

            case 1101:

                // Connectivity between IB and TWS has been restored data lost.
                if (this.fixSessionStateHolder.logon(false)) {
                    // initSubscriptions if there is a marketDataService
                    if (this.marketDataService != null) {
                        this.marketDataService.initSubscriptions();
                    }
                }
                logger.debug(message);
                break;

            case 1102:

                // Connectivity between IB and TWS has been restored data maintained.
                if (this.fixSessionStateHolder.logon(true)) {
                    // initSubscriptions if there is a marketDataService
                    if (this.marketDataService != null) {
                        this.marketDataService.initSubscriptions();
                    }
                }
                logger.debug(message);
                break;

            case 2110:

                // Connectivity between TWS and server is broken. It will be restored automatically.
                this.fixSessionStateHolder.logoff();
                logger.debug(message);
                break;

            case 2104:

                // A market data farm is connected.
                if (this.fixSessionStateHolder.logon(true)) {
                    // initSubscriptions if there is a marketDataService
                    if (this.marketDataService != null) {
                        this.marketDataService.initSubscriptions();
                    }
                }
                logger.debug(message);
                break;

            case 2105:

                // 2105 A historical data farm is disconnected.
                if (this.historicalDataQueue != null) {
                    this.historicalDataQueue.offer(Bar.Factory.newInstance());
                }
                logger.warn(message);
                break;

            case 2107:

                // 2107 A historical data farm connection has become inactive
                // but should be available upon demand.
                if (this.historicalDataQueue != null) {
                    this.historicalDataQueue.offer(Bar.Factory.newInstance());
                }
                logger.warn(message);
                break;

            default:
                if (code < 1000) {
                    orderRejected(id, errorMsg);
                    logger.error(message);
                } else {
                    logger.debug(message);
                }
                break;
        }
    }

    @Override
    public void error(String str) {
        logger.error(str, new RuntimeException(str));
    }

    @Override
    public synchronized void nextValidId(final int orderId) {

        if (this.clientId == 0) {
            this.iBIdGenerator.initializeOrderId(orderId);
            logger.debug("client: " + this.clientId + " " + EWrapperMsgGenerator.nextValidId(orderId));
        }
    }

    private void orderRejected(int orderId, String reason) {

        // get the order from the OpenOrderWindow
        Order order = this.lookupService.getOpenOrderByIntId(String.valueOf(orderId));

        if (order != null) {

            // assemble the IBOrderStatus
            IBOrderStatus orderStatus = new IBOrderStatus(Status.REJECTED, 0, order.getQuantity(), null, order, reason);

            this.serverEngine.sendEvent(orderStatus);
        }
    }
}
