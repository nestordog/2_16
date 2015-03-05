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
package ch.algotrader.esper.subscriber;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;

import com.espertech.esper.collection.Pair;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.marketData.Tick;

/**
 * Esper event subscriber for {@link ch.algotrader.service.MarketDataService#persistTick(ch.algotrader.entity.marketData.Tick)}.
 */
public class PersistTickSubscriber {

    private static Logger LOGGER = LogManager.getLogger(PersistTickSubscriber.class.getName());

    @SuppressWarnings("rawtypes")
    public void update(Pair<Tick, Object> insertStream, Map removeStream) {

        Tick tick = insertStream.getFirst();

        try {
            ServiceLocator.instance().getMarketDataService().persistTick(tick);

            // catch duplicate entry errors and log them as warn
        } catch (DataIntegrityViolationException e) {
            LOGGER.warn(e.getRootCause().getMessage());
        }
    }
}
