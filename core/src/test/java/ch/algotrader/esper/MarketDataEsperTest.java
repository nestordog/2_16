package ch.algotrader.esper;

import java.math.BigDecimal;
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
import ch.algotrader.esper.io.CollectionInputAdapter;
import ch.algotrader.service.CalendarService;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.vo.marketData.AskVO;
import ch.algotrader.vo.marketData.BidVO;
import ch.algotrader.vo.marketData.SubscribeTickVO;
import ch.algotrader.vo.marketData.TradeVO;

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

        epService = EPServiceProviderManager.getDefaultProvider(config);
        epRuntime = epService.getEPRuntime();
        epRuntime.setVariableValue("lookupService", lookupService);
        epRuntime.setVariableValue("calendarService", calendarService);
        epRuntime.setVariableValue("marketDataService", marketDataService);

        usdFx = SecurityFamily.Factory.newInstance();
        usdFx.setId(1);
        usdFx.setSymbolRoot("USD FX");
        usdFx.setCurrency(Currency.USD);
        usdFx.setTickSizePattern("0<0.00005");
        usdFx.setTradeable(true);
        usdFx.setScale(4);
        exchange = Exchange.Factory.newInstance("exchange", "GMT");
        exchange.setId(5L);
        usdFx.setExchange(exchange);

        eurusd = Forex.Factory.newInstance();
        eurusd.setId(1);
        eurusd.setSymbol("EUR.USD");
        eurusd.setBaseCurrency(Currency.EUR);
        eurusd.setSecurityFamily(usdFx);

        chfusd = Forex.Factory.newInstance();
        chfusd.setId(2);
        chfusd.setSymbol("NZD.USD");
        chfusd.setBaseCurrency(Currency.CHF);
        chfusd.setSecurityFamily(usdFx);

        Mockito.when(lookupService.getSecurity(1L)).thenReturn(eurusd);
        Mockito.when(lookupService.getSecurity(2L)).thenReturn(chfusd);
        Mockito.when(lookupService.getSecurityFamilyBySecurity(1L)).thenReturn(usdFx);
        Mockito.when(lookupService.getSecurityFamilyBySecurity(2L)).thenReturn(usdFx);
        Mockito.when(lookupService.getExchangeBySecurity(1L)).thenReturn(exchange);
        Mockito.when(lookupService.getExchangeBySecurity(2L)).thenReturn(exchange);
    }

    @After
    public void cleanUpEsper() {
        if (epService != null) {
            epService.destroy();
        }
    }

    @Test
    public void testTicksOnTradeAskBid() throws Exception {

        deployModule(epService,
                getClass().getResource("/module-market-data.epl"),
                "TICK_WINDOW", "INSERT_INTO_TICK_WINDOW",
                "UPDATE_TICK_WINDOW_FROM_TRADE", "UPDATE_TICK_WINDOW_FROM_BID", "UPDATE_TICK_WINDOW_FROM_ASK",
                "INCOMING_TICK", "VALIDATE_TICK");

        final Queue<Map<?, ?>> unvalidatedTickQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = epService.getEPAdministrator().getStatement("INCOMING_TICK");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final Map<?, ?> event) {
                unvalidatedTickQueue.add(event);
            }
        });
        final Queue<TickVO> validatedTickQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement2 = epService.getEPAdministrator().getStatement("VALIDATE_TICK");
        Assert.assertNotNull(statement1);
        statement2.setSubscriber(new Object() {
            public void update(final TickVO event) {
                validatedTickQueue.add(event);
            }
        });

        Mockito.when(calendarService.isOpen(Mockito.anyLong(), Mockito.<Date>any())).thenReturn(true);
        Mockito.when(marketDataService.isTickValid(Mockito.any())).thenReturn(false);

        SubscribeTickVO subscribeEvent1 = new SubscribeTickVO("some-ticker1", 1L, FeedType.IB);
        epRuntime.sendEvent(subscribeEvent1);
        SubscribeTickVO subscribeEvent2 = new SubscribeTickVO("some-ticker2", 2L, FeedType.BB);
        epRuntime.sendEvent(subscribeEvent2);
        SubscribeTickVO subscribeEvent3 = new SubscribeTickVO("some-ticker3", 1L, FeedType.LMAX);
        epRuntime.sendEvent(subscribeEvent3);

        EPOnDemandQueryResult result = epRuntime.executeQuery("select * from TickWindow");

        EventBean[] entries = result.getArray();
        Assert.assertEquals(3, entries.length);
        EventBean entry1 = entries[0];
        Assert.assertEquals("some-ticker1", entry1.get("tickerId"));
        Assert.assertEquals(FeedType.IB, entry1.get("feedType"));
        Assert.assertEquals(1L, entry1.get("securityId"));
        Assert.assertEquals(true, entry1.get("refresh"));
        EventBean entry2 = entries[1];
        Assert.assertEquals("some-ticker2", entry2.get("tickerId"));
        Assert.assertEquals(FeedType.BB, entry2.get("feedType"));
        Assert.assertEquals(2L, entry2.get("securityId"));
        Assert.assertEquals(true, entry2.get("refresh"));
        EventBean entry3 = entries[2];
        Assert.assertEquals("some-ticker3", entry3.get("tickerId"));
        Assert.assertEquals(FeedType.LMAX, entry3.get("feedType"));
        Assert.assertEquals(1L, entry3.get("securityId"));
        Assert.assertEquals(true, entry3.get("refresh"));

        Assert.assertEquals(3, unvalidatedTickQueue.size());
        unvalidatedTickQueue.clear();
        Assert.assertEquals(0, validatedTickQueue.size());

        Mockito.when(marketDataService.isTickValid(Mockito.any())).thenReturn(true);

        TradeVO trade = new TradeVO("some-ticker1", FeedType.IB, new Date(), 1.23d, 567);
        epRuntime.sendEvent(trade);

        Assert.assertEquals(1, unvalidatedTickQueue.size());
        unvalidatedTickQueue.clear();
        final TickVO tick1 = validatedTickQueue.remove();

        Assert.assertEquals(1l, tick1.getSecurityId());
        Assert.assertEquals(FeedType.IB, tick1.getFeedType());
        Assert.assertEquals(new BigDecimal("1.2300"), tick1.getLast());
        Assert.assertEquals(567, tick1.getVol());
        Assert.assertEquals(null, tick1.getAsk());
        Assert.assertEquals(0, tick1.getVolAsk());
        Assert.assertEquals(null, tick1.getBid());
        Assert.assertEquals(0, tick1.getVolBid());

        AskVO ask = new AskVO("some-ticker1", FeedType.IB, new Date(), 1.24d, 568);
        epRuntime.sendEvent(ask);

        final TickVO tick2 = validatedTickQueue.remove();
        Assert.assertEquals(1l, tick2.getSecurityId());
        Assert.assertEquals(FeedType.IB, tick2.getFeedType());
        Assert.assertEquals(new BigDecimal("1.2300"), tick2.getLast());
        Assert.assertEquals(567, tick2.getVol());
        Assert.assertEquals(new BigDecimal("1.2400"), tick2.getAsk());
        Assert.assertEquals(568, tick2.getVolAsk());
        Assert.assertEquals(null, tick2.getBid());
        Assert.assertEquals(0, tick2.getVolBid());

        BidVO bid = new BidVO("some-ticker1", FeedType.IB, new Date(), 1.25d, 569);
        epRuntime.sendEvent(bid);

        final TickVO tick3 = validatedTickQueue.remove();
        Assert.assertEquals(1l, tick3.getSecurityId());
        Assert.assertEquals(FeedType.IB, tick3.getFeedType());
        Assert.assertEquals(new BigDecimal("1.2300"), tick3.getLast());
        Assert.assertEquals(567, tick3.getVol());
        Assert.assertEquals(new BigDecimal("1.2400"), tick3.getAsk());
        Assert.assertEquals(568, tick3.getVolAsk());
        Assert.assertEquals(new BigDecimal("1.2500"), tick3.getBid());
        Assert.assertEquals(569, tick3.getVolBid());
    }

    @Test
    public void testMarketNotOpen() throws Exception {

        deployModule(epService,
                getClass().getResource("/module-market-data.epl"),
                "TICK_WINDOW", "INSERT_INTO_TICK_WINDOW", "UPDATE_TICK_WINDOW_FROM_TRADE",  "INCOMING_TICK");

        final Queue<Map<?, ?>> unvalidatedTickQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = epService.getEPAdministrator().getStatement("INCOMING_TICK");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final Map<?, ?> event) {
                unvalidatedTickQueue.add(event);
            }
        });

        Mockito.when(calendarService.isOpen(Mockito.anyLong(), Mockito.<Date>any())).thenReturn(false);

        SubscribeTickVO subscribeEvent1 = new SubscribeTickVO("some-ticker1", 1L, FeedType.IB);
        epRuntime.sendEvent(subscribeEvent1);

        TradeVO trade = new TradeVO("some-ticker1", FeedType.IB, new Date(), 1.23d, 567);
        epRuntime.sendEvent(trade);

        Mockito.verify(calendarService, Mockito.times(2)).isOpen(Mockito.eq(exchange.getId()), Mockito.any());
        Assert.assertEquals(0, unvalidatedTickQueue.size());
    }

    @Test
    public void testTickFailedValidation() throws Exception {

        deployModule(epService,
                getClass().getResource("/module-market-data.epl"),
                "TICK_WINDOW", "INSERT_INTO_TICK_WINDOW",
                "UPDATE_TICK_WINDOW_FROM_TRADE", "UPDATE_TICK_WINDOW_FROM_BID", "UPDATE_TICK_WINDOW_FROM_ASK",
                "INCOMING_TICK", "VALIDATE_TICK");

        final Queue<Map<?, ?>> unvalidatedTickQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = epService.getEPAdministrator().getStatement("INCOMING_TICK");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final Map<?, ?> event) {
                unvalidatedTickQueue.add(event);
            }
        });
        final Queue<TickVO> validatedTickQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement2 = epService.getEPAdministrator().getStatement("VALIDATE_TICK");
        statement2.setSubscriber(new Object() {
            public void update(final TickVO event) {
                validatedTickQueue.add(event);
            }
        });

        Mockito.when(calendarService.isOpen(Mockito.anyLong(), Mockito.<Date>any())).thenReturn(true);
        Mockito.when(marketDataService.isTickValid(Mockito.any())).thenReturn(false);

        SubscribeTickVO subscribeEvent1 = new SubscribeTickVO("some-ticker1", 1L, FeedType.IB);
        epRuntime.sendEvent(subscribeEvent1);

        Mockito.when(marketDataService.isTickValid(Mockito.any())).thenReturn(true);

        TradeVO trade = new TradeVO("some-ticker1", FeedType.IB, new Date(), 1.23d, 567);
        epRuntime.sendEvent(trade);

        Assert.assertEquals(2, unvalidatedTickQueue.size());
        final TickVO tick1 = validatedTickQueue.remove();

        Assert.assertEquals(1l, tick1.getSecurityId());
        Assert.assertEquals(FeedType.IB, tick1.getFeedType());
        Assert.assertEquals(new BigDecimal("1.2300"), tick1.getLast());
        Assert.assertEquals(567, tick1.getVol());

        Assert.assertEquals(0, validatedTickQueue.size());
    }

    @Test
    public void testMarketDataGap() throws Exception {

        deployModule(epService,
                getClass().getResource("/module-market-data.epl"),
                "CHECK_TICK_GAPS", "PROPAGATE_MARKET_DATA_EVENTS");
        final Queue<Long> securityWithGapQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = epService.getEPAdministrator().getStatement("CHECK_TICK_GAPS");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final long securityId) {
                securityWithGapQueue.add(securityId);
            }
        });

        Mockito.when(calendarService.isOpen(Mockito.anyLong(), Mockito.<Date>any())).thenReturn(true);

        usdFx.setMaxGap(1);

        LocalDateTime start = LocalDateTime.of(2015, Month.JUNE, 1, 12, 0);
        TickVO tick1 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(5)), FeedType.IB, usdFx.getId(), 0, 0, 0);
        TickVO tick2 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(5)), FeedType.IB, chfusd.getId(), 0, 0, 0);
        TickVO tick3 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(10)), FeedType.IB, usdFx.getId(), 0, 0, 0);
        TickVO tick4 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(10)), FeedType.IB, chfusd.getId(), 0, 0, 0);
        TickVO tick5 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(50)), FeedType.IB, chfusd.getId(), 0, 0, 0);
        TickVO tick6 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(100)), FeedType.IB, chfusd.getId(), 0, 0, 0);
        TickVO tick7 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(120)), FeedType.IB, usdFx.getId(), 0, 0, 0);
        TickVO tick8 = new TickVO(0L, DateTimeLegacy.toLocalDateTime(start.plusSeconds(120)), FeedType.IB, chfusd.getId(), 0, 0, 0);

        epRuntime.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
        epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.toLocalDateTime(start).getTime()));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(epService, true, true, true);
        CollectionInputAdapter adapter = new CollectionInputAdapter(Arrays.asList(tick1, tick2, tick3, tick4, tick5, tick6, tick7, tick8), "dateTime");
        coordinator.coordinate(adapter);
        coordinator.start();

        Mockito.verify(calendarService, Mockito.times(1)).isOpen(Mockito.eq(exchange.getId()), Mockito.any());
        Long securityWithGap = securityWithGapQueue.remove();
        Assert.assertEquals(Long.valueOf(1L), securityWithGap);
        Assert.assertEquals(0, securityWithGapQueue.size());
    }
}

