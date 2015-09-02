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

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.dc.DCFixMarketDataRequestFactory;
import ch.algotrader.adapter.dc.DCUtil;
import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.fix.FixSessionStateHolder;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.fix.FixMarketDataServiceImpl;
import ch.algotrader.service.fix.fix44.Fix44MarketDataService;
import quickfix.field.SubscriptionRequestType;
import quickfix.fix44.MarketDataRequest;

/**
 * DukasCopy market data service implementation.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DCFixMarketDataServiceImpl extends FixMarketDataServiceImpl implements Fix44MarketDataService {

    private static final long serialVersionUID = 7765025849172510539L;

    private final DCFixMarketDataRequestFactory requestFactory;

    public DCFixMarketDataServiceImpl(
            final FixSessionStateHolder lifeCycle,
            final FixAdapter fixAdapter,
            final Engine serverEngine) {

        super(lifeCycle, fixAdapter, serverEngine);

        this.requestFactory = new DCFixMarketDataRequestFactory();
    }

    @Override
    public FeedType getFeedType() {

        return FeedType.DC;
    }

    @Override
    public String getSessionQualifier() {

        return "DCMD";
    }

    @Override
    public void sendSubscribeRequest(Security security) {

        Validate.notNull(security, "Security is null");

        MarketDataRequest request = this.requestFactory.create(security, new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));

        getFixAdapter().sendMessage(request, getSessionQualifier());
    }

    @Override
    public void sendUnsubscribeRequest(Security security) {

        Validate.notNull(security, "Security is null");

        MarketDataRequest request = this.requestFactory.create(security, new SubscriptionRequestType(SubscriptionRequestType.DISABLE_PREVIOUS_SNAPSHOT_PLUS_UPDATE_REQUEST));

        getFixAdapter().sendMessage(request, getSessionQualifier());
    }

    @Override
    public String getTickerId(Security security) {

        Validate.notNull(security, "Security is null");

        return DCUtil.getDCSymbol(security);

    }
}
