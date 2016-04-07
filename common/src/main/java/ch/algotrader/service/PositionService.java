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

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface PositionService {

    /**
     * Closes all Positions of the specified Strategy and unsubscribes the corresponding Security if
     * {@code unsubscribe} is set to true.
     */
    public void closeAllPositionsByStrategy(String strategyName, boolean unsubscribe);

    /**
     * Closes the specified Position and unsubscribes the corresponding Security if {@code
     * unsubscribe} is set to true.
     */
    public void closePosition(long positionId, boolean unsubscribe);

    /**
     * Reduces the specified Position by the specified {@code quantity}
     */
    public void reducePosition(long positionId, long quantity);

    /**
     * Transfers a Position to another Strategy.
     */
    public void transferPosition(long positionId, String targetStrategyName);

    /**
     * Calculates all Position {@code quantities} based on Transactions in the database and makes
     * adjustments if necessary.
     */
    public String resetPositions();

}
