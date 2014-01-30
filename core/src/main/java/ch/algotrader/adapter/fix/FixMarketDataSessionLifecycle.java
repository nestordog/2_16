/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.adapter.fix;

import ch.algotrader.enumeration.FeedType;
import ch.algotrader.service.MarketDataService;

/**
 * DukasCopy specific {@link FixSessionLifecycle}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision: 6424 $ $Date: 2013-11-06 14:29:48 +0100 (Mi, 06 Nov 2013) $
 */
public class FixMarketDataSessionLifecycle extends DefaultFixSessionLifecycle {

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
