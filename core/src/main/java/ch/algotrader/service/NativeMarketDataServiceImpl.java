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
package ch.algotrader.service;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.security.Security;
import ch.algotrader.esper.Engine;
import ch.algotrader.vo.marketData.SubscribeTickVO;

/**
 * Common functionality for native {@link ExternalMarketDataService} implementations.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public abstract class NativeMarketDataServiceImpl implements ExternalMarketDataService {

    private final Engine serverEngine;

    public NativeMarketDataServiceImpl(final Engine serverEngine) {

        Validate.notNull(serverEngine, "Engine is null");

        this.serverEngine = serverEngine;
    }

    protected final void esperSubscribe(Security security, String tickerId) {

        SubscribeTickVO subscribeTickEvent = new SubscribeTickVO(tickerId, security.getId(), getFeedType());

        this.serverEngine.sendEvent(subscribeTickEvent);
    }

    protected final String esperUnsubscribe(Security security) {

        String tickerId = (String) this.serverEngine.executeSingelObjectQuery(
                "select tickerId from TickWindow where securityId = " + security.getId(),
                "tickerId");
        if (tickerId == null) {
            throw new ServiceException("tickerId for security " + security + " was not found");
        }

        this.serverEngine.executeQuery("delete from TickWindow where securityId = " + security.getId());
        return tickerId;
    }

}
