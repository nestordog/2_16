/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.adapter.fix;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.ExternalServiceException;
import ch.algotrader.service.fix.FixMarketDataService;
import ch.algotrader.service.fix.FixMarketDataServiceImpl;
import quickfix.field.MDReqID;
import quickfix.field.SubscriptionRequestType;
import quickfix.fix44.MarketDataRequest;

/**
 * Mock FIX 4.4 market data service
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
class FakeFix44MarketDataService extends FixMarketDataServiceImpl implements FixMarketDataService {

    public FakeFix44MarketDataService(
            final ExternalSessionStateHolder lifeCycle,
            final FixAdapter fixAdapter,
            final Engine serverEngine) {

        super(FeedType.SIM.name(), lifeCycle, fixAdapter, Security::getSymbol, serverEngine);
    }

    @Override
    public void sendSubscribeRequest(final Security security) {

        try {
            String symbol = security.getSymbol();
            MarketDataRequest request = new MarketDataRequest();
            request.set(new MDReqID(symbol));
            request.set(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));
            getFixAdapter().sendMessage(request, getSessionQualifier());
        } catch (Exception ex) {
            throw new ExternalServiceException(ex.getMessage(), ex);
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
            throw new ExternalServiceException(ex.getMessage(), ex);
        }
    }

}
