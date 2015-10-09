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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;

import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.Security;
import ch.algotrader.util.io.CsvTickWriter;

/**
 * A utility class that propagates tick persistence events to {@link MarketDataService}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TickPersister {

    private final LookupService lookupService;
    private final MarketDataService marketDataService;
    private final Map<Security, CsvTickWriter> csvWriters;

    public TickPersister(
            final LookupService lookupService,
            final MarketDataService marketDataService) {
        this.lookupService = lookupService;
        this.marketDataService = marketDataService;
        this.csvWriters = new HashMap<>();
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
        Date date = DateUtils.round(event.getDateTime() != null ? event.getDateTime() : new Date(), Calendar.MINUTE);
        tick.setDateTime(date);

        saveCvs(tick);

        this.marketDataService.persistTick(tick);
    }

    private void saveCvs(final Tick tick) throws IOException {

        Security security = tick.getSecurity();

        CsvTickWriter csvWriter;
        synchronized (this.csvWriters) {
            csvWriter = this.csvWriters.get(security);
            if (csvWriter == null) {
                String fileName = security.getIsin() != null ? security.getIsin() : security.getSymbol() != null ? security.getSymbol() : String.valueOf(security.getId());
                csvWriter = new CsvTickWriter(fileName);
                this.csvWriters.put(security, csvWriter);
            }
            csvWriter.write(tick);
        }
    }

    public void destroy() {
        synchronized (this.csvWriters) {
            this.csvWriters.values().forEach(csvTickWriter -> {
                try {
                    csvTickWriter.close();
                } catch (IOException ignore) {
                }
            });
        }
    }

}
