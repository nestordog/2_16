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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

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
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.Engine;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.service.ExternalServiceException;
import ch.algotrader.util.DateTimeLegacy;
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

    private final static AtomicLong MSG_SEQ = new AtomicLong(0);

    private static final Logger LOGGER = LogManager.getLogger(DefaultIBMessageHandler.class);
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd  HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final int clientId;
    private final IBSessionStateHolder sessionStateHolder;
    private final IBPendingRequests pendingRequests;
    private final IBIdGenerator idGenerator;

    private final OrderRegistry orderRegistry;
    private final IBExecutions executions;

    private final BlockingQueue<AccountUpdate> accountUpdateQueue;
    private final BlockingQueue<Set<String>> accountsQueue;
    private final BlockingQueue<Profile> profilesQueue;
    private final Engine serverEngine;

    public DefaultIBMessageHandler(
            final int clientId,
            final IBSessionStateHolder sessionStateHolder,
            final IBPendingRequests pendingRequests,
            final IBIdGenerator idGenerator,
            final OrderRegistry orderRegistry,
            final IBExecutions executions,
            final BlockingQueue<AccountUpdate> accountUpdateQueue,
            final BlockingQueue<Set<String>> accountsQueue,
            final BlockingQueue<Profile> profilesQueue,
            final Engine serverEngine) {
        this.clientId = clientId;
        this.sessionStateHolder = sessionStateHolder;
        this.pendingRequests = pendingRequests;
        this.idGenerator = idGenerator;
        this.orderRegistry = orderRegistry;
        this.executions = executions;
        this.accountUpdateQueue = accountUpdateQueue;
        this.accountsQueue = accountsQueue;
        this.profilesQueue = profilesQueue;
        this.serverEngine = serverEngine;
    }

    @Override
    public void execDetails(final int reqId, final Contract contract, final Execution execution) {

        // ignore FA transfer execution reports
        if (execution.m_execId.startsWith("F-") || execution.m_execId.startsWith("U+")) {
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(EWrapperMsgGenerator.execDetails(reqId, contract, execution));
        }

        String intId = String.valueOf(execution.m_orderId);

        // get the order from the OpenOrderWindow
        Order order = this.orderRegistry.getOpenOrderByIntId(intId);
        if (order == null) {
            LOGGER.error("Order with IntId {} could not be found for execution {} {}", intId, contract, execution);
            return;
        }

        OrderStatus orderStatus = null;
        IBExecution executionEntry = this.executions.get(intId);
        synchronized (executionEntry) {
            executionEntry.setLastQuantity(execution.m_shares);
            if (executionEntry.getStatus() == Status.OPEN) {

                executionEntry.setStatus(Status.SUBMITTED);

                orderStatus = OrderStatus.Factory.newInstance();
                orderStatus.setStatus(Status.SUBMITTED);
                orderStatus.setExtId(Integer.toString(execution.m_permId));
                orderStatus.setIntId(intId);
                orderStatus.setSequenceNumber(MSG_SEQ.incrementAndGet());
                orderStatus.setFilledQuantity(0L);
                orderStatus.setRemainingQuantity(order.getQuantity());
                orderStatus.setLastQuantity(0L);
                orderStatus.setOrder(order);
                orderStatus.setExtDateTime(this.serverEngine.getCurrentTime());

            }
        }

        if (orderStatus != null) {
            this.serverEngine.sendEvent(orderStatus);
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

        this.serverEngine.sendEvent(fill);
    }

    @Override
    public void orderStatus(final int reqId, final String statusString, final int filled, final int remaining, final double avgFillPrice, final int permId,
            final int parentId, final double lastFillPrice, final int clientId, final String whyHeld) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(EWrapperMsgGenerator.orderStatus(reqId, statusString, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld));
        }

        Status status = IBUtil.getStatus(statusString, filled);
        if (status == Status.REJECTED) {
            // Reject status needs to be processed from #handleError method in order to get the reason message
            return;
        }

        String intId = String.valueOf(reqId);

        long lastQuantity = 0L;
        boolean statusUpdate = false;

        IBExecution executionEntry = this.executions.get(intId);
        synchronized (executionEntry) {

            if (executionEntry.getStatus() != status
                    || executionEntry.getFilledQuantity() != filled
                    || executionEntry.getRemainingQuantity() != remaining) {

                statusUpdate = true;
                lastQuantity = executionEntry.getLastQuantity();

                executionEntry.setStatus(status);
                executionEntry.setLastQuantity(0L);
                executionEntry.setFilledQuantity(filled);
                executionEntry.setRemainingQuantity(remaining);
            }
        }

        if (statusUpdate) {
            Order order = this.orderRegistry.getOpenOrderByIntId(intId);
            if (order != null) {

                OrderStatus orderStatus = OrderStatus.Factory.newInstance();
                orderStatus.setStatus(status);
                orderStatus.setExtId(String.valueOf(permId));
                orderStatus.setIntId(intId);
                orderStatus.setSequenceNumber(MSG_SEQ.incrementAndGet());
                orderStatus.setFilledQuantity(filled);
                orderStatus.setRemainingQuantity(remaining);
                orderStatus.setLastQuantity(lastQuantity);
                orderStatus.setOrder(order);
                orderStatus.setExtDateTime(this.serverEngine.getCurrentTime());
                if (lastFillPrice != 0.0) {
                    orderStatus.setLastPrice(PriceUtil.normalizePrice(order, lastFillPrice));
                }
                if (avgFillPrice != 0.0) {
                    orderStatus.setAvgPrice(PriceUtil.normalizePrice(order, avgFillPrice));
                }

                this.serverEngine.sendEvent(orderStatus);
            }
        }
    }

    @Override
    public void tickPrice(final int tickerId, final int field, final double price, final int canAutoExecute) {

        if(LOGGER.isTraceEnabled()) {
            LOGGER.trace(EWrapperMsgGenerator.tickPrice(tickerId, field, price, canAutoExecute));
        }

        TickPriceVO o = new TickPriceVO(Integer.toString(tickerId), field, price, canAutoExecute);
        this.serverEngine.sendEvent(o);
    }

    @Override
    public void tickSize(final int tickerId, final int field, final int size) {

        if(LOGGER.isTraceEnabled()) {
            LOGGER.trace(EWrapperMsgGenerator.tickSize(tickerId, field, size));
        }

        TickSizeVO o = new TickSizeVO(Integer.toString(tickerId), field, size);
        this.serverEngine.sendEvent(o);
    }

    @Override
    public void tickString(final int tickerId, final int tickType, final String value) {

        if(LOGGER.isTraceEnabled()) {
            LOGGER.trace(EWrapperMsgGenerator.tickString(tickerId, tickType, value));
        }

        TickStringVO o = new TickStringVO(Integer.toString(tickerId), tickType, value);
        this.serverEngine.sendEvent(o);
    }

    @Override
    public void historicalData(int requestId, String dateString, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {

        IBPendingRequest<Bar> pendingHistoricDataRequest = this.pendingRequests.getHistoricDataRequest(requestId);
        if (pendingHistoricDataRequest == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Unexpected historic data request id: " + requestId);
            }
            return;
        }
        if (dateString.startsWith("finished")) {

            this.pendingRequests.removeHistoricDataRequest(requestId);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Historic data request completed; request id = " + requestId);
            }
            pendingHistoricDataRequest.completed();
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Historic data; request id = " + requestId + " (" + dateString + ")");
        }

        Bar bar = Bar.Factory.newInstance();

        Date date;
        try {
            date = DateTimeLegacy.parseAsLocalDateTime(dateString, DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            try {
                date = DateTimeLegacy.parseAsLocalDateTime(dateString, DATE_FORMAT);
            } catch (DateTimeParseException e1) {
                throw new IBSessionException(-1, "Invalid date attribute: " + dateString);
            }
        }

        bar.setDateTime(date);
        bar.setOpen(BigDecimal.valueOf(open));
        bar.setHigh(BigDecimal.valueOf(high));
        bar.setLow(BigDecimal.valueOf(low));
        bar.setClose(BigDecimal.valueOf(close));
        bar.setVol(volume);

        pendingHistoricDataRequest.add(bar);

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

            LOGGER.error(message);
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
                Set<String> accounts = new HashSet<>();
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
            throw new ExternalServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {

        IBPendingRequest<ContractDetails> pendingContractRequest = this.pendingRequests.getContractDetailRequest(reqId);
        if (pendingContractRequest == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Unexpected contract detail request id: " + reqId);
            }
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Contract details; request id = " + reqId +
                    " (" + contractDetails.m_longName + ", " + contractDetails.m_contractMonth + ", " + contractDetails.m_summary.m_conId + ")");
        }
        pendingContractRequest.add(contractDetails);
    }

    @Override
    public void contractDetailsEnd(int reqId) {

        IBPendingRequest<ContractDetails> pendingContractRequest = this.pendingRequests.removeContractDetailRequest(reqId);
        if (pendingContractRequest == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Unexpected contract detail request id: " + reqId);
            }
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Contract detail request completed; request id = " + reqId);
        }
        pendingContractRequest.completed();
    }

    @Override
    public void connectionClosed() {

        // IB client executes this notification from an interrupted thread, which prevents
        // execution of potentially blocking I/O operations such as publishing to a JMS queue
        // This makes it necessary to execute #onDisconnect() event on a separate thread
        final Thread disposableThread = new Thread(null, this.sessionStateHolder::onDisconnect, "IB-disconnect-thread");
        disposableThread.setDaemon(true);
        disposableThread.start();
    }

    @Override
    public void error(Exception e) {

        // we get EOFException and SocketException when TWS is closed
        if (!(e instanceof EOFException || e instanceof SocketException)) {
            LOGGER.error("ib error", e);
        }
    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {
        try {
            handleError(id, errorCode, errorMsg);
        } finally {
            IBPendingRequest<?> pendingRequest = this.pendingRequests.removeRequest(id);
            if (pendingRequest != null) {
                pendingRequest.fail(new IBSessionException(errorCode, errorMsg));
            }
        }
    }

    private void handleError(int id, int code, String errorMsg) {
        String message = "client: " + this.clientId + "; request id: " + id + "; error code: " + code +
                "; error message: " + errorMsg.replaceAll("\n", " ");

        switch (code) {

        // order related error messages will usually come along with a orderStatus=Inactive
        // which will lead to a cancellation of the GenericOrder. If there is no orderStatus=Inactive
        // coming along, the GenericOrder has to be cancelled by us (potentially creating a "fake" OrderStatus)

            case 104:

                // Can't modify a filled order.
                // do nothing, we modified the order just a little bit too late
                LOGGER.warn(message);
                break;

            case 161:

                // Cancel attempted when order is not in a cancellable state
                // do nothing, we cancelled the order just a little bit too late
                LOGGER.warn(message);
                break;

            case 162:

                LOGGER.warn(message);
                break;

            case 165:

                // Historical data farm is connected
                LOGGER.debug(message);
                break;

            case 200:

                // No security definition has been found for the request
                orderRejected(id, errorMsg);
                LOGGER.error(message);
                break;

            case 201:

                if (errorMsg.contains("Order rejected - reason:Cannot cancel the filled order")) {

                    // Cannot cancel the filled order
                    // do nothing, we cancelled the order just a little bit too late
                    LOGGER.warn(message);

                } else {

                    // The exchange is closed
                    // The account does not have trading permissions for this product
                    // No Trading Permission
                    // The maximum order size of xxx is exceeded
                    // The maximum order value of xxx is exceeded
                    // No clearing rule found
                    // etc.
                    orderRejected(id, errorMsg);
                    LOGGER.error(message);
                }
                break;


            case 202:

                // Order cancelled
                if (errorMsg.contains("Order Canceled - reason:")) {
                    // do nothing, since we cancelled the order ourself
                    LOGGER.debug(message);
                } else {
                    orderRejected(id, errorMsg);
                    LOGGER.error(message);
                }
                break;

            case 399:

                // Order Message: Warning: Your order size is below the EUR 20000 IdealPro minimum and will be routed as an odd lot order.
                // do nothing, this is ok for small FX Orders
                LOGGER.info(message);
                break;

            case 434:

                // The order size cannot be zero
                // This happens in a closing order using PctChange where the percentage is
                // small enough to round to zero for each individual client account
                orderRejected(id, errorMsg);
                LOGGER.info(message);
                break;

            case 502:

                // Couldn't onConnect to TWS
                this.sessionStateHolder.onDisconnect();
                LOGGER.debug(message);
                break;

            case 1100:

                // Connectivity between IB and TWS has been lost.
                this.sessionStateHolder.onLogoff();
                LOGGER.debug(message);
                break;

            case 1101:

                // Connectivity between IB and TWS has been restored data lost.
                this.sessionStateHolder.onLogon(false);
                LOGGER.debug(message);
                break;

            case 1102:

                // Connectivity between IB and TWS has been restored data maintained.
                this.sessionStateHolder.onLogon(true);
                LOGGER.debug(message);
                break;

            case 2110:

                // Connectivity between TWS and server is broken. It will be restored automatically.
                this.sessionStateHolder.onLogoff();
                LOGGER.debug(message);
                break;

            case 2104:

                // A market data farm is connected.
                this.sessionStateHolder.onLogon(true);
                LOGGER.debug(message);
                break;

            case 2105:

                // 2105 A historical data farm is disconnected.
                LOGGER.warn(message);
                break;

            case 2107:

                // 2107 A historical data farm connection has become inactive
                // but should be available upon demand.
                LOGGER.warn(message);
                break;

            default:
                if (code < 1000) {
                    orderRejected(id, errorMsg);
                    LOGGER.error(message);
                } else {
                    LOGGER.debug(message);
                }
                break;
        }
    }

    @Override
    public void error(String str) {
        LOGGER.error(str, new RuntimeException(str));
    }

    @Override
    public synchronized void nextValidId(final int orderId) {

        if (this.clientId == 0) {
            this.idGenerator.initializeOrderId(orderId);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("client: {} {}", this.clientId, EWrapperMsgGenerator.nextValidId(orderId));
            }
        }
    }

    private void orderRejected(int orderId, String reason) {

        String intId = String.valueOf(orderId);
        Order order = this.orderRegistry.getOpenOrderByIntId(intId);

        if (order != null) {

            IBExecution executionEntry = this.executions.get(intId);
            OrderStatus orderStatus = null;
            synchronized (executionEntry) {
                if (executionEntry.getStatus() != Status.REJECTED) {

                    executionEntry.setStatus(Status.REJECTED);

                    orderStatus = OrderStatus.Factory.newInstance();
                    orderStatus.setFilledQuantity(executionEntry.getFilledQuantity());
                    orderStatus.setRemainingQuantity(executionEntry.getRemainingQuantity());
                    orderStatus.setStatus(Status.REJECTED);
                    orderStatus.setIntId(intId);
                    orderStatus.setSequenceNumber(MSG_SEQ.incrementAndGet());
                    orderStatus.setOrder(order);
                    orderStatus.setExtDateTime(this.serverEngine.getCurrentTime());
                    orderStatus.setReason(reason);

                }
            }

            if (orderStatus != null) {
                this.serverEngine.sendEvent(orderStatus);
            }
        }
    }

}
