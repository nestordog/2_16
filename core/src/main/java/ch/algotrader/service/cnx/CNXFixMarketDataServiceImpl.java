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
package ch.algotrader.service.cnx;

import ch.algotrader.adapter.cnx.CNXFixMarketDataRequestFactory;
import ch.algotrader.adapter.cnx.CNXUtil;
import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.FeedType;
import quickfix.field.SubscriptionRequestType;
import quickfix.fix44.MarketDataRequest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CNXFixMarketDataServiceImpl extends CNXFixMarketDataServiceBase {

    private static final long serialVersionUID = 2946126163433296876L;

    private final CNXFixMarketDataRequestFactory requestFactory;

    public CNXFixMarketDataServiceImpl() {
        this.requestFactory = new CNXFixMarketDataRequestFactory();
    }

    @Override
    protected void handleSendSubscribeRequest(Security security) throws Exception {

        MarketDataRequest request = this.requestFactory.create(security, new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));

        getFixAdapter().sendMessage(request, getSessionQualifier());
    }

    @Override
    protected String handleGetTickerId(Security security) throws Exception {

        if (!(security instanceof Forex)) {

            throw new FixApplicationException("Currenex supports forex orders only");
        }
        Forex forex = (Forex) security;
        return CNXUtil.getCNXSymbol(forex);
    }

    @Override
    protected void handleSendUnsubscribeRequest(Security security) throws Exception {

        MarketDataRequest request = this.requestFactory.create(security, new SubscriptionRequestType(SubscriptionRequestType.DISABLE_PREVIOUS_SNAPSHOT_PLUS_UPDATE_REQUEST));

        getFixAdapter().sendMessage(request, getSessionQualifier());
    }

    @Override
    protected String handleGetSessionQualifier() throws Exception {

        return "CNXMD";
    }

    @Override
    protected FeedType handleGetFeedType() throws Exception {

        return FeedType.CNX;
    }

}
