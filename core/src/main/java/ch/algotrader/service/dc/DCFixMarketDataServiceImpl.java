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
package ch.algotrader.service.dc;

import quickfix.field.SubscriptionRequestType;
import quickfix.fix44.MarketDataRequest;
import ch.algotrader.adapter.dc.DCFixMarketDataRequestFactory;
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

    private final DCFixMarketDataRequestFactory requestFactory;

    public DCFixMarketDataServiceImpl() {

        this.requestFactory = new DCFixMarketDataRequestFactory();
    }

    @Override
    protected FeedType handleGetFeedType() throws Exception {

        return FeedType.DC;
    }

    @Override
    protected String handleGetSessionQualifier() {

        return "DCMD";
    }

    @Override
    protected void handleSendSubscribeRequest(Security security) throws Exception {

        MarketDataRequest request = this.requestFactory.create(security, new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));

        getFixAdapter().sendMessage(request, getSessionQualifier());
    }

    @Override
    protected void handleSendUnsubscribeRequest(Security security) throws Exception {

        MarketDataRequest request = this.requestFactory.create(security, new SubscriptionRequestType(SubscriptionRequestType.DISABLE_PREVIOUS_SNAPSHOT_PLUS_UPDATE_REQUEST));

        getFixAdapter().sendMessage(request, getSessionQualifier());
    }

    @Override
    protected String handleGetTickerId(Security security) throws Exception {

        return DCUtil.getDCSymbol(security);
    }

}
