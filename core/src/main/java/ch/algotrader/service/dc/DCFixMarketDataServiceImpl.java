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
package ch.algotrader.service.dc;

import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.fix44.MarketDataRequest;
import ch.algotrader.adapter.dc.DCUtil;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.FeedType;

/**
 * DukasCopy market data service implementation.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DCFixMarketDataServiceImpl extends DCFixMarketDataServiceBase {

    private static final long serialVersionUID = 7765025849172510539L;

    @Override
    protected FeedType handleGetFeedType() throws Exception {

        return FeedType.DC;
    }

    @Override
    protected void handleSendSubscribeRequest(Security security) throws Exception {

        MarketDataRequest request = createMarketDataRequest(security, SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES);

        getFixAdapter().sendMessage(request, getSessionQualifier());
    }

    @Override
    protected void handleSendUnsubscribeRequest(Security security) throws Exception {

        MarketDataRequest request = createMarketDataRequest(security, SubscriptionRequestType.DISABLE_PREVIOUS_SNAPSHOT_PLUS_UPDATE_REQUEST);

        getFixAdapter().sendMessage(request, getSessionQualifier());
    }

    @Override
    protected String handleGetSessionQualifier() {
        return "DCMD";
    }

    @Override
    protected int handleGetTickerId(Security security) throws Exception {
        return DCUtil.getTickerId(security);
    }

    private MarketDataRequest createMarketDataRequest(Security security, char type) {

        MarketDataRequest request = new MarketDataRequest();
        request.set(new SubscriptionRequestType(type));
        request.set(new MDReqID(DCUtil.getSymbol(security)));

        MarketDataRequest.NoMDEntryTypes bid = new MarketDataRequest.NoMDEntryTypes();
        bid.set(new MDEntryType(MDEntryType.BID));
        request.addGroup(bid);

        MarketDataRequest.NoMDEntryTypes offer = new MarketDataRequest.NoMDEntryTypes();
        offer.set(new MDEntryType(MDEntryType.OFFER));
        request.addGroup(offer);

        MarketDataRequest.NoRelatedSym symbol = new MarketDataRequest.NoRelatedSym();
        symbol.set(new Symbol(DCUtil.getSymbol(security)));
        request.addGroup(symbol);

        request.set(new MarketDepth(1));
        request.set(new MDUpdateType(0));

        return request;
    }
}
