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
package ch.algotrader.service.ib;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.adapter.ib.IBIdGenerator;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.adapter.ib.IBUtil;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.MyLogger;
import ch.algotrader.vo.SubscribeTickVO;

import com.ib.client.Contract;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeMarketDataServiceImpl extends IBNativeMarketDataServiceBase implements DisposableBean {

    private static final long serialVersionUID = -4704799803078842628L;
    private static Logger logger = MyLogger.getLogger(IBNativeMarketDataServiceImpl.class.getName());
    private static IBSession session;

    private @Value("${ib.genericTickList}") String genericTickList;

    @Override
    protected void handleInit() throws Exception {

        session = getIBSessionFactory().getDefaultSession();
    }

    @Override
    protected void handleInitSubscriptions() {

        if (session != null && (session.getMessageHandler().getState().getValue() >= ConnectionState.LOGGED_ON.getValue()) && !session.getMessageHandler().isRequested()) {

            session.getMessageHandler().setRequested(true);
            session.getMessageHandler().setState(ConnectionState.SUBSCRIBED);

            super.handleInitSubscriptions();
        }
    }

    @Override
    protected void handleExternalSubscribe(Security security) throws Exception {

        if (session.getMessageHandler().getState().getValue() < ConnectionState.LOGGED_ON.getValue()) {
            throw new IBNativeMarketDataServiceException("IB is not logged on to subscribe " + security);
        }

        // create the SubscribeTickEvent (must happen before reqMktData so that Esper is ready to receive marketdata)
        int tickerId = IBIdGenerator.getInstance().getNextRequestId();
        Tick tick = Tick.Factory.newInstance();
        tick.setSecurity(security);

        SubscribeTickVO subscribeTickEvent = new SubscribeTickVO();
        subscribeTickEvent.setTick(tick);
        subscribeTickEvent.setTickerId(tickerId);

        EngineLocator.instance().getBaseEngine().sendEvent(subscribeTickEvent);

        // requestMarketData from IB
        Contract contract = IBUtil.getContract(security);

        session.reqMktData(tickerId, contract, this.genericTickList, false);

        logger.debug("request " + tickerId + " for : " + security);
    }

    @Override
    protected void handleExternalUnsubscribe(Security security) throws Exception {

        if (!session.getMessageHandler().getState().equals(ConnectionState.SUBSCRIBED)) {
            throw new IBNativeMarketDataServiceException("IB ist not subscribed, security cannot be unsubscribed " + security);
        }

        // get the tickerId by querying the TickWindow
        Integer tickerId = getTickDao().findTickerIdBySecurity(security.getId());
        if (tickerId == null) {
            throw new IBNativeMarketDataServiceException("tickerId for security " + security + " was not found");
        }

        session.cancelMktData(tickerId);

        EngineLocator.instance().getBaseEngine().executeQuery("delete from TickWindow where security.id = " + security.getId());

        logger.debug("cancelled market data for : " + security);
    }

    @Override
    public void destroy() throws Exception {

        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}
