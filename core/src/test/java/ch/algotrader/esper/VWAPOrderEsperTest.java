package ch.algotrader.esper;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
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
import org.mockito.runners.MockitoJUnitRunner;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esperio.AdapterCoordinator;
import com.espertech.esperio.AdapterCoordinatorImpl;

import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.algo.VWAPOrder;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.io.CollectionInputAdapter;
import ch.algotrader.service.OrderService;
import ch.algotrader.util.DateTimeLegacy;

@RunWith(MockitoJUnitRunner.class)
public class VWAPOrderEsperTest extends EsperTestBase {

    @Mock
    private OrderService orderService;

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
        config.getEngineDefaults().getThreading().setInternalTimerEnabled(false);

        this.epService = EPServiceProviderManager.getDefaultProvider(config);
        this.epRuntime = this.epService.getEPRuntime();
        this.epRuntime.setVariableValue("orderService", this.orderService);

        this.usdFx = SecurityFamily.Factory.newInstance();
        this.usdFx.setId(1);
        this.usdFx.setSymbolRoot("USD FX");
        this.usdFx.setCurrency(Currency.USD);
        this.usdFx.setTickSizePattern("0<0.00005");
        this.usdFx.setTradeable(true);
        this.usdFx.setScale(4);
        this.usdFx.setContractSize(10.0d);
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
    }

    @After
    public void cleanUpEsper() {
        if (this.epService != null) {
            this.epService.destroy();
        }
    }

    @Test
    public void testVWAPNextOrder() throws Exception {

        deployModule(this.epService, getClass().getResource("/module-algo-vwap.epl"), "VWAP_NEXT_ORDER");

        final Queue<Object> eventQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("VWAP_NEXT_ORDER");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final VWAPOrder algoOrder, final Date dateTime) {
                eventQueue.add(algoOrder);
                eventQueue.add(dateTime);
            }
        });

        this.epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        VWAPOrder algoOrder = new VWAPOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(this.eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setMinInterval(60);
        algoOrder.setMaxInterval(60);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        Date dateTime1 = DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:01:01");
        Map<String, Date> timeEvent1 = Collections.singletonMap("dateTime", dateTime1);

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(this.epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(algoOrder, timeEvent1), "dateTime");

        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Assert.assertEquals(algoOrder, eventQueue.poll());
        Assert.assertEquals(dateTime1, eventQueue.poll());
    }

    @Test
    public void testVWAPNextOrderExecuted() throws Exception {

        deployModule(this.epService, getClass().getResource("/module-algo-vwap.epl"), "VWAP_NEXT_ORDER");

        final Queue<Object> eventQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("VWAP_NEXT_ORDER");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final VWAPOrder algoOrder, final Date dateTime) {
                eventQueue.add(algoOrder);
                eventQueue.add(dateTime);
            }
        });

        this.epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        VWAPOrder algoOrder = new VWAPOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(this.eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setMinInterval(60);
        algoOrder.setMaxInterval(60);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-algo-order");
        orderStatus1.setOrder(algoOrder);
        orderStatus1.setStatus(Status.EXECUTED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:02"));

        Date dateTime1 = DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:01:01");
        Map<String, Date> timeEvent1 = Collections.singletonMap("dateTime", dateTime1);

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(this.epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(algoOrder, orderStatus1, timeEvent1), "dateTime");

        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Assert.assertNull(eventQueue.poll());
    }
}
