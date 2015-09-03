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

import java.util.ArrayList;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import com.ib.client.Contract;

import ch.algotrader.adapter.ib.IBIdGenerator;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.adapter.ib.IBSessionStateHolder;
import ch.algotrader.adapter.ib.IBUtil;
import ch.algotrader.config.IBConfig;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.ExternalMarketDataService;
import ch.algotrader.service.NativeMarketDataServiceImpl;
import ch.algotrader.service.ServiceException;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeMarketDataServiceImpl extends NativeMarketDataServiceImpl implements ExternalMarketDataService, DisposableBean {

    private static final Logger LOGGER = LogManager.getLogger(IBNativeMarketDataServiceImpl.class);

    private final IBSession iBSession;
    private final IBIdGenerator iBIdGenerator;
    private final IBSessionStateHolder sessionStateHolder;
    private final IBConfig iBConfig;

    public IBNativeMarketDataServiceImpl(
            final IBSession iBSession,
            final IBSessionStateHolder sessionStateHolder,
            final IBIdGenerator iBIdGenerator,
            final IBConfig iBConfig,
            final Engine serverEngine) {

        super(serverEngine);

        Validate.notNull(iBSession, "IBSession is null");
        Validate.notNull(sessionStateHolder, "IBSessionStateHolder is null");
        Validate.notNull(iBIdGenerator, "IBIdGenerator is null");
        Validate.notNull(iBConfig, "IBConfig is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.iBSession = iBSession;
        this.sessionStateHolder = sessionStateHolder;
        this.iBIdGenerator = iBIdGenerator;
        this.iBConfig = iBConfig;
    }

    @Override
    public boolean initSubscriptions() {

        return this.sessionStateHolder.onSubscribe();
    }

    @Override
    public void subscribe(Security security) {

        Validate.notNull(security, "Security is null");

        if (!this.sessionStateHolder.isLoggedOn()) {
            throw new ServiceException("IB is not logged on to subscribe " + security);
        }

        // create the SubscribeTickEvent (must happen before reqMktData so that Esper is ready to receive marketdata)
        int tickerId = this.iBIdGenerator.getNextRequestId();
        esperSubscribe(security, Integer.toString(tickerId));

        // requestMarketData from IB
        Contract contract = IBUtil.getContract(security);

        this.iBSession.reqMktData(tickerId, contract, this.iBConfig.getGenericTickList(), false, new ArrayList<>());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("requested market data for: {} tickerId: {}", security, tickerId);
        }

    }

    @Override
    public void unsubscribe(Security security) {

        Validate.notNull(security, "Security is null");

        if (!this.sessionStateHolder.isSubscribed()) {
            throw new ServiceException("IB ist not subscribed, security cannot be unsubscribed " + security);
        }

        String tickerId = esperUnsubscribe(security);

        this.iBSession.cancelMktData(Integer.parseInt(tickerId));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("cancelled market data for : {}", security);
        }

    }

    @Override
    public FeedType getFeedType() {

        return FeedType.IB;
    }

    @Override
    public void destroy() throws Exception {

        this.iBSession.disconnect();
    }
}
