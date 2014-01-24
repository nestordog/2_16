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
package ch.algotrader.service.bb;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import ch.algotrader.adapter.bb.BBIdGenerator;
import ch.algotrader.adapter.bb.BBSession;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.service.ib.IBNativeMarketDataServiceException;
import ch.algotrader.util.MyLogger;
import ch.algotrader.vo.SubscribeTickVO;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Subscription;
import com.bloomberglp.blpapi.SubscriptionList;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class BBMarketDataServiceImpl extends BBMarketDataServiceBase implements DisposableBean {

    private static final long serialVersionUID = -3463200344945144471L;

    private static Logger logger = MyLogger.getLogger(BBMarketDataServiceImpl.class.getName());
    private static BBSession session;

    @Override
    protected void handleInit() throws Exception {

        session = getBBSessionFactory().getMarketDataSession();
    }

    @Override
    protected void handleSubscribe(Security security) throws Exception {

        if (!session.isRunning()) {
            throw new IBNativeMarketDataServiceException("Bloomberg session is not running to subscribe " + security);
        }

        // make sure SecurityFamily is initialized
        security.getSecurityFamilyInitialized();

        // create the SubscribeTickEvent (must happen before reqMktData so that Esper is ready to receive marketdata)
        int tickerId = BBIdGenerator.getInstance().getNextRequestId();
        Tick tick = Tick.Factory.newInstance();
        tick.setSecurity(security);
        tick.setFeedType(FeedType.BB);

        // create the SubscribeTickEvent and propagate it
        SubscribeTickVO subscribeTickEvent = new SubscribeTickVO();
        subscribeTickEvent.setTick(tick);
        subscribeTickEvent.setTickerId(tickerId);

        EngineLocator.instance().getBaseEngine().sendEvent(subscribeTickEvent);

        SubscriptionList subscriptions = getSubscriptionList(security, tickerId);

        session.subscribe(subscriptions);

        logger.debug("requested market data for: " + security + " tickerId: " + tickerId);
    }

    @Override
    protected void handleUnsubscribe(Security security) throws Exception {

        if (!session.isRunning()) {
            throw new IBNativeMarketDataServiceException("Bloomberg session is not running to unsubscribe " + security);
        }

        // get the tickerId by querying the TickWindow
        Integer tickerId = getTickDao().findTickerIdBySecurity(security.getId());
        if (tickerId == null) {
            throw new IBNativeMarketDataServiceException("tickerId for security " + security + " was not found");
        }

        SubscriptionList subscriptions = getSubscriptionList(security, tickerId);

        session.unsubscribe(subscriptions);

        EngineLocator.instance().getBaseEngine().executeQuery("delete from TickWindow where security.id = " + security.getId());

        logger.debug("cancelled market data for : " + security);
    }

    private SubscriptionList getSubscriptionList(Security security, int tickerId) {

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
    protected FeedType handleGetFeedType() throws Exception {
        return FeedType.BB;
    }

    @Override
    public void destroy() throws Exception {

        if (session != null && session.isRunning()) {
            session.stop();
        }
    }
}
