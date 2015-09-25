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
package ch.algotrader.service.lmax;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.adapter.RequestIdGenerator;
import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.lmax.LMAXFixMarketDataRequestFactory;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.fix.FixMarketDataService;
import ch.algotrader.service.fix.FixMarketDataServiceImpl;
import quickfix.field.SubscriptionRequestType;
import quickfix.fix44.MarketDataRequest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class LMAXFixMarketDataServiceImpl extends FixMarketDataServiceImpl implements FixMarketDataService {

    private final LMAXFixMarketDataRequestFactory requestFactory;

    public LMAXFixMarketDataServiceImpl(
            final String sessionQualifier,
            final ExternalSessionStateHolder lifeCycle,
            final FixAdapter fixAdapter,
            final RequestIdGenerator<Security> tickerIdGenerator,
            final Engine serverEngine) {

        super(FeedType.LMAX.name(), sessionQualifier, lifeCycle, fixAdapter, tickerIdGenerator, serverEngine);
        this.requestFactory = new LMAXFixMarketDataRequestFactory(tickerIdGenerator);
    }

    @Override
    public void sendSubscribeRequest(Security security) {

        Validate.notNull(security, "Security is null");

        MarketDataRequest request = this.requestFactory.create(security, SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES);

        getFixAdapter().sendMessage(request, getSessionQualifier());
    }

    @Override
    public void sendUnsubscribeRequest(Security security) {

        Validate.notNull(security, "Security is null");

        MarketDataRequest request = this.requestFactory.create(security, SubscriptionRequestType.DISABLE_PREVIOUS_SNAPSHOT_PLUS_UPDATE_REQUEST);

        getFixAdapter().sendMessage(request, getSessionQualifier());
    }

}
