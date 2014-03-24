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
package ch.algotrader.service.lmax;

import java.io.IOException;

import quickfix.field.SubscriptionRequestType;
import quickfix.fix44.MarketDataRequest;
import ch.algotrader.adapter.lmax.LMAXFixMarketDataRequestFactory;
import ch.algotrader.adapter.lmax.LMAXInstrumentCodeMapper;
import ch.algotrader.adapter.lmax.LMAXUtil;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.FeedType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class LMAXFixMarketDataServiceImpl extends LMAXFixMarketDataServiceBase {

    private static final long serialVersionUID = 1144501885597028244L;

    private final LMAXFixMarketDataRequestFactory requestFactory;

    public LMAXFixMarketDataServiceImpl(final LMAXInstrumentCodeMapper mapper) {

        this.requestFactory = new LMAXFixMarketDataRequestFactory(mapper);
    }

    public LMAXFixMarketDataServiceImpl() {

        try {
            this.requestFactory = new LMAXFixMarketDataRequestFactory(LMAXInstrumentCodeMapper.load());
        } catch (IOException ex) {
            throw new IllegalStateException("Unexpected I/O error loading LMAX instrument list");
        }
    }


    @Override
    protected FeedType handleGetFeedType() throws Exception {

        return FeedType.LMAX;
    }

    @Override
    protected String handleGetSessionQualifier() throws Exception {

        return "LMAXMD";
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

        return LMAXUtil.getLMAXSymbol(security);
    }

}
