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
package ch.algotrader.adapter.fix;

import ch.algotrader.enumeration.FeedType;
import ch.algotrader.service.MarketDataService;

/**
 * Market Data feed specific {@link FixSessionLifecycle}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision: 6424 $ $Date: 2013-11-06 14:29:48 +0100 (Mi, 06 Nov 2013) $
 */
public class MarketDataFixSessionLifecycle extends DefaultFixSessionLifecycle {

    private MarketDataService marketDataService;
    private FeedType feedType;

    public void setMarketDataService(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    public void setFeedType(FeedType feedType) {
        this.feedType = feedType;
    }

    @Override
    public void logon() {

        super.logon();

        marketDataService.initSubscriptions(feedType);
    }
}
