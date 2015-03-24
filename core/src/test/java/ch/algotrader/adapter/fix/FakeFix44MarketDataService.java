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

import quickfix.field.MDReqID;
import quickfix.field.SubscriptionRequestType;
import quickfix.fix44.MarketDataRequest;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.service.fix.fix44.Fix44MarketDataServiceException;
import ch.algotrader.service.fix.fix44.Fix44MarketDataServiceImpl;

/**
 * Mock FIX 4.4 market data service
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
class FakeFix44MarketDataService extends Fix44MarketDataServiceImpl {

    private static final long serialVersionUID = -1901678386181476171L;

    public FakeFix44MarketDataService(
            final FixSessionStateHolder lifeCycle,
            final FixAdapter fixAdapter,
            final EngineManager engineManager,
            final SecurityDao securityDao) {

        super(lifeCycle, fixAdapter, engineManager, securityDao);
    }

    @Override
    public void sendSubscribeRequest(final Security security) {

        try {
            String symbol = security.getSymbol();
            MarketDataRequest request = new MarketDataRequest();
            request.set(new MDReqID(symbol));
            request.set(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));
            getFixAdapter().sendMessage(request, "FAKE");
        } catch (Exception ex) {
            throw new Fix44MarketDataServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void sendUnsubscribeRequest(final Security security) {

        try {
            String symbol = security.getSymbol();
            MarketDataRequest request = new MarketDataRequest();
            request.set(new MDReqID(symbol));
            request.set(new SubscriptionRequestType(SubscriptionRequestType.DISABLE_PREVIOUS_SNAPSHOT_PLUS_UPDATE_REQUEST));
            getFixAdapter().sendMessage(request, "FAKE");
        } catch (Exception ex) {
            throw new Fix44MarketDataServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public String getSessionQualifier() {

        return "FAKE";
    }

    @Override
    public String getTickerId(final Security security) {

        return security.getSymbol();
    }

    @Override
    public FeedType getFeedType() {

        return FeedType.SIM;
    }
}
