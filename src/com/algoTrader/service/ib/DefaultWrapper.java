package com.algoTrader.service.ib;

import java.io.EOFException;
import java.net.SocketException;

import org.apache.log4j.Logger;

import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.util.MyLogger;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.UnderComp;

public class DefaultWrapper implements EWrapper {

    private static Logger logger = MyLogger.getLogger(DefaultWrapper.class.getName());

    private ConnectionState state = ConnectionState.DISCONNECTED;
    private boolean requested;

    public void accountDownloadEnd(String accountName) {
    }

    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
    }

    public void contractDetails(int reqId, ContractDetails contractDetails) {
    }

    public void contractDetailsEnd(int reqId) {
    }

    public void currentTime(long time) {
    }

    public void deltaNeutralValidation(int reqId, UnderComp underComp) {
    }

    public void execDetails(int reqId, Contract contract, Execution execution) {
    }

    public void execDetailsEnd(int reqId) {
    }

    public void fundamentalData(int reqId, String data) {
    }

    public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
    }

    public void managedAccounts(String accountsList) {
    }

    public void nextValidId(int orderId) {
    }

    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
    }

    public void openOrderEnd() {
    }

    public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
    }

    public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
    }

    public void receiveFA(int faDataType, String xml) {
    }

    public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) {
    }

    public void scannerDataEnd(int reqId) {
    }

    public void scannerParameters(String xml) {
    }

    public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureExpiry, double dividendImpact,
            double dividendsToExpiry) {
    }

    public void tickGeneric(int tickerId, int tickType, double value) {
    }

    public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double modelPrice, double pvDividend) {
    }

    public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
    }

    public void tickSize(int tickerId, int field, int size) {
    }

    public void tickSnapshotEnd(int reqId) {
    }

    public void tickString(int tickerId, int tickType, String value) {
    }

    public void updateAccountTime(String timeStamp) {
    }

    public void updateAccountValue(String key, String value, String currency, String accountName) {
    }

    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
    }

    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size) {
    }

    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
    }

    public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
    }

    public void connectionClosed() {

        this.state = ConnectionState.DISCONNECTED;
    }

    public void error(Exception e) {

        // we get EOFException and SocketException when TWS is closed
        if (!(e instanceof EOFException || e instanceof SocketException)) {
            logger.error("ib error", e);
        }
    }

    public void error(String str) {
        logger.error(str);
    }

    public void error(int id, int code, String errorMsg) {

        if (code == 502) {

            // Couldn't connect to TWS
            this.state = ConnectionState.DISCONNECTED;
            logger.debug("connectionState: " + this.state);

        } else if (code == 1100) {

            // Connectivity between IB and TWS has been lost.
            this.state = ConnectionState.CONNECTED;
            logger.debug("connectionState: " + this.state);

        } else if (code == 1101) {

            // Connectivity between IB and TWS has been restored-
            // data lost.
            this.requested = false;
            this.state = ConnectionState.READY;
            logger.debug("connectionState: " + this.state);

        } else if (code == 1102) {

            // Connectivity between IB and TWS has been restored-
            // data maintained.
            if (this.requested) {
                this.state = ConnectionState.SUBSCRIBED;
            } else {
                this.state = ConnectionState.READY;
            }
            logger.debug("connectionState: " + this.state);

        } else if (code == 2110) {

            // Connectivity between TWS and server is broken. It will be
            // restored automatically.
            this.state = ConnectionState.CONNECTED;
            logger.debug("connectionState: " + this.state);

        } else if (code == 2104) {

            // A market data farm is connected.
            if (this.requested) {
                this.state = ConnectionState.SUBSCRIBED;
            } else {
                this.state = ConnectionState.READY;
            }
            logger.debug("connectionState: " + this.state);
        }

        errorMsg = errorMsg.replaceAll("\n", " ");

        if (code < 1000) {
            logger.error("id: " + id + " code: " + code + " " + errorMsg);
        } else {
            logger.debug("id: " + id + " code: " + code + " " + errorMsg);
        }
    }

    public ConnectionState getState() {
        return this.state;
    }

    public void setState(ConnectionState state) {
        this.state = state;
    }

    public boolean isRequested() {
        return this.requested;
    }

    public void setRequested(boolean requested) {
        this.requested = requested;
    }
}
