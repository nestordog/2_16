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
package ch.algotrader.dao.security;

import java.util.Date;
import java.util.List;

import ch.algotrader.entity.security.Future;
import ch.algotrader.hibernate.ReadWriteDao;

/**
 * DAO for {@link ch.algotrader.entity.security.Future} objects.
 *
 * @see ch.algotrader.entity.security.Future
 */
public interface FutureDao extends ReadWriteDao<Future> {

    /**
     * Finds a Future by the specified {@code expirationDate} and {@code futureFamilyId} with the
     * {@link ch.algotrader.entity.security.SecurityFamily} initialized.
     * @param futureFamilyId
     * @param expirationDate
     * @return Future
     */
    Future findByExpirationInclSecurityFamily(long futureFamilyId, Date expirationDate);

    /**
     * Finds all Futures with {@code expirationDate} after the specified {@code
     * targetExpirationDate} and {@code futureFamilyId}. The returned Futures are sorted with their
     * {@code expirationDate} in ascending order.
     * @param futureFamilyId
     * @param targetExpirationDate
     * @return List<Future>
     */
    List<Future> findByMinExpiration(long futureFamilyId, Date targetExpirationDate);

    /**
     * <p>
     * Does the same thing as {@link #findByMinExpiration(long, Date)} with an
     * additional argument called <code>limit</code>. The <code>limit</code>
     * argument allows you to specify the limit when you are paging the results.
     * </p>
     * @param limit
     * @param futureFamilyId
     * @param targetExpirationDate
     * @return List<Future>
     */
    List<Future> findByMinExpiration(int limit, long futureFamilyId, Date targetExpirationDate);

    /**
     * Finds all Futures that are subscribed by at least one Strategy.
     * @return List<Future>
     */
    List<Future> findSubscribedFutures();

    /**
     * Finds all Futures of the specified {@link ch.algotrader.entity.security.SecurityFamily}
     * @param securityFamilyId
     * @return List<Future>
     */
    List<Future> findBySecurityFamily(long securityFamilyId);

    /**
     * Finds a Future by the specified {@code expirationMonth} and {@code futureFamilyId}. the
     * {@code expirationMonth} has to be defined as the last day of the month (e.g. March 31st
     * 00:00:00).
     * @param futureFamilyId
     * @param expirationMonth
     * @return Future
     */
    Future findByExpirationMonth(long futureFamilyId, Date expirationMonth);
    // spring-dao merge-point

}