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
package ch.algotrader.service.ftx;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.adapter.RequestIdGenerator;
import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.ftx.FTXFixMarketDataRequestFactory;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.fix.FixMarketDataService;
import ch.algotrader.service.fix.FixMarketDataServiceImpl;
import quickfix.fix44.QuoteRequest;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class FTXFixMarketDataServiceImpl extends FixMarketDataServiceImpl implements FixMarketDataService {

    private final FTXFixMarketDataRequestFactory requestFactory;

    public FTXFixMarketDataServiceImpl(
            final String sessionQualifier,
            final ExternalSessionStateHolder stateHolder,
            final FixAdapter fixAdapter,
            final RequestIdGenerator<Security> tickerIdGenerator,
            final Engine serverEngine) {

        super(FeedType.FTX.name(), sessionQualifier, stateHolder, fixAdapter, tickerIdGenerator, serverEngine);
        this.requestFactory = new FTXFixMarketDataRequestFactory(tickerIdGenerator);
    }

    @Override
    public void sendSubscribeRequest(Security security) {

        Validate.notNull(security, "Security is null");

        QuoteRequest request = this.requestFactory.create(security, 1);

        getFixAdapter().sendMessage(request, getSessionQualifier());
    }

    @Override
    public void sendUnsubscribeRequest(Security security) {

        Validate.notNull(security, "Security is null");

        QuoteRequest request = this.requestFactory.create(security, 2);

        getFixAdapter().sendMessage(request, getSessionQualifier());
    }

}
