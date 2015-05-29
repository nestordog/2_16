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

import java.math.BigDecimal;

import ch.algotrader.entity.Position;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
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
     * Creates a Position based on a non-tradeable Security (e.g. {@link
     * ch.algotrader.entity.security.Combination Combination})
     */
    public Position createNonTradeablePosition(String strategyName, long securityId, long quantity);

    /**
     * Modifies a Position that is based on a non-tradeable Security (e.g. {@link
     * ch.algotrader.entity.security.Combination Combination})
     */
    public Position modifyNonTradeablePosition(long positionId, long quantity);

    /**
     * Deletes a Position that is based on a non-tradeable Security (e.g. {@link
     * ch.algotrader.entity.security.Combination Combination})
     */
    public void deleteNonTradeablePosition(long positionId, boolean unsubscribe);

    /**
     * Reduces the specified Position by the specified {@code quantity}
     */
    public void reducePosition(long positionId, long quantity);

    /**
     * Transfers a Position to another Strategy.
     */
    public void transferPosition(long positionId, String targetStrategyName);

    /**
     * Calculates margins for all open positions
     */
    public void setMargins();

    /**
     * Calculates the margin for the specified position.
     */
    public Position setMargin(long positionId);

    /**
     * Expires all expirable Positions. Only Positions on Securities that have an {@code
     * expirationDate} in the past will be expired.
     */
    public void expirePositions();

    /**
     * Sets or modifies the ExitValue of the specified Position. The ExitValue is set according to
     * the {@code scale} defined by the {@link ch.algotrader.entity.security.SecurityFamily
     * SecurityFamily}.
     * The method performs the following checks:
     * <ul>
     * <li>The new ExitValues should not be set lower (higher) than the existing ExitValue for long
     * (short) positions. This check can be overwritten by setting {@code force} to true</li>
     * <li>The new ExitValues cannot be higher (lower) than the {@code currentValue} for long
     * (short) positions</li>
     * <ul>
     */
    public Position setExitValue(long positionId, BigDecimal exitValue, boolean force);

    /**
     * Removes the ExitValue from the specified Position.
     */
    public Position removeExitValue(long positionId);

    /**
     * Calculates all Position {@code quantities} based on Transactions in the database and makes
     * adjustments if necessary.
     */
    public String resetPositions();

}
