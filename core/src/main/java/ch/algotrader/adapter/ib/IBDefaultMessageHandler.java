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
package ch.algotrader.adapter.ib;

import java.io.EOFException;
import java.net.SocketException;

import org.apache.log4j.Logger;

import ch.algotrader.util.MyLogger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.enumeration.ConnectionState;
import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EWrapper;
import com.ib.client.EWrapperMsgGenerator;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.UnderComp;

/**
 * Default MessageHandler that implements the {@link EWrapper} interface.
 * Most methods have a do-nothing implementation.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBDefaultMessageHandler implements EWrapper {

    private static Logger logger = MyLogger.getLogger(IBDefaultMessageHandler.class.getName());

    private ConnectionState state = ConnectionState.DISCONNECTED;
    private boolean requested;
    private int clientId;

    public IBDefaultMessageHandler(int clientId) {

        this.clientId = clientId;
    }

    @Override
    public void connectionClosed() {

        this.state = ConnectionState.DISCONNECTED;
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
        // coming along, the GenericOrder has to be cancelled by us (potenially creating a "fake" OrderStatus)

            case 104:

                // Can't modify a filled order.
                // do nothing, we modified the order just a little bit too late
                logger.warn(message);
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
                    logger.error(message);
                }
                break;

            case 202:

                // Order cancelled
                // do nothing, since we cancelled the order ourself
                logger.debug(message);
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
                logger.info(message);
                break;

            case 502:

                // Couldn't connect to TWS
                setState(ConnectionState.DISCONNECTED);
                logger.debug(message);
                break;

            case 1100:

                // Connectivity between IB and TWS has been lost.
                setState(ConnectionState.CONNECTED);
                logger.debug(message);
                break;

            case 1101:

                // Connectivity between IB and TWS has been restored data lost.
                setRequested(false);
                setState(ConnectionState.LOGGED_ON);
                ServiceLocator.instance().getMarketDataService().initSubscriptions();
                logger.debug(message);
                break;

            case 1102:

                // Connectivity between IB and TWS has been restored data maintained.
                if (isRequested()) {
                    setState(ConnectionState.SUBSCRIBED);
                } else {
                    setState(ConnectionState.LOGGED_ON);
                    ServiceLocator.instance().getMarketDataService().initSubscriptions();
                }
                logger.debug(message);
                break;

            case 2110:

                // Connectivity between TWS and server is broken. It will be restored automatically.
                setState(ConnectionState.CONNECTED);
                logger.debug(message);
                break;

            case 2104:

                // A market data farm is connected.
                if (isRequested()) {
                    setState(ConnectionState.SUBSCRIBED);
                } else {
                    setState(ConnectionState.LOGGED_ON);

                    // initSubscriptions if there is a marketDataService
                    if (ServiceLocator.instance().containsService("marketDataService")) {
                        ServiceLocator.instance().getMarketDataService().initSubscriptions();
                    }
                }
                logger.debug(message);
                break;

            default:
                if (code < 1000) {
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

    public ConnectionState getState() {
        return this.state;
    }

    public void setState(ConnectionState state) {

        if (this.state != state) {
            logger.debug("client: " + this.clientId + " state: " + state);
        }
        this.state = state;
    }

    public boolean isRequested() {
        return this.requested;
    }

    public void setRequested(boolean requested) {

        if (this.requested != requested) {
            logger.debug("client: " + this.clientId + " requested: " + requested);
        }

        this.requested = requested;
    }

    @Override
    public synchronized void nextValidId(final int orderId) {

        if (this.clientId == 0) {
            IBIdGenerator.getInstance().initializeOrderId(orderId);
            logger.debug("client: " + this.clientId + " " + EWrapperMsgGenerator.nextValidId(orderId));
        }
    }

    // Override EWrapper methods with default implementation

    @Override
    public void accountDownloadEnd(String accountName) {
    }

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {
    }

    @Override
    public void contractDetailsEnd(int reqId) {
    }

    @Override
    public void currentTime(long time) {
    }

    @Override
    public void deltaNeutralValidation(int reqId, UnderComp underComp) {
    }

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
    }

    @Override
    public void execDetailsEnd(int reqId) {
    }

    @Override
    public void fundamentalData(int reqId, String data) {
    }

    @Override
    public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double wap, boolean hasGaps) {
    }

    @Override
    public void managedAccounts(String accountsList) {
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
    }

    @Override
    public void openOrderEnd() {
    }

    @Override
    public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice,
            int clientId, String whyHeld) {
    }

    @Override
    public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
    }

    @Override
    public void receiveFA(int faDataType, String xml) {
    }

    @Override
    public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) {
    }

    @Override
    public void scannerDataEnd(int reqId) {
    }

    @Override
    public void scannerParameters(String xml) {
    }

    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureExpiry,
            double dividendImpact, double dividendsToExpiry) {
    }

    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {
    }

    @Override
    public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
    }

    @Override
    public void tickSize(int tickerId, int field, int size) {
    }

    @Override
    public void tickSnapshotEnd(int reqId) {
    }

    @Override
    public void tickString(int tickerId, int tickType, String value) {
    }

    @Override
    public void tickOptionComputation(int arg0, int arg1, double arg2, double arg3, double arg4, double arg5, double arg6, double arg7, double arg8, double arg9) {

    }

    @Override
    public void marketDataType(int arg0, int arg1) {
    }

    @Override
    public void updateAccountTime(String timeStamp) {
    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
    }

    @Override
    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size) {
    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
    }

    @Override
    public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL,
            double realizedPNL, String accountName) {
    }

    @Override
    public void commissionReport(CommissionReport paramCommissionReport) {
    }
}
