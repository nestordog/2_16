package ch.algotrader.esper;

import java.math.BigDecimal;
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
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.marketData.TickVOBuilder;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.algo.SlicingOrder;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.io.CollectionInputAdapter;
import ch.algotrader.service.OrderService;
import ch.algotrader.util.DateTimeLegacy;

@RunWith(MockitoJUnitRunner.class)
public class SlicingOrderEsperTest extends EsperTestBase {

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
    public void testSlicingCancelOrder() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-algo-slicing.epl"), "SLICING_CANCEL_ORDER");

        final Queue<Order> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("SLICING_CANCEL_ORDER");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final Order event) {
                orderQueue.add(event);
            }
        });

        this.epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(this.eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setMinDuration(5);
        algoOrder.setMaxDuration(10);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(this.eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);
        childOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("some-other-order");
        orderStatus1.setOrder(childOrder);
        orderStatus1.setStatus(Status.SUBMITTED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:01:00"));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(this.epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(childOrder, orderStatus1), "dateTime");

        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Assert.assertSame(childOrder, orderQueue.poll());
    }

    @Test
    public void testSlicingCancelAborted() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-algo-slicing.epl"), "SLICING_CANCEL_ORDER");

        final Queue<Order> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("SLICING_CANCEL_ORDER");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final Order event) {
                orderQueue.add(event);
            }
        });

        this.epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(this.eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setMinDuration(5);
        algoOrder.setMaxDuration(10);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(this.eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);
        childOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-child-order");
        orderStatus1.setOrder(childOrder);
        orderStatus1.setStatus(Status.EXECUTED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:02"));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(this.epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(childOrder, orderStatus1), "dateTime");
        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Assert.assertNull(orderQueue.poll());
    }

    @Test
    public void testSlicingIncreaseOffsetTick() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-algo-slicing.epl"), "SLICING_INCREASE_OFFSET_TICKS");

        final Queue<Boolean> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("SLICING_INCREASE_OFFSET_TICKS");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final Map<?, ?> event) {
                orderQueue.add(Boolean.TRUE);
            }
        });

        this.epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(this.eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setMinDuration(5);
        algoOrder.setMaxDuration(10);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(this.eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);
        childOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-child-order");
        orderStatus1.setOrder(childOrder);
        orderStatus1.setStatus(Status.PARTIALLY_EXECUTED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:05"));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(this.epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(childOrder, orderStatus1), "dateTime");
        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Assert.assertEquals(Boolean.TRUE, orderQueue.poll());
    }

    @Test
    public void testSlicingIncreaseOffsetTickChildCanceled() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-algo-slicing.epl"), "SLICING_INCREASE_OFFSET_TICKS");

        final Queue<Boolean> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("SLICING_INCREASE_OFFSET_TICKS");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final Map<?, ?> event) {
                orderQueue.add(Boolean.TRUE);
            }
        });

        this.epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(this.eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setMinDuration(5);
        algoOrder.setMaxDuration(10);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(this.eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);
        childOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-child-order");
        orderStatus1.setOrder(childOrder);
        orderStatus1.setStatus(Status.CANCELED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:03"));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(this.epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(childOrder, orderStatus1), "dateTime");
        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Assert.assertNull(orderQueue.poll());
    }

    @Test
    public void testSlicingDecreaseOffsetTick() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-algo-slicing.epl"), "SLICING_DECREASE_OFFSET_TICKS");

        final Queue<Boolean> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("SLICING_DECREASE_OFFSET_TICKS");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final Map<?, ?> event) {
                orderQueue.add(Boolean.TRUE);
            }
        });

        this.epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(this.eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setMinDuration(5);
        algoOrder.setMaxDuration(10);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(this.eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);
        childOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-child-order");
        orderStatus1.setOrder(childOrder);
        orderStatus1.setStatus(Status.CANCELED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:05"));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(this.epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(childOrder, orderStatus1), "dateTime");
        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Assert.assertEquals(Boolean.TRUE, orderQueue.poll());
    }

    @Test
    public void testSlicingDecreaseOffsetTickChildPartiallyExecuted() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-algo-slicing.epl"), "SLICING_DECREASE_OFFSET_TICKS");

        final Queue<Boolean> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("SLICING_DECREASE_OFFSET_TICKS");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final Map<?, ?> event) {
                orderQueue.add(Boolean.TRUE);
            }
        });

        this.epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(this.eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setMinDuration(5);
        algoOrder.setMaxDuration(10);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(this.eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);
        childOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-child-order");
        orderStatus1.setOrder(childOrder);
        orderStatus1.setStatus(Status.PARTIALLY_EXECUTED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:05"));

        OrderStatus orderStatus2 = OrderStatus.Factory.newInstance();
        orderStatus2.setIntId("my-child-order");
        orderStatus2.setOrder(childOrder);
        orderStatus2.setStatus(Status.CANCELED);
        orderStatus2.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:05"));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(this.epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(childOrder, orderStatus1, orderStatus2), "dateTime");
        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Assert.assertNull(orderQueue.poll());
    }

    @Test
    public void testSlicingNextOrder() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-algo-slicing.epl"), "SLICING_NEXT_ORDER");

        final Queue<Order> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("SLICING_NEXT_ORDER");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final Order event) {
                orderQueue.add(event);
            }
        });

        this.epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        TickVO tick1 = TickVOBuilder.create() //
                .setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01")) //
                .setFeedType(FeedType.IB.name()) //
                .setSecurityId(this.eurusd.getId()) //
                .setLast(new BigDecimal("1.11")) //
                .build();

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(this.eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setMinDelay(5);
        algoOrder.setMaxDelay(5);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(this.eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);
        childOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-child-order");
        orderStatus1.setOrder(childOrder);
        orderStatus1.setStatus(Status.EXECUTED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:02"));

        Map<String, Date> time1 = Collections.singletonMap("dateTime", DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:07"));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(this.epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(tick1, algoOrder, childOrder, orderStatus1, time1), "dateTime");

        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Order nextOrder = orderQueue.poll();
        Assert.assertSame(algoOrder, nextOrder);
    }

    @Test
    public void testSlicingNextOrderSlicingOrderExecuted() throws Exception {

        deployModule(this.epService, getClass().getResource("/module-algo-slicing.epl"), "SLICING_NEXT_ORDER");

        final Queue<Order> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("SLICING_NEXT_ORDER");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final Order event) {
                orderQueue.add(event);
            }
        });

        this.epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(this.eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setMinDuration(5);
        algoOrder.setMaxDuration(5);
        algoOrder.setMinDelay(5);
        algoOrder.setMaxDelay(5);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(this.eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);
        childOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-child-order");
        orderStatus1.setOrder(childOrder);
        orderStatus1.setStatus(Status.EXECUTED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:03"));

        OrderStatus orderStatus2 = OrderStatus.Factory.newInstance();
        orderStatus2.setIntId("my-algo-order");
        orderStatus2.setOrder(algoOrder);
        orderStatus2.setStatus(Status.EXECUTED);
        orderStatus2.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:04"));

        Map<String, Date> time1 = Collections.singletonMap("dateTime", DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:10"));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(this.epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(algoOrder, childOrder, orderStatus1, orderStatus2, time1), "dateTime");

        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Order nextOrder = orderQueue.poll();
        Assert.assertNull(nextOrder);
    }

}
