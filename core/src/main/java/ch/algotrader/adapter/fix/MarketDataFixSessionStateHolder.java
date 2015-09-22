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

import ch.algotrader.adapter.DataFeedSessionStateHolder;
import ch.algotrader.event.dispatch.EventDispatcher;

/**
 * Market Data feed specific {@link ch.algotrader.adapter.ExternalSessionStateHolder}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class MarketDataFixSessionStateHolder extends DefaultFixSessionStateHolder implements DataFeedSessionStateHolder {

    private final String feedType;

    public MarketDataFixSessionStateHolder(final String name, final EventDispatcher eventDispatcher, final String feedType) {
        super(name, eventDispatcher);
        this.feedType = feedType;
    }

    public String getFeedType() {
        return this.feedType;
    }

}
