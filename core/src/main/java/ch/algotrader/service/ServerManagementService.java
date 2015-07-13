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
package ch.algotrader.service;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface ServerManagementService {

    /**
     * Cancels all orders currently outstanding.
     */
    public void cancelAllOrders();

    /**
     * Manually record a Transaction
     * @param securityId SecurityId (for CREDIT / DEBIT / INTREST_PAID / INTREST_RECEIVED / DIVIDEND / FEES / REFUND enter 0)
     * @param strategyName Name of the Strategy
     * @param extId External transaction id (e.g. 0001f4e6.4fe7e2cb.01.01)
     * @param dateTime DateTime of the Transaction. Format: dd.mm.yyyy hh:mm:ss
     * @param quantity
     * Requested quantity:
     * <ul>
     * <li>BUY: pos</li>
     * <li>SELL: neg</li>
     * <li>EXPIRATION: pos/neg</li>
     * <li>TRANSFER : pos/neg</li>
     * <li>CREDIT: 1</li>
     * <li>INTREST_RECEIVED: 1</li>
     * <li>REFUND : 1</li>
     * <li>DIVIDEND : 1</li>
     * <li>DEBIT: -1</li>
     * <li>INTREST_PAID: -1</li>
     * <li>FEES: -1</li>
     * </ul>
     * @param price Price
     * @param executionCommission Execution Commission. 0 if not applicable
     * @param clearingCommission Clearing Commission. 0 if not applicable
     * @param currency Currency
     * @param transactionType
     * Transaction type:
     * <ul>
     * <li>B (BUY)</li>
     * <li>S (SELL)</li>
     * <li>E (EXPIRATION)</li>
     * <li>T (TRANSFER)</li>
     * <li>C (CREDIT)</li>
     * <li>D (DEBIT)</li>
     * <li>IP (INTREST_PAID)</li>
     * <li>IR (INTREST_RECEIVED)</li>
     * <li>DI (DIVIDEND)</li>
     * <li>F (FEES)</li>
     * <li>RF (REFUND)</li>
     * </ul>
     * @param accountName Account Name
     */
    public void recordTransaction(long securityId, String strategyName, String extId, String dateTime, long quantity, double price, double executionCommission, double clearingCommission, double fee,
            String currency, String transactionType, String accountName);

    /**
     * Transfers a Position to another Strategy.
     * @param positionId Id of the Position
     * @param targetStrategyName Strategy where the Position should be moved to
     */
    public void transferPosition(long positionId, String targetStrategyName);

    /**
     * Hedges all non-base currency exposures with a corresponding FX / FX Future Position
     */
    public void hedgeForex();

    /**
     * performs a Delta Hedge of all Securities of the specified underlyingId
     */
    public void hedgeDelta(long underlyingId);

    /**
     * Creates Rebalance Transactions so that Net-Liquidation-Values of all strategies are in line
     * with the defined Strategy-Allocation.
     */
    public void rebalancePortfolio();

    /**
     * Calculates all Cash Balances and Position quantities based on Transactions in the database
     * and makes adjustments if necessary
     */
    public String resetPositionsAndCashBalances();

    /**
     * Updates the Component Window. This method should only be called after manually manipulating
     * components in the DB.
     */
    public void resetComponentWindow();

    /**
     * Clears the Open Order Window. Should only be called if there are no open orders outstanding
     * with the external Broker
     */
    public void emptyOpenOrderWindow();

    /**
     * Logs current Esper / Java Metrics.
     */
    public void logMetrics();

    /**
     * Resers current Esper / Java Metrics.
     */
    public void resetMetrics();

}
