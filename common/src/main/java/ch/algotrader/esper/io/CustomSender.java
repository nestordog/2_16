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
package ch.algotrader.esper.io;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.CurrentTimeSpanEvent;
import com.espertech.esperio.AbstractSendableEvent;
import com.espertech.esperio.AbstractSender;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.marketData.BarVO;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.event.dispatch.EventRecipient;
import ch.algotrader.service.ServerLookupService;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * Custom Esper Sender that initializes Ticks and Bars.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class CustomSender extends AbstractSender {

    private final ConcurrentMap<String, Long> securityIdMap = new ConcurrentHashMap<>();

    private long getSecurity(final String securityString) {
        final ServiceLocator serviceLocator = ServiceLocator.instance();

        //first, lookup the security ID (does normally not change)
        Long securityId = securityIdMap.get(securityString);
        if (securityId == null) {
            // lookup the securityId
            ServerLookupService serverLookupService = ServiceLocator.instance().getService("serverLookupService", ServerLookupService.class);
            securityId = serverLookupService.getSecurityIdBySecurityString(securityString);
            securityIdMap.put(securityString, securityId);//due to racing we may replace an existing entry but that's fine
        }
        return securityId;
    }

    private TickVO rawTickToEvent(final RawTickVO raw) {

        long securityId = getSecurity(raw.getSecurity());
        return new TickVO(0L, raw.getDateTime(), "SIM", securityId, raw.getLast(), raw.getLastDateTime(),
                raw.getBid(), raw.getAsk(), raw.getVol(), raw.getVolAsk(), raw.getVolBid());
    }

    private  BarVO rawBarToEvent(final RawBarVO raw) {

        long securityId = getSecurity(raw.getSecurity());
        return new BarVO(0L, raw.getDateTime(), "SIM", securityId, raw.getBarSize(), raw.getOpen(), raw.getHigh(), raw.getLow(), raw.getClose(), raw.getVol());
    }

    @Override
    public void sendEvent(AbstractSendableEvent event, Object beanToSend) {


        // raw Ticks are always sent using MarketDataService
        if (beanToSend instanceof RawTickVO) {

            long beforeCompleteRawT = System.nanoTime();
            TickVO tick = rawTickToEvent((RawTickVO) beanToSend);
            long afterCompleteRaw = System.nanoTime();

            long beforeSendEvent = System.nanoTime();
            ServiceLocator.instance().getEngineManager().getServerEngine().sendEvent(tick);
            long afterSendEvent = System.nanoTime();

            MetricsUtil.account("CustomSender.completeRaw", (afterCompleteRaw - beforeCompleteRawT));
            MetricsUtil.account("CustomSender.sendMarketDataEvent", (afterSendEvent - beforeSendEvent));

            // Bars are always sent using MarketDataService
        } else if (beanToSend instanceof RawBarVO) {

            long beforeCompleteRawT = System.nanoTime();
            BarVO bar = rawBarToEvent((RawBarVO) beanToSend);
            long afterCompleteRaw = System.nanoTime();

            long beforeSendEvent = System.nanoTime();
            ServiceLocator.instance().getEngineManager().getServerEngine().sendEvent(bar);
            long afterSendEvent = System.nanoTime();

            MetricsUtil.account("CustomSender.completeRaw", (afterCompleteRaw - beforeCompleteRawT));
            MetricsUtil.account("CustomSender.sendMarketDataEvent", (afterSendEvent - beforeSendEvent));

            // currentTimeEvents are sent to all started strategies
        } else if (beanToSend instanceof CurrentTimeEvent || beanToSend instanceof CurrentTimeSpanEvent) {

            long beforeSendEvent = System.nanoTime();
            ServiceLocator.instance().getEventDispatcher().broadcast(beanToSend, EventRecipient.ALL_LOCAL);
            long afterSendEvent = System.nanoTime();

            MetricsUtil.account("CustomSender.sendCurrentTimeEvent", (afterSendEvent - beforeSendEvent));

            // everything else (especially Ticks) are sent to the specified runtime
        } else {
            this.runtime.sendEvent(beanToSend);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void sendEvent(AbstractSendableEvent event, Map mapToSend, String eventTypeName) {
        this.runtime.sendEvent(mapToSend, eventTypeName);
    }

    @Override
    public void onFinish() {
        // do nothing
    }
}
