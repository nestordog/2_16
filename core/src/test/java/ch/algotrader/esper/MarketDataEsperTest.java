package ch.algotrader.esper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPOnDemandQueryResult;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;
import com.espertech.esperio.AdapterCoordinator;
import com.espertech.esperio.AdapterCoordinatorImpl;

import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.TradingStatus;
import ch.algotrader.esper.io.CollectionInputAdapter;
import ch.algotrader.service.CalendarService;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.vo.TradingStatusEventVO;
import ch.algotrader.vo.marketData.AskVO;
import ch.algotrader.vo.marketData.BidVO;
import ch.algotrader.vo.marketData.SubscribeTickVO;
import ch.algotrader.vo.marketData.TradeVO;
import ch.algotrader.vo.marketData.TradingHaltVO;

@RunWith(MockitoJUnitRunner.class)
public class MarketDataEsperTest extends EsperTestBase {

    @Mock
    private LookupService lookupService;
    @Mock
    private CalendarService calendarService;
    @Mock
    private MarketDataService marketDataService;

    private EPServiceProvider epService;
    private EPRuntime epRuntime;

    private SecurityFamily usdFx;
    private Exchange exchange;
    private Forex eurusd;
    private Forex chfusd;

    @Before
    public void setupEsper() throws Exception {

        Configuration config = new Configuration();
        config.configure("/META-INF/esper-common.cfg.xml");
        config.configure("/META-INF/esper-core.cfg.xml");
        config.getEngineDefaults().getExpression().setMathContext(new MathContext(3, RoundingMode.HALF_EVEN));

        this.epService = EPServiceProviderManager.getDefaultProvider(config);
        this.epRuntime = this.epService.getEPRuntime();
        this.epRuntime.setVariableValue("lookupService", this.lookupService);
        this.epRuntime.setVariableValue("calendarService", this.calendarService);
        this.epRuntime.setVariableValue("marketDataService", this.marketDataService);

        this.usdFx = SecurityFamily.Factory.newInstance();
        this.usdFx.setId(1);
        this.usdFx.setSymbolRoot("USD FX");
        this.usdFx.setCurrency(Currency.USD);
        this.usdFx.setTickSizePattern("0<0.00005");
        this.usdFx.setTradeable(true);
        this.usdFx.setScale(4);
        this.exchange = Exchange.Factory.newInstance("exchange", "GMT");
        this.exchange.setId(5L);
        this.usdFx.setExchange(this.exchange);

        this.eurusd = Forex.Factory.newInstance();
        this.eurusd.setId(1);
        this.eurusd.setSymbol("EUR.USD");
        this.eurusd.setBaseCurrency(Currency.EUR);
        this.eurusd.setSecurityFamily(this.usdFx);

        this.chfusd = Forex.Factory.newInstance();
        this.chfusd.setId(2);
        this.chfusd.setSymbol("NZD.USD");
        this.chfusd.setBaseCurrency(Currency.CHF);
        this.chfusd.setSecurityFamily(this.usdFx);

        Mockito.when(this.lookupService.getSecurity(1L)).thenReturn(this.eurusd);
        Mockito.when(this.lookupService.getSecurity(2L)).thenReturn(this.chfusd);
        Mockito.when(this.lookupService.getSecurityFamilyBySecurity(1L)).thenReturn(this.usdFx);
        Mockito.when(this.lookupService.getSecurityFamilyBySecurity(2L)).thenReturn(this.usdFx);
        Mockito.when(this.lookupService.getExchangeBySecurity(1L)).thenReturn(this.exchange);
        Mockito.when(this.lookupService.getExchangeBySecurity(2L)).thenReturn(this.exchange);
    }

    @After
    public void cleanUpEsper() {
        if (this.epService != null) {
            this.epService.destroy();
        }
    }

    @Test
    public void testTicksOnTradeAskBid() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-market-data.epl"),
                "TICK_WINDOW", "INSERT_INTO_TICK_WINDOW",
                "UPDATE_TICK_WINDOW_FROM_TRADE", "UPDATE_TICK_WINDOW_FROM_BID", "UPDATE_TICK_WINDOW_FROM_ASK",
                "INCOMING_TICK", "VALIDATE_TICK");

        final Queue<Map<?, ?>> unvalidatedTickQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("INCOMING_TICK");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final Map<?, ?> event) {
                unvalidatedTickQueue.add(event);
            }
        });
        final Queue<TickVO> validatedTickQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement2 = this.epService.getEPAdministrator().getStatement("VALIDATE_TICK");
        Assert.assertNotNull(statement2);
        statement2.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final TickVO event) {
                validatedTickQueue.add(event);
            }
        });

        Mockito.when(this.calendarService.isOpen(Mockito.anyLong(), Mockito.<Date>any())).thenReturn(true);
        Mockito.when(this.marketDataService.isTickValid(Mockito.any())).thenReturn(false);

        SubscribeTickVO subscribeEvent1 = new SubscribeTickVO("some-ticker1", 1L, FeedType.IB.name());
        this.epRuntime.sendEvent(subscribeEvent1);
        SubscribeTickVO subscribeEvent2 = new SubscribeTickVO("some-ticker2", 2L, FeedType.BB.name());
        this.epRuntime.sendEvent(subscribeEvent2);
        SubscribeTickVO subscribeEvent3 = new SubscribeTickVO("some-ticker3", 1L, FeedType.LMAX.name());
        this.epRuntime.sendEvent(subscribeEvent3);

        EPOnDemandQueryResult result = this.epRuntime.executeQuery("select * from TickWindow");

        EventBean[] entries = result.getArray();
        Assert.assertEquals(3, entries.length);
        EventBean entry1 = entries[0];
        Assert.assertEquals("some-ticker1", entry1.get("tickerId"));
        Assert.assertEquals(FeedType.IB.name(), entry1.get("feedType"));
        Assert.assertEquals(1L, entry1.get("securityId"));
        Assert.assertEquals(false, entry1.get("refresh"));
        EventBean entry2 = entries[1];
        Assert.assertEquals("some-ticker2", entry2.get("tickerId"));
        Assert.assertEquals(FeedType.BB.name(), entry2.get("feedType"));
        Assert.assertEquals(2L, entry2.get("securityId"));
        Assert.assertEquals(false, entry2.get("refresh"));
        EventBean entry3 = entries[2];
        Assert.assertEquals("some-ticker3", entry3.get("tickerId"));
        Assert.assertEquals(FeedType.LMAX.name(), entry3.get("feedType"));
        Assert.assertEquals(1L, entry3.get("securityId"));
        Assert.assertEquals(false, entry3.get("refresh"));

        Assert.assertEquals(0, unvalidatedTickQueue.size());
        unvalidatedTickQueue.clear();
        Assert.assertEquals(0, validatedTickQueue.size());

        Mockito.when(this.marketDataService.isTickValid(Mockito.any())).thenReturn(true);

        TradeVO trade = new TradeVO("some-ticker1", FeedType.IB.name(), new Date(), 1.23d, 567);
        this.epRuntime.sendEvent(trade);

        Assert.assertEquals(1, unvalidatedTickQueue.size());
        unvalidatedTickQueue.clear();
        final TickVO tick1 = validatedTickQueue.remove();

        Assert.assertEquals(1l, tick1.getSecurityId());
        Assert.assertEquals(FeedType.IB.name(), tick1.getFeedType());
        Assert.assertEquals(new BigDecimal("1.2300"), tick1.getLast());
        Assert.assertEquals(567, tick1.getVol());
        Assert.assertEquals(null, tick1.getAsk());
        Assert.assertEquals(0, tick1.getVolAsk());
        Assert.assertEquals(null, tick1.getBid());
        Assert.assertEquals(0, tick1.getVolBid());

        AskVO ask = new AskVO("some-ticker1", FeedType.IB.name(), new Date(), 1.24d, 568);
        this.epRuntime.sendEvent(ask);

        final TickVO tick2 = validatedTickQueue.remove();
        Assert.assertEquals(1l, tick2.getSecurityId());
        Assert.assertEquals(FeedType.IB.name(), tick2.getFeedType());
        Assert.assertEquals(new BigDecimal("1.2300"), tick2.getLast());
        Assert.assertEquals(567, tick2.getVol());
        Assert.assertEquals(new BigDecimal("1.2400"), tick2.getAsk());
        Assert.assertEquals(568, tick2.getVolAsk());
        Assert.assertEquals(null, tick2.getBid());
        Assert.assertEquals(0, tick2.getVolBid());

        BidVO bid = new BidVO("some-ticker1", FeedType.IB.name(), new Date(), 1.25d, 569);
        this.epRuntime.sendEvent(bid);

        final TickVO tick3 = validatedTickQueue.remove();
        Assert.assertEquals(1l, tick3.getSecurityId());
        Assert.assertEquals(FeedType.IB.name(), tick3.getFeedType());
        Assert.assertEquals(new BigDecimal("1.2300"), tick3.getLast());
        Assert.assertEquals(567, tick3.getVol());
        Assert.assertEquals(new BigDecimal("1.2400"), tick3.getAsk());
        Assert.assertEquals(568, tick3.getVolAsk());
        Assert.assertEquals(new BigDecimal("1.2500"), tick3.getBid());
        Assert.assertEquals(569, tick3.getVolBid());
    }

    @Test
    public void testMarketNotOpen() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-market-data.epl"),
                "TICK_WINDOW", "INSERT_INTO_TICK_WINDOW", "UPDATE_TICK_WINDOW_FROM_TRADE",  "INCOMING_TICK");

        final Queue<Map<?, ?>> unvalidatedTickQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("INCOMING_TICK");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final Map<?, ?> event) {
                unvalidatedTickQueue.add(event);
            }
        });

        Mockito.when(this.calendarService.isOpen(Mockito.anyLong(), Mockito.<Date>any())).thenReturn(false);

        SubscribeTickVO subscribeEvent1 = new SubscribeTickVO("some-ticker1", 1L, FeedType.IB.name());
        this.epRuntime.sendEvent(subscribeEvent1);

        TradeVO trade = new TradeVO("some-ticker1", FeedType.IB.name(), new Date(), 1.23d, 567);
        this.epRuntime.sendEvent(trade);

        Mockito.verify(this.calendarService, Mockito.times(1)).isOpen(Mockito.eq(this.exchange.getId()), Mockito.any());
        Assert.assertEquals(0, unvalidatedTickQueue.size());
    }

    @Test
    public void testTickFailedValidation() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-market-data.epl"),
                "TICK_WINDOW", "INSERT_INTO_TICK_WINDOW",
                "UPDATE_TICK_WINDOW_FROM_TRADE", "UPDATE_TICK_WINDOW_FROM_BID", "UPDATE_TICK_WINDOW_FROM_ASK",
                "INCOMING_TICK", "VALIDATE_TICK");

        final Queue<Map<?, ?>> unvalidatedTickQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("INCOMING_TICK");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final Map<?, ?> event) {
                unvalidatedTickQueue.add(event);
            }
        });
        final Queue<TickVO> validatedTickQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement2 = this.epService.getEPAdministrator().getStatement("VALIDATE_TICK");
        statement2.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final TickVO event) {
                validatedTickQueue.add(event);
            }
        });

        Mockito.when(this.calendarService.isOpen(Mockito.anyLong(), Mockito.<Date>any())).thenReturn(true);
        Mockito.when(this.marketDataService.isTickValid(Mockito.any())).thenReturn(false);

        SubscribeTickVO subscribeEvent1 = new SubscribeTickVO("some-ticker1", 1L, FeedType.IB.name());
        this.epRuntime.sendEvent(subscribeEvent1);

        Mockito.when(this.marketDataService.isTickValid(Mockito.any())).thenReturn(true);

        TradeVO trade = new TradeVO("some-ticker1", FeedType.IB.name(), new Date(), 1.23d, 567);
        this.epRuntime.sendEvent(trade);

        Assert.assertEquals(1, unvalidatedTickQueue.size());
        final TickVO tick1 = validatedTickQueue.remove();

        Assert.assertEquals(1l, tick1.getSecurityId());
        Assert.assertEquals(FeedType.IB.name(), tick1.getFeedType());
        Assert.assertEquals(new BigDecimal("1.2300"), tick1.getLast());
        Assert.assertEquals(567, tick1.getVol());

        Assert.assertEquals(0, validatedTickQueue.size());
    }

    @Test
    public void testMarketDataGap() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-market-data.epl"),
                "CHECK_TICK_GAPS", "PROPAGATE_MARKET_DATA_EVENTS");
        final Queue<Long> securityWithGapQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("CHECK_TICK_GAPS");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final long securityId) {
                securityWithGapQueue.add(securityId);
            }
        });

        Mockito.when(this.calendarService.isOpen(Mockito.anyLong(), Mockito.<Date>any())).thenReturn(true);

        this.usdFx.setMaxGap(1);

        LocalDateTime start = LocalDateTime.of(2015, Month.JUNE, 1, 12, 0);
        TickVO tick1 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(5)), FeedType.IB.name(), this.usdFx.getId(), 0, 0, 0);
        TickVO tick2 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(5)), FeedType.IB.name(), this.chfusd.getId(), 0, 0, 0);
        TickVO tick3 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(10)), FeedType.IB.name(), this.usdFx.getId(), 0, 0, 0);
        TickVO tick4 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(10)), FeedType.IB.name(), this.chfusd.getId(), 0, 0, 0);
        TickVO tick5 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(50)), FeedType.IB.name(), this.chfusd.getId(), 0, 0, 0);
        TickVO tick6 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(100)), FeedType.IB.name(), this.chfusd.getId(), 0, 0, 0);
        TickVO tick7 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(120)), FeedType.IB.name(), this.usdFx.getId(), 0, 0, 0);
        TickVO tick8 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(120)), FeedType.IB.name(), this.chfusd.getId(), 0, 0, 0);

        this.epRuntime.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
        this.epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.toLocalDateTime(start).getTime()));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(this.epService, true, true, true);
        CollectionInputAdapter adapter = new CollectionInputAdapter(Arrays.asList(tick1, tick2, tick3, tick4, tick5, tick6, tick7, tick8), "dateTime");
        coordinator.coordinate(adapter);
        coordinator.start();

        Mockito.verify(this.calendarService, Mockito.times(1)).isOpen(Mockito.eq(this.exchange.getId()), Mockito.any());
        Long securityWithGap = securityWithGapQueue.remove();
        Assert.assertEquals(Long.valueOf(1L), securityWithGap);
        Assert.assertEquals(0, securityWithGapQueue.size());
    }

    @Test
    public void testTradingStatusEvent() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-market-data.epl"),
                "TICK_WINDOW", "INSERT_INTO_TICK_WINDOW",
                "TRADING_CONTEXT", "TRADING_HALTED", "TRADING_RESUMED");

        final Queue<TradingStatusEventVO> tradingStatusEventQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement = this.epService.getEPAdministrator().createEPL("select * from TradingStatusEventVO");
        statement.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final TradingStatusEventVO event) {
                tradingStatusEventQueue.add(event);
            }
        });

        SubscribeTickVO subscribeEvent1 = new SubscribeTickVO("some-ticker1", 1L, FeedType.IB.name());
        this.epRuntime.sendEvent(subscribeEvent1);
        SubscribeTickVO subscribeEvent3 = new SubscribeTickVO("some-ticker2", 2L, FeedType.LMAX.name());
        this.epRuntime.sendEvent(subscribeEvent3);

        TradingHaltVO halt1 = new TradingHaltVO("some-ticker1", FeedType.IB.name(), null);
        this.epRuntime.sendEvent(halt1);
        TradingHaltVO halt2 = new TradingHaltVO("some-ticker2", FeedType.LMAX.name(), null);
        this.epRuntime.sendEvent(halt2);
        TradingHaltVO halt3 = new TradingHaltVO("some-ticker1", FeedType.IB.name(), null);
        this.epRuntime.sendEvent(halt3);
        TradingHaltVO halt4 = new TradingHaltVO("some-ticker2", FeedType.LMAX.name(), null);
        this.epRuntime.sendEvent(halt4);

        TradingStatusEventVO tradingStatusEvent1 = tradingStatusEventQueue.poll();
        Assert.assertNotNull(tradingStatusEvent1);
        Assert.assertEquals(TradingStatus.TRADING_HALT, tradingStatusEvent1.getStatus());
        Assert.assertEquals(1L, tradingStatusEvent1.getSecurityId());
        Assert.assertEquals(FeedType.IB.name(), tradingStatusEvent1.getFeedType());

        TradingStatusEventVO tradingStatusEvent2 = tradingStatusEventQueue.poll();
        Assert.assertNotNull(tradingStatusEvent2);
        Assert.assertEquals(TradingStatus.TRADING_HALT, tradingStatusEvent2.getStatus());
        Assert.assertEquals(2L, tradingStatusEvent2.getSecurityId());
        Assert.assertEquals(FeedType.LMAX.name(), tradingStatusEvent2.getFeedType());

        TradingStatusEventVO tradingStatusEvent3 = tradingStatusEventQueue.poll();
        Assert.assertNotNull(tradingStatusEvent3);
        Assert.assertEquals(TradingStatus.TRADING_HALT, tradingStatusEvent3.getStatus());
        Assert.assertEquals(1L, tradingStatusEvent3.getSecurityId());
        Assert.assertEquals(FeedType.IB.name(), tradingStatusEvent3.getFeedType());

        TradingStatusEventVO tradingStatusEvent4 = tradingStatusEventQueue.poll();
        Assert.assertNotNull(tradingStatusEvent4);
        Assert.assertEquals(TradingStatus.TRADING_HALT, tradingStatusEvent4.getStatus());
        Assert.assertEquals(2L, tradingStatusEvent4.getSecurityId());
        Assert.assertEquals(FeedType.LMAX.name(), tradingStatusEvent4.getFeedType());

        TradeVO trade1 = new TradeVO("some-ticker1", FeedType.IB.name(), new Date(), 1.23d, 567);
        this.epRuntime.sendEvent(trade1);

        TradingStatusEventVO tradingStatusEvent5 = tradingStatusEventQueue.poll();
        Assert.assertNotNull(tradingStatusEvent5);
        Assert.assertEquals(TradingStatus.READY_TO_TRADE, tradingStatusEvent5.getStatus());
        Assert.assertEquals(1L, tradingStatusEvent5.getSecurityId());
        Assert.assertEquals(FeedType.IB.name(), tradingStatusEvent5.getFeedType());

        TradingStatusEventVO tradingStatusEvent6 = tradingStatusEventQueue.poll();
        Assert.assertNull(tradingStatusEvent6);

        BidVO bid1 = new BidVO("some-ticker1", FeedType.IB.name(), new Date(), 1.23d, 567);
        this.epRuntime.sendEvent(bid1);

        TradingStatusEventVO tradingStatusEvent7 = tradingStatusEventQueue.poll();
        Assert.assertNull(tradingStatusEvent7);

        AskVO ask1 = new AskVO("some-ticker2", FeedType.LMAX.name(), new Date(), 1.23d, 567);
        this.epRuntime.sendEvent(ask1);

        TradingStatusEventVO tradingStatusEvent8 = tradingStatusEventQueue.poll();
        Assert.assertNotNull(tradingStatusEvent8);
        Assert.assertEquals(TradingStatus.READY_TO_TRADE, tradingStatusEvent8.getStatus());
        Assert.assertEquals(2L, tradingStatusEvent8.getSecurityId());
        Assert.assertEquals(FeedType.LMAX.name(), tradingStatusEvent8.getFeedType());

        TradingStatusEventVO tradingStatusEvent9 = tradingStatusEventQueue.poll();
        Assert.assertNull(tradingStatusEvent9);
    }

}

