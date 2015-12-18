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

import java.util.Date;
import java.util.List;

import ch.algotrader.entity.security.Future;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface FutureService {

    /**
     * Creates the missing Part of the Futures Chain defined by the {@code futureFamilyId}.
     */
    public void createDummyFutures(long futureFamilyId);

    /**
     * Get the first Future with {@code expirationDate} after the specified {@code
     * targetExpirationDate} and {@code futureFamilyId}.
     * In simulation mode, if {@code simulateFuturesByUnderlying} or {code
     * simulateFuturesByGenericFutures} is configured, a dummy Future will be created if none was
     * found.
     */
    public Future getFutureByMinExpiration(long futureFamilyId, Date targetExpirationDate);

    /**
     * Gets a Future by its {@code futureFamilyId} and {@code expirationDate}.
     * In simulation mode, if {@code simulateFuturesByUnderlying} or {code
     * simulateFuturesByGenericFutures} is configured, a dummy Future will be created if none was
     * found.
     */
    public Future getFutureByExpiration(long futureFamilyId, Date expirationDate);

    /**
     * Gets a Future by its {@code futureFamilyId}, that is {@code n} durations after the specified
     * {@code targetExpirationDate}.
     * In simulation mode, if {@code simulateFuturesByUnderlying} or {code
     * simulateFuturesByGenericFutures} is configured, a dummy Future will be created if none was
     * found.
     */
    public Future getFutureByDuration(long futureFamilyId, Date targetDate, int duration);

    /**
     * Gets a Future by its {@code futureFamilyId} and maturity {@code month} and {@code year}.
     * In simulation mode, if {@code simulateFuturesByUnderlying} or {code
     * simulateFuturesByGenericFutures} is configured, a dummy Future will be created if none was
     * found.
     */
    public Future getFutureByMonthYear(long futureFamilyId, int year, int month);

    /**
     * Gets all Futures for the specified {@code futureFamilyId} with an {@code expirationDate} after
     * the specified {@code targetExpirationDate}.
     * The returned Futures are sorted with their {@code expirationDate}
     * in ascending order.
     */
    public List<Future> getFuturesByMinExpiration(long futureFamilyId, Date minExpirationDate);

}
