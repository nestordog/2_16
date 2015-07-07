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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional
public abstract class ExternalMarketDataServiceImpl implements ExternalMarketDataService {

    private final EngineManager engineManager;

    private final SecurityDao securityDao;

    public ExternalMarketDataServiceImpl(final EngineManager engineManager, final SecurityDao securityDao) {

        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(securityDao, "SecurityDao is null");

        this.engineManager = engineManager;
        this.securityDao = securityDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initSubscriptions() {

        final List<Security> securities = new ArrayList<Security>();
        for (final Engine engine : engineManager.getEngines()) {
            securities.addAll(this.securityDao.findSubscribedByFeedTypeAndStrategyInclFamily(getFeedType(), engine.getStrategyName()));
        }

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
