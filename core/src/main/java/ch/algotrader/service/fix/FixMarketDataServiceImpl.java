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
package ch.algotrader.service.fix;

import org.apache.log4j.Logger;

import ch.algotrader.adapter.fix.FixSessionLifecycle;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Security;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.ib.IBNativeMarketDataServiceException;
import ch.algotrader.util.MyLogger;
import ch.algotrader.vo.SubscribeTickVO;

/**
 * Generic FIX market data service
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class FixMarketDataServiceImpl extends FixMarketDataServiceBase implements InitializingServiceI {

    private static final long serialVersionUID = 4880040246465806082L;

    private static Logger logger = MyLogger.getLogger(FixMarketDataServiceImpl.class.getName());

    private FixSessionLifecycle lifeCycle;

    public void setFixSessionLifecycle(FixSessionLifecycle lifeCycle) {
        this.lifeCycle = lifeCycle;
    }

    public FixSessionLifecycle getFixSessionLifecycle() {
        return this.lifeCycle;
    }

    @Override
    protected void handleInit() throws Exception {

        try {
            getFixAdapter().createSession(getSessionQualifier());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void handleInitSubscriptions() {

        if (getFixSessionLifecycle().subscribe()) {
            super.handleInitSubscriptions();
        }
    }

    @Override
    protected void handleSubscribe(Security security) throws Exception {

        if (!getFixSessionLifecycle().isLoggedOn()) {
            throw new FixMarketDataServiceException("Fix session is not logged on to subscribe " + security);
        }

        // make sure SecurityFamily is initialized
        security.getSecurityFamilyInitialized();

        // create the SubscribeTickEvent (must happen before reqMktData so that Esper is ready to receive marketdata)
        Tick tick = Tick.Factory.newInstance();
        tick.setSecurity(security);
        tick.setFeedType(getFeedType());

        int tickerId = getTickerId(security);

        // create the SubscribeTickEvent and propagate it
        SubscribeTickVO subscribeTickEvent = new SubscribeTickVO();
        subscribeTickEvent.setTick(tick);
        subscribeTickEvent.setTickerId(tickerId);

        EngineLocator.instance().getBaseEngine().sendEvent(subscribeTickEvent);

        sendSubscribeRequest(security);

        logger.debug("request market data for : " + security);
    }

    @Override
    protected void handleUnsubscribe(Security security) throws Exception {

        if (!getFixSessionLifecycle().isSubscribed()) {
            throw new IBNativeMarketDataServiceException("Fix session ist not subscribed, security cannot be unsubscribed " + security);
        }

        sendUnsubscribeRequest(security);

        EngineLocator.instance().getBaseEngine().executeQuery("delete from TickWindow where security.id = " + security.getId());

        logger.debug("cancelled market data for : " + security);
    }
}

