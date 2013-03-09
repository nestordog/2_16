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
package com.algoTrader.esper.io;

import java.util.Map;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.marketData.Bar;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.strategy.StrategyImpl;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.util.metric.MetricsUtil;
import com.algoTrader.vo.RawBarVO;
import com.algoTrader.vo.RawTickVO;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esperio.AbstractSendableEvent;
import com.espertech.esperio.AbstractSender;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CustomSender extends AbstractSender {

    @Override
    public void sendEvent(AbstractSendableEvent event, Object beanToSend) {


        // raw Ticks are always sent using MarketDataService
        if (beanToSend instanceof RawTickVO) {

            long beforeCompleteRawT = System.nanoTime();
            Tick tick = ServiceLocator.instance().getLookupService().getTickFromRawTick((RawTickVO) beanToSend);
            long afterCompleteRaw = System.nanoTime();

            long beforeSendEvent = System.nanoTime();
            EsperManager.sendEvent(StrategyImpl.BASE, tick);
            long afterSendEvent = System.nanoTime();

            MetricsUtil.account("CustomSender.completeRaw", (afterCompleteRaw - beforeCompleteRawT));
            MetricsUtil.account("CustomSender.sendMarketDataEvent", (afterSendEvent - beforeSendEvent));

            // Bars are always sent using MarketDataService
        } else if (beanToSend instanceof RawBarVO) {

            long beforeCompleteRawT = System.nanoTime();
            Bar bar = ServiceLocator.instance().getLookupService().getBarFromRawBar((RawBarVO) beanToSend);
            long afterCompleteRaw = System.nanoTime();

            long beforeSendEvent = System.nanoTime();
            EsperManager.sendEvent(StrategyImpl.BASE, bar);
            long afterSendEvent = System.nanoTime();

            MetricsUtil.account("CustomSender.completeRaw", (afterCompleteRaw - beforeCompleteRawT));
            MetricsUtil.account("CustomSender.sendMarketDataEvent", (afterSendEvent - beforeSendEvent));

            // currentTimeEvents are sent to all started strategies
        } else if (beanToSend instanceof CurrentTimeEvent) {

            long beforeSendEvent = System.nanoTime();
            EsperManager.setCurrentTime((CurrentTimeEvent) beanToSend);
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
