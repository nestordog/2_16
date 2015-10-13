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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ch.algotrader.dao.ReadWriteDao;
import ch.algotrader.entity.security.Security;

/**
 * DAO for {@link ch.algotrader.entity.security.Security} objects.
 *
 * @see ch.algotrader.entity.security.Security
 */
public interface SecurityDao extends ReadWriteDao<Security> {

    /**
     * Finds a Security by its {@code id} and initializes it
     * @param id
     * @return Security
     */
    Security findByIdInitialized(long id);

    /**
     * Finds multiple Securities by their {@code ids}.
     * @param ids
     * @return List<Security>
     */
    List<Security> findByIds(Collection<Long> ids);

    /**
     * Finds a Security by its {@code symbol}
     * @param symbol
     * @return Security
     */
    Security findBySymbol(String symbol);

    /**
     * Finds a Security by its {@code isin}
     * @param isin
     * @return Security
     */
    Security findByIsin(String isin);

    /**
     * Finds a Security by its {@code bbgid}
     * @param bbgid
     * @return Security
     */
    Security findByBbgid(String bbgid);

    /**
     * Finds a Security by its {@code ric}
     * @param ric
     * @return Security
     */
    Security findByRic(String ric);

    /**
     * Finds a Security by its {@code conid}
     * @param conid
     * @return Security
     */
    Security findByConid(String conid);

    /**
     * Finds a Security by its {@code TT security ID}
     * @param ttid
     * @return Security
     */
    Security findByTtid(String ttid);

    /**
     * Finds a Security by its {@code id} incl. In addition the corresponding {@link ch.algotrader.entity.security.SecurityFamily}
     * and Underlying {@link Security} are initialized.
     * @param id
     * @return Security
     */
    Security findByIdInclFamilyAndUnderlying(long id);

    /**
     * Finds a Security by its {@code id} incl. In addition the corresponding {@link ch.algotrader.entity.security.SecurityFamily},
     * underlying {@link Security}, the {@link ch.algotrader.entity.exchange.Exchange} and
     * {@link ch.algotrader.entity.security.BrokerParameters} are initialized.
     * @param id
     * @return Security
     */
    Security findByIdInclFamilyUnderlyingExchangeAndBrokerParameters(long id);

    /**
     * Finds all Securities that are subscribed by at least one Strategy which is marked as {@code
     * autoActive}.
     * @return List<Security>
     */
    List<Security> findSubscribedForAutoActivateStrategies();

    /**
     * Finds all Securities that are subscribed by at least one Strategy which is marked as {@code
     * autoActive} and by the specified {@code feedType}. In addition the {@link ch.algotrader.entity.security.SecurityFamily}
     * will be initialized.
     * @param feedType
     * @return List<Security>
     */
    List<Security> findSubscribedByFeedTypeForAutoActivateStrategiesInclFamily(String feedType);

    /**
     * Finds all Securities that are subscribed by at least one Strategy which is marked as {@code
     * autoActive} and by the specified {@code feedType}. In addition the {@link ch.algotrader.entity.security.SecurityFamily}
     * will be initialized.
     * @param feedType
     * @param strategyName
     * @return List<Security>
     */
    List<Security> findSubscribedByFeedTypeAndStrategyInclFamily(String feedType, String strategyName);

    /**
     * Finds all Securities and corresponding feed types that are subscribed by at least one
     * Strategy which is marked as {@code autoActive} and the specified {@code feedType}.
     * @return List<Map>
     */
    List<Map<String, Object>> findSubscribedAndFeedTypeForAutoActivateStrategies();

    /**
     * Gets the {@code securityId} of the Security defined by the specified {@code isin}.
     * @param isin
     * @return Long
     */
    Long findSecurityIdByIsin(String isin);

}
