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
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.enumeration.FeedType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional
public abstract class ExternalMarketDataServiceImpl implements ExternalMarketDataService {

    private final CommonConfig commonConfig;

    private final SecurityDao securityDao;

    public ExternalMarketDataServiceImpl(final CommonConfig commonConfig, final SecurityDao securityDao) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(securityDao, "SecurityDao is null");

        this.commonConfig = commonConfig;
        this.securityDao = securityDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initSubscriptions() {

        List<Security> securities;
        if (this.commonConfig.isEmbedded()) {
            securities = this.securityDao.findSubscribedByFeedTypeAndStrategyInclFamily(getFeedType(), this.commonConfig.getStartedStrategyName());
        } else {
            securities = this.securityDao.findSubscribedByFeedTypeForAutoActivateStrategiesInclFamily(getFeedType());
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
