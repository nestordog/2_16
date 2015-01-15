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
package ch.algotrader.entity.security;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import ch.algotrader.enumeration.OptionType;
import ch.algotrader.hibernate.ReadWriteDao;

/**
 * DAO for {@link ch.algotrader.entity.security.Option} objects.
 *
 * @see ch.algotrader.entity.security.Option
 */
public interface OptionDao extends ReadWriteDao<Option> {

    /**
     * Finds all Options of the give {@code underlyingId} and {@code optionType} that expire after
     * the specified {@code targetExpirationDate}. The returned Options are sorted by their {@code
     * expirationDate} and distance to the defined {@code underlyingSpot} (the Option with the
     * minimum distance coming first).
     * The <code>limit</code> argument allows you to specify the limit when you are paging the results.
     * @param limit
     * @param underlyingId
     * @param targetExpirationDate
     * @param underlyingSpot
     * @param optionType
     * @return List<Option>
     */
    List<Option> findByMinExpirationAndMinStrikeDistance(int limit, int underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot, OptionType optionType);

    /**
     * Finds all Options of the give {@code underlyingId} and {@code optionType} that expire after
     * the specified {@code targetExpirationDate} and have a strike that is lower (for PUTS) resp.
     * higher (for Calls) than the {@code underlyingSpot}. The returned Options are sorted by their
     * {@code expirationDate} and distance to the defined {@code underlyingSpot} (the Option with
     * the minimum distance coming first).
     * The <code>limit</code> argument allows you to specify the limit when you are paging the results.
     * @param limit
     * @param underlyingId
     * @param targetExpirationDate
     * @param underlyingSpot
     * @param optionType
     * @return List<Option>
     */
    List<Option> findByMinExpirationAndStrikeLimit(int limit, int underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot, OptionType optionType);

    /**
     * Finds all Options of the give {@code underlyingId} and {@code optionType} that expire after
     * the specified {@code targetExpirationDate} and have Ticks on the given {@code date}. The
     * returned Options are sorted by their {@code expirationDate} and distance to the defined
     * {@code underlyingSpot} (the Option with the minimum distance coming first).
     * The <code>limit</code> argument allows you to specify the limit when you are paging the results.
     * @param limit
     * @param underlyingId
     * @param targetExpirationDate
     * @param underlyingSpot
     * @param optionType
     * @param date
     * @return List<Option>
     */
    List<Option> findByMinExpirationAndMinStrikeDistanceWithTicks(int limit, int underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot, OptionType optionType, Date date);

    /**
     * Finds all Options of the give {@code underlyingId} and {@code optionType} that expire after
     * the specified {@code targetExpirationDate}, have a strike that is lower (for PUTS) resp.
     * higher (for Calls) than the {@code underlyingSpot} and have Ticks on the given {@code date}.
     * The returned Options are sorted by their {@code expirationDate} and distance to the defined
     * {@code underlyingSpot} (the Option with the minimum distance coming first).
     * The <code>limit</code> argument allows you to specify the limit when you are paging the results.
     * @param limit
     * @param underlyingId
     * @param targetExpirationDate
     * @param underlyingSpot
     * @param optionType
     * @param date
     * @return List<Option>
     */
    List<Option> findByMinExpirationAndStrikeLimitWithTicks(int limit, int underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot, OptionType optionType, Date date);

    /**
     * Finds all Options that are subscribed by at least one Strategy.
     * @return List<Option>
     */
    List<Option> findSubscribedOptions();

    /**
     * Finds all Options of the specified {@link SecurityFamily}
     * @param securityFamilyId
     * @return List<Option>
     */
    List<Option> findBySecurityFamily(int securityFamilyId);

    /**
     * Finds all Expiration Dates of Options of the specified {@code underlyingId} that are
     * tradeable (i.e. have Ticks) on the given {@code dateTime}.
     * @param underlyingId
     * @param dateTime
     * @return List<Date>
     */
    List<Date> findExpirationsByUnderlyingAndDate(int underlyingId, Date dateTime);

    /**
     * Finds a Option by the specified {@code expirationDate}, {@code strike}, {@code type} and
     * {@code optionFamilyId}.
     * @param optionFamilyId
     * @param expirationDate
     * @param strike
     * @param type
     * @return Option
     */
    Option findByExpirationStrikeAndType(int optionFamilyId, Date expirationDate, BigDecimal strike, OptionType type);

    // spring-dao merge-point

}