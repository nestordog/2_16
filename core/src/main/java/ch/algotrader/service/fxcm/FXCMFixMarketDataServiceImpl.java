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

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.fix.FixSessionStateHolder;
import ch.algotrader.adapter.fxcm.FXCMFixMarketDataRequestFactory;
import ch.algotrader.adapter.fxcm.FXCMUtil;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.fix.fix44.Fix44MarketDataServiceImpl;
import quickfix.field.SubscriptionRequestType;
import quickfix.fix44.MarketDataRequest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FXCMFixMarketDataServiceImpl extends Fix44MarketDataServiceImpl implements FXCMFixMarketDataService {

    private static final long serialVersionUID = 4881654181517654955L;

    private final FXCMFixMarketDataRequestFactory requestFactory;

    public FXCMFixMarketDataServiceImpl(
            final CommonConfig commonConfig,
            final FixSessionStateHolder lifeCycle,
            final FixAdapter fixAdapter,
            final Engine serverEngine,
            final SecurityDao securityDao) {

        super(commonConfig, lifeCycle, fixAdapter, serverEngine, securityDao);

        this.requestFactory = new FXCMFixMarketDataRequestFactory();
    }

    @Override
    public void init() {

        getFixAdapter().openSession(getSessionQualifier());
    }

    @Override
    public FeedType getFeedType() {

        return FeedType.FXCM;
    }

    @Override
    public String getSessionQualifier() {

        return "FXCM";
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

        return FXCMUtil.getFXCMSymbol(security);

    }
}
