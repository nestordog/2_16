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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.adapter.RequestIdGenerator;
import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.InitializationPriority;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.ServiceException;
import ch.algotrader.vo.marketData.SubscribeTickVO;

/**
 * Generic FIX market data service
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
@InitializationPriority(InitializingServiceType.BROKER_INTERFACE)
public abstract class FixMarketDataServiceImpl implements FixMarketDataService, InitializingServiceI {

    private static final Logger LOGGER = LogManager.getLogger(FixMarketDataServiceImpl.class);

    private final String feedType;
    private final String sessionQualifier;
    private final ExternalSessionStateHolder stateHolder;
    private final FixAdapter fixAdapter;
    private final RequestIdGenerator<Security> tickerIdGenerator;
    private final Engine serverEngine;

    public FixMarketDataServiceImpl(
            final String feedType,
            final String sessionQualifier,
            final ExternalSessionStateHolder stateHolder,
            final FixAdapter fixAdapter,
            final RequestIdGenerator<Security> tickerIdGenerator,
            final Engine serverEngine) {

        Validate.notEmpty(feedType, "FeedType is null");
        Validate.notEmpty(sessionQualifier, "SessionQualifier is empty");
        Validate.notNull(stateHolder, "FixSessionStateHolder is null");
        Validate.notNull(fixAdapter, "FixAdapter is null");
        Validate.notNull(tickerIdGenerator, "RequestIdGenerator is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.feedType = feedType;
        this.sessionQualifier = sessionQualifier;
        this.stateHolder = stateHolder;
        this.fixAdapter = fixAdapter;
        this.tickerIdGenerator = tickerIdGenerator;
        this.serverEngine = serverEngine;
    }

    protected FixAdapter getFixAdapter() {

        return this.fixAdapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {

        this.fixAdapter.createSession(getSessionQualifier());
    }

    @Override
    public boolean initSubscriptions() {

        return this.stateHolder.onSubscribe();
    }

    @Override
    public void subscribe(Security security) {

        Validate.notNull(security, "Security is null");

        if (!this.stateHolder.isLoggedOn()) {
            throw new ServiceException("Fix session is not logged on to subscribe " + security);
        }

        // create the SubscribeTickEvent and propagate it
        String tickerId = getTickerId(security);
        SubscribeTickVO subscribeTickEvent = new SubscribeTickVO(tickerId, security.getId(), getFeedType());

        this.serverEngine.sendEvent(subscribeTickEvent);

        sendSubscribeRequest(security);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("request market data for : {}", security);
        }

    }

    @Override
    public void unsubscribe(Security security) {

        Validate.notNull(security, "Security is null");

        if (!this.stateHolder.isSubscribed()) {
            throw new ServiceException("Fix session ist not subscribed, security cannot be unsubscribed " + security);
        }

        sendUnsubscribeRequest(security);

        this.serverEngine.executeQuery("delete from TickWindow where securityId = " + security.getId());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("cancelled market data for : {}", security);
        }

    }

    @Override
    public final String getTickerId(final Security security) {

        return this.tickerIdGenerator.generateId(security);
    }

    @Override
    public final String getFeedType() {

        return this.feedType;
    }

    @Override
    public final String getSessionQualifier() {

        return this.sessionQualifier;
    }
}
