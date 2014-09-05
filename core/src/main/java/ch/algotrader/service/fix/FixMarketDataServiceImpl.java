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
package ch.algotrader.service.fix;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.fix.FixSessionLifecycle;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.service.ExternalMarketDataServiceImpl;
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
public abstract class FixMarketDataServiceImpl extends ExternalMarketDataServiceImpl implements FixMarketDataService, InitializingServiceI {

    private static final long serialVersionUID = 4880040246465806082L;

    private static Logger logger = MyLogger.getLogger(FixMarketDataServiceImpl.class.getName());

    private final FixSessionLifecycle lifeCycle;

    private final FixAdapter fixAdapter;

    public FixMarketDataServiceImpl(final FixSessionLifecycle lifeCycle,
            final FixAdapter fixAdapter,
            final SecurityDao securityDao) {

        super(securityDao);

        Validate.notNull(lifeCycle, "FixSessionLifecycle is null");
        Validate.notNull(fixAdapter, "FixAdapter is null");

        this.lifeCycle = lifeCycle;
        this.fixAdapter = fixAdapter;
    }

    protected FixAdapter getFixAdapter() {

        return this.fixAdapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {

        try {
            this.fixAdapter.createSession(getSessionQualifier());
        } catch (Exception ex) {
            throw new FixMarketDataServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void initSubscriptions() {

        try {
            if (this.lifeCycle.subscribe()) {
                super.initSubscriptions();
            }
        } catch (Exception ex) {
            throw new FixMarketDataServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void subscribe(Security security) {

        Validate.notNull(security, "Security is null");

        try {
            if (!this.lifeCycle.isLoggedOn()) {
                throw new FixMarketDataServiceException("Fix session is not logged on to subscribe " + security);
            }

            // make sure SecurityFamily is initialized
            security.getSecurityFamilyInitialized();

            // create the SubscribeTickEvent (must happen before reqMktData so that Esper is ready to receive marketdata)
            Tick tick = Tick.Factory.newInstance();
            tick.setSecurity(security);
            tick.setFeedType(getFeedType());

            String tickerId = getTickerId(security);

            // create the SubscribeTickEvent and propagate it
            SubscribeTickVO subscribeTickEvent = new SubscribeTickVO();
            subscribeTickEvent.setTick(tick);
            subscribeTickEvent.setTickerId(tickerId);

            EngineLocator.instance().getBaseEngine().sendEvent(subscribeTickEvent);

            sendSubscribeRequest(security);

            logger.debug("request market data for : " + security);
        } catch (Exception ex) {
            throw new FixMarketDataServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void unsubscribe(Security security) {

        Validate.notNull(security, "Security is null");

        try {
            if (!this.lifeCycle.isSubscribed()) {
                throw new IBNativeMarketDataServiceException("Fix session ist not subscribed, security cannot be unsubscribed " + security);
            }

            sendUnsubscribeRequest(security);

            EngineLocator.instance().getBaseEngine().executeQuery("delete from TickWindow where security.id = " + security.getId());

            logger.debug("cancelled market data for : " + security);
        } catch (Exception ex) {
            throw new FixMarketDataServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void sendSubscribeRequest(Security security);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void sendUnsubscribeRequest(Security security);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String getSessionQualifier();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String getTickerId(Security security);

}
