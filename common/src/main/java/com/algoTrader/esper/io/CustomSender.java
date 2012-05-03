package com.algoTrader.esper.io;

import java.util.Map;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.marketData.Bar;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.util.metric.MetricsUtil;
import com.algoTrader.vo.RawBarVO;
import com.algoTrader.vo.RawTickVO;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esperio.AbstractSendableEvent;
import com.espertech.esperio.AbstractSender;

public class CustomSender extends AbstractSender {

    @Override
    public void sendEvent(AbstractSendableEvent event, Object beanToSend) {


        // raw Ticks are always sent using MarketDataService
        if (beanToSend instanceof RawTickVO) {

            long beforeCompleteRawTick = System.nanoTime();
            Tick tick = ServiceLocator.instance().getLookupService().getTickFromRawTick((RawTickVO) beanToSend);
            long afterCompleteRawTick = System.nanoTime();

            long beforeSendEvent = System.nanoTime();
            EsperManager.sendEvent(StrategyImpl.BASE, tick);
            long afterSendEvent = System.nanoTime();

            MetricsUtil.account("CustomSender.completeRawTick", (afterCompleteRawTick - beforeCompleteRawTick));
            MetricsUtil.account("CustomSender.sendTick", (afterSendEvent - beforeSendEvent));

            // Bars are always sent using MarketDataService
        } else if (beanToSend instanceof RawBarVO) {

            Bar bar = ServiceLocator.instance().getLookupService().getBarFromRawBar((RawBarVO) beanToSend);

            EsperManager.sendEvent(StrategyImpl.BASE, bar);

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
