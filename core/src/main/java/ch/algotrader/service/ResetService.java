/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.service;

import java.util.EnumSet;

import ch.algotrader.enumeration.ResetType;

/**
 * Resets the database to a specific state either in simulation or live trading
 *
 * The following ResetTypes are available:
 * <ul>
 * <li>TRADES: deletes all transactions (except the initial CREDIT), resets all cash balances (except the one associated with the initial CREDIT), deletes all non-persistent positions and resets all persistent ones</li>
 * <li>ORDERS: deletes all orders, orderStati as well as orderProperties</li>
 * <li>SUBSCRIPTIONS: deletes all non-persistent subscriptions and references to them</li>
 * <li>COMBINATIONS_AND_COMPONENTS: deletes all non-persistent combinations as well as all non-persistent components and references to them</li>
 * <li>PROPERTIES: deletes all non-persistent properties</li>
 * <li>MEASUREMENTS: deletes all measurements</li>
 * <li>PORTFOLIO_VALUES: deletes all portfolio values</li>
 * <li>OPTIONS: deletes all options if they are being simulated</li>
 * <li>FUTURES: deletes all Futures if they are being simulated</li>
 * <li>MARKET_DATA: deletes all Bars and Ticks</li>
 * </ul>
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface ResetService {

    /**
     * invokes the specified reset operations
     */
    public void reset(EnumSet<ResetType> resetItems);

    /**
     * Invokes a DB Reset of all simulation related data. The following operations are invoked:
     * <ul>
     * <li>TRADES</li>
     * <li>SUBSCRIPTIONS</li>
     * <li>COMBINATIONS_AND_COMPONENTS</li>
     * <li>PROPERTIES</li>
     * <li>MEASUREMENTS</li>
     * <li>OPTIONS (if they are being simulated)</li>
     * <li>FUTURES (if they are being simulated)</li>
     * </ul>
     */
    public void resetSimulation();

}
