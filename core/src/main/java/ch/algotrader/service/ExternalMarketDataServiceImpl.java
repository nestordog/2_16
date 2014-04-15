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

import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.FeedType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class ExternalMarketDataServiceImpl extends ExternalMarketDataServiceBase {

    @Override
    protected void handleInitSubscriptions() {

        // process all subscriptions that do not have a feedType associated
        List<Security> securities = getSecurityDao().findSubscribedByFeedTypeForAutoActivateStrategiesInclFamily(getFeedType());
        for (Security security : securities) {
            if (!security.getSecurityFamily().isSynthetic()) {
                subscribe(security);
            }
        }
    }

    @Override
    protected abstract void handleSubscribe(Security security) throws Exception;

    @Override
    protected abstract void handleUnsubscribe(Security security) throws Exception;

    @Override
    protected abstract FeedType handleGetFeedType() throws Exception;
}
