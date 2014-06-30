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
package ch.algotrader.service.ib;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import ch.algotrader.adapter.ib.IBUtil;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.MyLogger;
import ch.algotrader.vo.SubscribeTickVO;

import com.ib.client.Contract;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeMarketDataServiceImpl extends IBNativeMarketDataServiceBase implements DisposableBean {

    private static Logger logger = MyLogger.getLogger(IBNativeMarketDataServiceImpl.class.getName());

    @Override
    protected void handleInitSubscriptions() {

        if (getIBSession().getLifecycle().subscribe()) {
            super.handleInitSubscriptions();
        }
    }

    @Override
    protected void handleSubscribe(Security security) throws Exception {

        if (!getIBSession().getLifecycle().isLoggedOn()) {
            throw new IBNativeMarketDataServiceException("IB is not logged on to subscribe " + security);
        }

        // create the SubscribeTickEvent (must happen before reqMktData so that Esper is ready to receive marketdata)
        int tickerId = getIBIdGenerator().getNextRequestId();
        Tick tick = Tick.Factory.newInstance();
        tick.setSecurity(security);
        tick.setFeedType(FeedType.IB);

        SubscribeTickVO subscribeTickEvent = new SubscribeTickVO();
        subscribeTickEvent.setTick(tick);
        subscribeTickEvent.setTickerId(Integer.toString(tickerId));

        EngineLocator.instance().getBaseEngine().sendEvent(subscribeTickEvent);

        // requestMarketData from IB
        Contract contract = IBUtil.getContract(security);

        getIBSession().reqMktData(tickerId, contract, this.getIBConfig().getGenericTickList(), false);

        logger.debug("requested market data for: " + security + " tickerId: " + tickerId);
    }

    @Override
    protected void handleUnsubscribe(Security security) throws Exception {

        if (!getIBSession().getLifecycle().isSubscribed()) {
            throw new IBNativeMarketDataServiceException("IB ist not subscribed, security cannot be unsubscribed " + security);
        }

        // get the tickerId by querying the TickWindow
        String tickerId = getTickDao().findTickerIdBySecurity(security.getId());
        if (tickerId == null) {
            throw new IBNativeMarketDataServiceException("tickerId for security " + security + " was not found");
        }

        getIBSession().cancelMktData(Integer.parseInt(tickerId));

        EngineLocator.instance().getBaseEngine().executeQuery("delete from TickWindow where security.id = " + security.getId());

        logger.debug("cancelled market data for : " + security);
    }

    @Override
    protected FeedType handleGetFeedType() throws Exception {
        return FeedType.IB;
    }

    @Override
    public void destroy() throws Exception {

        getIBSession().disconnect();
    }
}
