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

import java.util.List;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.util.spring.HibernateSession;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@HibernateSession
public abstract class ExternalMarketDataServiceImpl implements ExternalMarketDataService {

    private final SecurityDao securityDao;

    public ExternalMarketDataServiceImpl(final SecurityDao securityDao) {

        Validate.notNull(securityDao, "SecurityDao is null");

        this.securityDao = securityDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initSubscriptions() {

        // process all subscriptions that do not have a feedType associated
        List<Security> securities = this.securityDao.findSubscribedByFeedTypeForAutoActivateStrategiesInclFamily(getFeedType());
        for (Security security : securities) {
            if (!security.getSecurityFamily().isSynthetic()) {
                subscribe(security);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void subscribe(Security security);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void unsubscribe(Security security);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FeedType getFeedType();

}
