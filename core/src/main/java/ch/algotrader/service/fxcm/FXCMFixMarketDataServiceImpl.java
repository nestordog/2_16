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
package ch.algotrader.service.fxcm;

import ch.algotrader.adapter.fxcm.FXCMFixMarketDataRequestFactory;
import ch.algotrader.adapter.fxcm.FXCMUtil;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.FeedType;
import quickfix.field.SubscriptionRequestType;
import quickfix.fix44.MarketDataRequest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FXCMFixMarketDataServiceImpl extends FXCMFixMarketDataServiceBase {

    private static final long serialVersionUID = 4881654181517654955L;

    private final FXCMFixMarketDataRequestFactory requestFactory;

    public FXCMFixMarketDataServiceImpl() {
        this.requestFactory = new FXCMFixMarketDataRequestFactory();
    }

    @Override
    protected void handleInit() throws Exception {

        getFixAdapter().openSession(getSessionQualifier());
    }

    @Override
    protected FeedType handleGetFeedType() throws Exception {

        return FeedType.FXCM;
    }

    @Override
    protected String handleGetSessionQualifier() throws Exception {

        return "FXCM";
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

        return FXCMUtil.getFXCMSymbol(security);
    }

}
