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
package ch.algotrader.service.bb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Subscription;
import com.bloomberglp.blpapi.SubscriptionList;

import ch.algotrader.adapter.bb.BBAdapter;
import ch.algotrader.adapter.bb.BBIdGenerator;
import ch.algotrader.adapter.bb.BBSession;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.marketData.TickDao;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.ExternalMarketDataServiceImpl;
import ch.algotrader.service.InitializationPriority;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.ib.IBNativeMarketDataServiceException;
import ch.algotrader.vo.SubscribeTickVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@InitializationPriority(InitializingServiceType.BROKER_INTERFACE)
public class BBMarketDataServiceImpl extends ExternalMarketDataServiceImpl implements BBMarketDataService, InitializingServiceI, DisposableBean {

    private static final long serialVersionUID = -3463200344945144471L;

    private static Logger logger = LogManager.getLogger(BBMarketDataServiceImpl.class.getName());
    private static BBSession session;

    private final BBAdapter bBAdapter;
    private final TickDao tickDao;
    private final Engine serverEngine;

    public BBMarketDataServiceImpl(
            final CommonConfig commonConfig,
            final BBAdapter bBAdapter,
            final TickDao tickDao,
            final SecurityDao securityDao,
            final Engine serverEngine            ) {

        super(commonConfig, securityDao);

        Validate.notNull(bBAdapter, "BBAdapter is null");
        Validate.notNull(tickDao, "TickDao is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.bBAdapter = bBAdapter;
        this.tickDao = tickDao;
        this.serverEngine = serverEngine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {

        try {
            session = this.bBAdapter.getMarketDataSession();
        } catch (IOException ex) {
            throw new BBMarketDataServiceException(ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BBMarketDataServiceException(ex);
        }

        // by this time the session is up and running, so no need to do this inside the messageHandler
        initSubscriptions();

    }

    @Override
    public void subscribe(Security security) {

        Validate.notNull(security, "Security is null");

        if (!session.isRunning()) {
            throw new IBNativeMarketDataServiceException("Bloomberg session is not running to subscribe " + security);
        }

        // create the SubscribeTickEvent (must happen before reqMktData so that Esper is ready to receive marketdata)
        String tickerId = BBIdGenerator.getInstance().getNextRequestId();
        Tick tick = Tick.Factory.newInstance();
        tick.setSecurity(security);
        tick.setFeedType(FeedType.BB);

        // create the SubscribeTickEvent and propagate it
        SubscribeTickVO subscribeTickEvent = new SubscribeTickVO();
        subscribeTickEvent.setTick(tick);
        subscribeTickEvent.setTickerId(tickerId);

        this.serverEngine.sendEvent(subscribeTickEvent);

        SubscriptionList subscriptions = getSubscriptionList(security, tickerId);

        try {
            session.subscribe(subscriptions);
        } catch (IOException ex) {
            throw new BBMarketDataServiceException(ex);
        }

        logger.debug("requested market data for: " + security + " tickerId: " + tickerId);

    }

    @Override
    public void unsubscribe(Security security) {

        Validate.notNull(security, "Security is null");

        if (!session.isRunning()) {
            throw new IBNativeMarketDataServiceException("Bloomberg session is not running to unsubscribe " + security);
        }

        // get the tickerId by querying the TickWindow
        String tickerId = this.tickDao.findTickerIdBySecurity(security.getId());
        if (tickerId == null) {
            throw new IBNativeMarketDataServiceException("tickerId for security " + security + " was not found");
        }

        SubscriptionList subscriptions = getSubscriptionList(security, tickerId);

        try {
            session.unsubscribe(subscriptions);
        } catch (IOException ex) {
            throw new BBMarketDataServiceException(ex);
        }

        this.serverEngine.executeQuery("delete from TickWindow where security.id = " + security.getId());

        logger.debug("cancelled market data for : " + security);

    }

    private SubscriptionList getSubscriptionList(Security security, String tickerId) {

        // get the topic
        String topic = "/bbgid/" + security.getBbgid();

        // defined fields
        List<String> fields = new ArrayList<String>();
        fields.add("TRADE_UPDATE_STAMP_RT");
        fields.add("BID_UPDATE_STAMP_RT");
        fields.add("ASK_UPDATE_STAMP_RT");
        fields.add("VOLUME");
        fields.add("LAST_PRICE");
        fields.add("BID");
        fields.add("ASK");
        fields.add("BID_SIZE");
        fields.add("ASK_SIZE");

        // create the subscription list
        SubscriptionList subscriptions = new SubscriptionList();
        subscriptions.add(new Subscription(topic, fields, new CorrelationID(tickerId)));
        return subscriptions;
    }

    @Override
    public FeedType getFeedType() {

        return FeedType.BB;
    }

    @Override
    public void destroy() throws Exception {

        if (session != null && session.isRunning()) {
            session.stop();
        }
    }
}
