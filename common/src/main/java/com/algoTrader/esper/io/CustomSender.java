package com.algoTrader.esper.io;

import java.util.Map;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.marketData.Bar;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.service.MarketDataService;
import com.algoTrader.util.MyLogger;
import com.algoTrader.vo.BarVO;
import com.algoTrader.vo.RawTickVO;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esperio.AbstractSendableEvent;
import com.espertech.esperio.AbstractSender;

public class CustomSender extends AbstractSender {

    private long time = 0;
    private static Logger metricsLogger = MyLogger.getLogger("com.algoTrader.metrics.MetricsLogger");

    @Override
    public void sendEvent(AbstractSendableEvent event, Object beanToSend) {

        long start = System.nanoTime();

        // raw Ticks are always sent using MarketDataService
        if (beanToSend instanceof RawTickVO) {

            MarketDataService marketDataService = ServiceLocator.instance().getMarketDataService();

            Tick tick = marketDataService.completeRawTick((RawTickVO) beanToSend);

            long rawTick = System.nanoTime();

            EsperManager.sendEvent(StrategyImpl.BASE, tick);

            long sendEvent = System.nanoTime();
            metricsLogger.trace("custom_sender," + (start - this.time)  + "," + (rawTick - start) + "," + (sendEvent- rawTick));

            // Bars are always sent using MarketDataService
        } else if (beanToSend instanceof BarVO) {

            MarketDataService marketDataService = ServiceLocator.instance().getMarketDataService();

            Bar bar = marketDataService.completeBar((BarVO) beanToSend);

            EsperManager.sendEvent(StrategyImpl.BASE, bar);

            // currentTimeEvents are sent to all started strategies
        } else if (beanToSend instanceof CurrentTimeEvent) {

            EsperManager.setCurrentTime((CurrentTimeEvent) beanToSend);

            // everything else (especially Ticks) are sent to the specified runtime
        } else {
            this.runtime.sendEvent(beanToSend);
        }

        this.time = System.nanoTime();
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
