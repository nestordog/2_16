package ch.algotrader.esper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Date;
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
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.ib.client.TickType;

import ch.algotrader.adapter.ib.TickPriceVO;
import ch.algotrader.adapter.ib.TickSizeVO;
import ch.algotrader.adapter.ib.TickStringVO;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.marketData.GenericTickVO;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.service.CalendarService;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.vo.marketData.SubscribeTickVO;

@RunWith(MockitoJUnitRunner.class)
public class IBEsperTest extends EsperTestBase {

    @Mock
    private LookupService lookupService;
    @Mock
    private CalendarService calendarService;
    @Mock
    private MarketDataService marketDataService;

    private EPServiceProvider epService;
    private EPRuntime epRuntime;

    private SecurityFamily family;
    private Exchange exchange;
    private Stock stock;

    @Before
    public void setupEsper() throws Exception {
        Configuration config = new Configuration();
        config.configure("/META-INF/esper-common.cfg.xml");
        config.configure("/META-INF/esper-core.cfg.xml");
        config.getEngineDefaults().getExpression().setMathContext(new MathContext(3, RoundingMode.HALF_EVEN));

        epService = EPServiceProviderManager.getDefaultProvider(config);
        epRuntime = epService.getEPRuntime();
        epRuntime.setVariableValue("lookupService", lookupService);
        epRuntime.setVariableValue("calendarService", calendarService);
        epRuntime.setVariableValue("marketDataService", marketDataService);

        family = SecurityFamily.Factory.newInstance();
        family.setId(1);
        family.setSymbolRoot("Stocks");
        family.setCurrency(Currency.USD);
        family.setTickSizePattern("0<0.00005");
        family.setTradeable(true);
        family.setScale(4);
        exchange = Exchange.Factory.newInstance("exchange", "GMT");
        exchange.setId(5L);
        family.setExchange(exchange);

        stock = Stock.Factory.newInstance();
        stock.setId(1);
        stock.setSymbol("GOOG");
        stock.setSecurityFamily(family);

        Mockito.when(lookupService.getSecurity(1L)).thenReturn(stock);
        Mockito.when(lookupService.getSecurityFamilyBySecurity(1L)).thenReturn(family);
        Mockito.when(lookupService.getExchangeBySecurity(1L)).thenReturn(exchange);
    }

    @After
    public void cleanUpEsper() {
        if (epService != null) {
            epService.destroy();
        }
    }

    @Test
    public void testTicksOnIBTicks() throws Exception {

        deployModule(epService, getClass().getResource("/module-market-data.epl"),
                "TICK_WINDOW", "INSERT_INTO_TICK_WINDOW", "INCOMING_TICK", "VALIDATE_TICK");
        final Queue<TickVO> validatedTickQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = epService.getEPAdministrator().getStatement("VALIDATE_TICK");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final TickVO event) {
                validatedTickQueue.add(event);
            }
        });
        deployModule(epService, getClass().getResource("/module-ib.epl"),
                "UPDATE_TICK_WINDOW_IB_LAST", "UPDATE_TICK_WINDOW_IB_LAST_TIMESTAMP", "UPDATE_TICK_WINDOW_IB_VOL",
                "UPDATE_TICK_WINDOW_IB_BID", "UPDATE_TICK_WINDOW_IB_ASK", "UPDATE_TICK_WINDOW_IB_VOL_BID", "UPDATE_TICK_WINDOW_IB_VOL_ASK");

        Mockito.when(calendarService.isOpen(Mockito.anyLong(), Mockito.<Date>any())).thenReturn(true);
        Mockito.when(marketDataService.isTickValid(Mockito.any())).thenReturn(true);

        SubscribeTickVO subscribeEvent1 = new SubscribeTickVO("some-ticker1", 1L, FeedType.IB);
        epRuntime.sendEvent(subscribeEvent1);

        TickPriceVO tickPrice1 = new TickPriceVO("some-ticker1", TickType.LAST, 1.12222d, 0);
        epRuntime.sendEvent(tickPrice1);
        TickStringVO tickString1 = new TickStringVO("some-ticker1", TickType.LAST_TIMESTAMP, Long.toString(Instant.now().getEpochSecond()));
        epRuntime.sendEvent(tickString1);

        TickPriceVO tickPrice2 = new TickPriceVO("some-ticker1", TickType.BID, 1.13333d, 0);
        epRuntime.sendEvent(tickPrice2);
        TickPriceVO tickPrice3 = new TickPriceVO("some-ticker1", TickType.ASK, 1.10001d, 0);
        epRuntime.sendEvent(tickPrice3);
        TickSizeVO tickSize1 = new TickSizeVO("some-ticker1", TickType.VOLUME, 10);
        epRuntime.sendEvent(tickSize1);
        TickSizeVO tickSize2 = new TickSizeVO("some-ticker1", TickType.BID_SIZE, 9);
        epRuntime.sendEvent(tickSize2);
        TickSizeVO tickSize3 = new TickSizeVO("some-ticker1", TickType.ASK_SIZE, 11);
        epRuntime.sendEvent(tickSize3);

        Assert.assertEquals(8, validatedTickQueue.size());

        final TickVO tick1 = validatedTickQueue.remove();
        Assert.assertEquals(1l, tick1.getSecurityId());
        Assert.assertEquals(FeedType.IB, tick1.getFeedType());
        Assert.assertEquals(null, tick1.getLast());
        Assert.assertEquals(0, tick1.getVol());
        Assert.assertEquals(null, tick1.getAsk());
        Assert.assertEquals(0, tick1.getVolAsk());
        Assert.assertEquals(null, tick1.getBid());
        Assert.assertEquals(0, tick1.getVolBid());

        final TickVO tick2 = validatedTickQueue.remove();
        Assert.assertEquals(1l, tick2.getSecurityId());
        Assert.assertEquals(FeedType.IB, tick2.getFeedType());
        Assert.assertEquals(new BigDecimal("1.1222"), tick2.getLast());
        Assert.assertEquals(0, tick2.getVol());
        Assert.assertEquals(null, tick2.getAsk());
        Assert.assertEquals(0, tick2.getVolAsk());
        Assert.assertEquals(null, tick2.getBid());
        Assert.assertEquals(0, tick2.getVolBid());

        final TickVO tick3 = validatedTickQueue.remove();
        Assert.assertEquals(1l, tick3.getSecurityId());
        Assert.assertEquals(FeedType.IB, tick3.getFeedType());
        Assert.assertEquals(new BigDecimal("1.1222"), tick3.getLast());
        Assert.assertNotNull(tick3.getLastDateTime());
        Assert.assertEquals(0, tick3.getVol());
        Assert.assertEquals(null, tick3.getAsk());
        Assert.assertEquals(0, tick3.getVolAsk());
        Assert.assertEquals(null, tick3.getBid());
        Assert.assertEquals(0, tick3.getVolBid());

        final TickVO tick4 = validatedTickQueue.remove();
        Assert.assertEquals(1l, tick4.getSecurityId());
        Assert.assertEquals(FeedType.IB, tick4.getFeedType());
        Assert.assertEquals(new BigDecimal("1.1222"), tick4.getLast());
        Assert.assertNotNull(tick4.getLastDateTime());
        Assert.assertEquals(0, tick4.getVol());
        Assert.assertEquals(null, tick4.getAsk());
        Assert.assertEquals(0, tick4.getVolAsk());
        Assert.assertEquals(new BigDecimal("1.1333"), tick4.getBid());
        Assert.assertEquals(0, tick4.getVolBid());

        final TickVO tick5 = validatedTickQueue.remove();
        Assert.assertEquals(1l, tick5.getSecurityId());
        Assert.assertEquals(FeedType.IB, tick5.getFeedType());
        Assert.assertEquals(new BigDecimal("1.1222"), tick5.getLast());
        Assert.assertNotNull(tick5.getLastDateTime());
        Assert.assertEquals(0, tick5.getVol());
        Assert.assertEquals(new BigDecimal("1.1000"), tick5.getAsk());
        Assert.assertEquals(0, tick5.getVolAsk());
        Assert.assertEquals(new BigDecimal("1.1333"), tick5.getBid());
        Assert.assertEquals(0, tick5.getVolBid());

        final TickVO tick6 = validatedTickQueue.remove();
        Assert.assertEquals(1l, tick6.getSecurityId());
        Assert.assertEquals(FeedType.IB, tick6.getFeedType());
        Assert.assertEquals(new BigDecimal("1.1222"), tick6.getLast());
        Assert.assertNotNull(tick6.getLastDateTime());
        Assert.assertEquals(10, tick6.getVol());
        Assert.assertEquals(new BigDecimal("1.1000"), tick6.getAsk());
        Assert.assertEquals(0, tick6.getVolAsk());
        Assert.assertEquals(new BigDecimal("1.1333"), tick6.getBid());
        Assert.assertEquals(0, tick6.getVolBid());

        final TickVO tick7 = validatedTickQueue.remove();
        Assert.assertEquals(1l, tick7.getSecurityId());
        Assert.assertEquals(FeedType.IB, tick7.getFeedType());
        Assert.assertEquals(new BigDecimal("1.1222"), tick7.getLast());
        Assert.assertNotNull(tick7.getLastDateTime());
        Assert.assertEquals(10, tick7.getVol());
        Assert.assertEquals(new BigDecimal("1.1000"), tick7.getAsk());
        Assert.assertEquals(0, tick7.getVolAsk());
        Assert.assertEquals(new BigDecimal("1.1333"), tick7.getBid());
        Assert.assertEquals(9, tick7.getVolBid());

        final TickVO tick8 = validatedTickQueue.remove();
        Assert.assertEquals(1l, tick8.getSecurityId());
        Assert.assertEquals(FeedType.IB, tick8.getFeedType());
        Assert.assertEquals(new BigDecimal("1.1222"), tick8.getLast());
        Assert.assertNotNull(tick8.getLastDateTime());
        Assert.assertEquals(10, tick8.getVol());
        Assert.assertEquals(new BigDecimal("1.1000"), tick8.getAsk());
        Assert.assertEquals(11, tick8.getVolAsk());
        Assert.assertEquals(new BigDecimal("1.1333"), tick8.getBid());
        Assert.assertEquals(9, tick8.getVolBid());
    }

    @Test
    public void testGenericTicksOnIBTicks() throws Exception {

        deployModule(epService, getClass().getResource("/module-market-data.epl"), "TICK_WINDOW", "INSERT_INTO_TICK_WINDOW");
        deployModule(epService, getClass().getResource("/module-ib.epl"),
                "INSERT_INTO_GENERIC_TICK_IB_OPEN", "INSERT_INTO_GENERIC_TICK_IB_CLOSE");

        EPStatement statement1 = epService.getEPAdministrator().createEPL("select * from GenericTickVO");
        final Queue<GenericTickVO> genericTickQueue = new ConcurrentLinkedQueue<>();
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final GenericTickVO event) {
                genericTickQueue.add(event);
            }
        });

        Mockito.when(calendarService.isOpen(Mockito.anyLong(), Mockito.<Date>any())).thenReturn(true);
        Mockito.when(marketDataService.isTickValid(Mockito.any())).thenReturn(true);

        SubscribeTickVO subscribeEvent1 = new SubscribeTickVO("some-ticker1", 1L, FeedType.IB);
        epRuntime.sendEvent(subscribeEvent1);

        TickPriceVO tickPrice1 = new TickPriceVO("some-ticker1", TickType.OPEN, 1.12222d, 0);
        epRuntime.sendEvent(tickPrice1);
        TickPriceVO tickPrice2 = new TickPriceVO("some-ticker1", TickType.CLOSE, 1.13333d, 0);
        epRuntime.sendEvent(tickPrice2);

        Assert.assertEquals(2, genericTickQueue.size());

        GenericTickVO genericTick1 = genericTickQueue.remove();
        Assert.assertEquals(1l, genericTick1.getSecurityId());
        Assert.assertEquals(ch.algotrader.enumeration.TickType.OPEN, genericTick1.getTickType());
        Assert.assertEquals(FeedType.IB, genericTick1.getFeedType());
        Assert.assertEquals(1.12222d, genericTick1.getDoubleValue(), 0.00001);

        GenericTickVO genericTick2 = genericTickQueue.remove();
        Assert.assertEquals(1l, genericTick2.getSecurityId());
        Assert.assertEquals(ch.algotrader.enumeration.TickType.CLOSE, genericTick2.getTickType());
        Assert.assertEquals(FeedType.IB, genericTick2.getFeedType());
        Assert.assertEquals(1.13333d, genericTick2.getDoubleValue(), 0.00001);
    }

}