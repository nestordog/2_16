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

    private int clientId;

    public DefaultWrapper(int clientId) {
        this.clientId = clientId;
    }

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
    public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
    }

    @Override
    public void managedAccounts(String accountsList) {
    }

    @Override
    public void nextValidId(int orderId) {
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
    public void error(String str) {
        logger.error(str, new RuntimeException(str));
    }

    @Override
    public void error(int id, int code, String errorMsg) {

        String message = "client: " + this.clientId + " id: " + id + " code: " + code + " " + errorMsg.replaceAll("\n", " ");
        if (code < 1000) {
            logger.error(message, new RuntimeException(message));
        } else {
            logger.debug(message);
        }

        if (code == 502) {

            // Couldn't connect to TWS
            this.state = ConnectionState.DISCONNECTED;
            logger.debug("connectionState: " + this.state);

        } else if (code == 1100) {

            // Connectivity between IB and TWS has been lost.
            this.state = ConnectionState.CONNECTED;
            logger.debug("connectionState: " + this.state);

        } else if (code == 1101) {

            // Connectivity between IB and TWS has been restored data lost.
            this.requested = false;
            this.state = ConnectionState.READY;
            logger.debug("connectionState: " + this.state);

        } else if (code == 1102) {

            // Connectivity between IB and TWS has been restored data maintained.
            if (this.requested) {
                this.state = ConnectionState.SUBSCRIBED;
            } else {
                this.state = ConnectionState.READY;
            }
            logger.debug("connectionState: " + this.state);

        } else if (code == 2110) {

            // Connectivity between TWS and server is broken. It will be restored automatically.
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
