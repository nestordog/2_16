/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;

import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.esper.Engine;

/**
 * A utility class that propagates tick persistence events to {@link MarketDataService}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TickPersister {

    private final Engine serverEngine;
    private final LookupService lookupService;
    private final MarketDataService marketDataService;

    public TickPersister(
            final Engine serverEngine,
            final LookupService lookupService,
            final MarketDataService marketDataService) {
        this.serverEngine = serverEngine;
        this.lookupService = lookupService;
        this.marketDataService = marketDataService;
    }

    public void persist(final TickVO event, final Map<?, ?> map) throws IOException {

        Tick tick = Tick.Factory.newInstance();

        tick.setSecurity(this.lookupService.getSecurity(event.getSecurityId()));
        tick.setFeedType(event.getFeedType());
        tick.setLast(event.getLast());
        tick.setLastDateTime(event.getLastDateTime());
        tick.setVol(event.getVol());
        tick.setAsk(event.getAsk());
        tick.setVolAsk(event.getVolAsk());
        tick.setBid(event.getBid());
        tick.setVolBid(event.getVolBid());

        // get the current Date rounded to MINUTES
        Date date = DateUtils.round(serverEngine.getCurrentTime(), Calendar.MINUTE);
        tick.setDateTime(date);

        this.marketDataService.persistTick(tick);
    }

}
