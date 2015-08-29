package ch.algotrader.esper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esperio.AdapterCoordinator;
import com.espertech.esperio.AdapterCoordinatorImpl;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigBeanFactory;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.config.spring.DefaultConfigProvider;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.trade.ExecutionStatusVO;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.SlicingOrder;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.io.CollectionInputAdapter;
import ch.algotrader.service.OrderService;
import ch.algotrader.util.DateTimeLegacy;

@RunWith(MockitoJUnitRunner.class)
public class SlicingEsperTest extends EsperTestBase {

    @Mock
    private OrderService orderService;

    private EPServiceProvider epService;
    private EPRuntime epRuntime;

    private SecurityFamily usdFx;
    private Exchange exchange;
    private Forex eurusd;
    private Forex chfusd;

    private static Map<String, String> CONFIG_MAP;

    @BeforeClass
    public static void setupConfig() {
        CONFIG_MAP = new HashMap<>();
        CONFIG_MAP.put("dataSource.dataSet", "someDataSet");
        CONFIG_MAP.put("dataSource.dataSetType", "BAR");
        CONFIG_MAP.put("dataSource.dataSetLocation", "stuff/more-stuff");
        CONFIG_MAP.put("dataSource.barSize", "MIN_5");
        CONFIG_MAP.put("dataSource.feedCSV", "false");
        CONFIG_MAP.put("dataSource.feedDB", "false");
        CONFIG_MAP.put("dataSource.feedGenericEvents", "true");
        CONFIG_MAP.put("dataSource.feedAllMarketDataFiles", "true");
        CONFIG_MAP.put("dataSource.feedBatchSize", "20");
        CONFIG_MAP.put("report.reportLocation", "stuff/report-stuff");
        CONFIG_MAP.put("simulation", "true");
        CONFIG_MAP.put("simulation.initialBalance", "500.5");
        CONFIG_MAP.put("simulation.logTransactions", "true");
        CONFIG_MAP.put("misc.embedded", "true");
        CONFIG_MAP.put("misc.portfolioBaseCurrency", "EUR");
        CONFIG_MAP.put("misc.portfolioDigits", "5");
        CONFIG_MAP.put("misc.defaultAccountName", "IB_NATIVE_TEST");
        CONFIG_MAP.put("misc.validateCrossedSpread", "true");
        CONFIG_MAP.put("misc.displayClosedPositions", "true");

        DefaultConfigProvider configProvider = new DefaultConfigProvider(CONFIG_MAP);
        ConfigParams configParams = new ConfigParams(configProvider);
        CommonConfig commonConfig = new ConfigBeanFactory().create(configParams, CommonConfig.class);
        ConfigLocator.initialize(configParams, commonConfig);
    }

    @Before
    public void setupEsper() throws Exception {

        Configuration config = new Configuration();
        config.configure("/META-INF/esper-common.cfg.xml");
        config.configure("/META-INF/esper-core.cfg.xml");

        epService = EPServiceProviderManager.getDefaultProvider(config);
        epRuntime = epService.getEPRuntime();
        epRuntime.setVariableValue("orderService", orderService);

        usdFx = SecurityFamily.Factory.newInstance();
        usdFx.setId(1);
        usdFx.setSymbolRoot("USD FX");
        usdFx.setCurrency(Currency.USD);
        usdFx.setTickSizePattern("0<0.00005");
        usdFx.setTradeable(true);
        usdFx.setScale(4);
        usdFx.setContractSize(10.0d);
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
    }

    @After
    public void cleanUpEsper() {
        if (epService != null) {
            epService.destroy();
        }
    }

    @Test
    public void testSlicingOrderStatusSubmitted() throws Exception {

        deployModule(epService,
                getClass().getResource("/module-trades.epl"), "INSERT_INTO_ALGO_ORDER_STATUS_SUBMITTED");

        final Queue<OrderStatus> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = epService.getEPAdministrator().createEPL("select * from OrderStatus");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final OrderStatus event) {
                orderQueue.add(event);
            }
        });

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-child-order");
        orderStatus1.setOrder(childOrder);
        orderStatus1.setStatus(Status.SUBMITTED);

        Mockito.when(orderService.getStatusByIntId("my-algo-order")).thenReturn(
                new ExecutionStatusVO("my-algo-order", Status.OPEN, 0L, 199L));

        epRuntime.sendEvent(orderStatus1);

        Assert.assertSame(orderStatus1, orderQueue.poll());
        OrderStatus orderStatus2 = orderQueue.poll();
        Assert.assertNotNull(orderStatus2);
        Assert.assertEquals(Status.SUBMITTED, orderStatus2.getStatus());
        Assert.assertSame(algoOrder, orderStatus2.getOrder());
        Assert.assertEquals(0L, orderStatus2.getFilledQuantity());
        Assert.assertEquals(199L, orderStatus2.getRemainingQuantity());
    }

    @Test
    public void testSlicingOrderStatusFilled() throws Exception {

        deployModule(epService,
                getClass().getResource("/module-trades.epl"), "INSERT_INTO_ALGO_ORDER_STATUS_FROM_FILL");

        final Queue<OrderStatus> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = epService.getEPAdministrator().createEPL("select * from OrderStatus");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final OrderStatus event) {
                orderQueue.add(event);
            }
        });

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);

        Mockito.when(orderService.getStatusByIntId("my-algo-order")).thenReturn(
                new ExecutionStatusVO("my-algo-order", Status.OPEN, 0L, 199L));

        Fill fill1 = new Fill();
        fill1.setOrder(childOrder);
        fill1.setSide(Side.BUY);
        fill1.setQuantity(10L);

        epRuntime.sendEvent(fill1);

        OrderStatus orderStatus1 = orderQueue.poll();
        Assert.assertNotNull(orderStatus1);
        Assert.assertEquals(Status.PARTIALLY_EXECUTED, orderStatus1.getStatus());
        Assert.assertSame(algoOrder, orderStatus1.getOrder());
        Assert.assertEquals(10L, orderStatus1.getFilledQuantity());
        Assert.assertEquals(189L, orderStatus1.getRemainingQuantity());

        Fill fill2 = new Fill();
        fill2.setOrder(childOrder);
        fill2.setSide(Side.BUY);
        fill2.setQuantity(199L);

        epRuntime.sendEvent(fill2);

        OrderStatus orderStatus2 = orderQueue.poll();
        Assert.assertNotNull(orderStatus2);
        Assert.assertEquals(Status.EXECUTED, orderStatus2.getStatus());
        Assert.assertSame(algoOrder, orderStatus2.getOrder());
        Assert.assertEquals(199L, orderStatus2.getFilledQuantity());
        Assert.assertEquals(0L, orderStatus2.getRemainingQuantity());
    }

    @Test
    public void testSlicingCancelOrder() throws Exception {

        deployModule(epService,
                getClass().getResource("/module-algo-slicing.epl"), "SLICING_CANCEL_ORDER");

        final Queue<Order> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = epService.getEPAdministrator().getStatement("SLICING_CANCEL_ORDER");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final Order event) {
                orderQueue.add(event);
            }
        });

        epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setMinDuration(5);
        algoOrder.setMaxDuration(10);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);
        childOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("some-other-order");
        orderStatus1.setOrder(childOrder);
        orderStatus1.setStatus(Status.SUBMITTED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:01:00"));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(childOrder, orderStatus1),
                "dateTime");
        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Assert.assertSame(childOrder, orderQueue.poll());
    }

    @Test
    public void testSlicingCancelChildCanceled() throws Exception {

        deployModule(epService,
                getClass().getResource("/module-algo-slicing.epl"), "SLICING_CANCEL_ORDER");

        final Queue<Order> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = epService.getEPAdministrator().getStatement("SLICING_CANCEL_ORDER");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final Order event) {
                orderQueue.add(event);
            }
        });

        epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setMinDuration(5);
        algoOrder.setMaxDuration(10);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);
        childOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-child-order");
        orderStatus1.setOrder(childOrder);
        orderStatus1.setStatus(Status.CANCELED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:02"));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(childOrder, orderStatus1),
                "dateTime");
        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Assert.assertNull(orderQueue.poll());
    }

    @Test
    public void testSlicingIncreaseOffsetTick() throws Exception {

        deployModule(epService,
                getClass().getResource("/module-algo-slicing.epl"), "SLICING_INCREASE_OFFSET_TICKS");

        final Queue<Boolean> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = epService.getEPAdministrator().getStatement("SLICING_INCREASE_OFFSET_TICKS");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final Map<?, ?> event) {
                orderQueue.add(Boolean.TRUE);
            }
        });

        epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setMinDuration(5);
        algoOrder.setMaxDuration(10);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);
        childOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-child-order");
        orderStatus1.setOrder(childOrder);
        orderStatus1.setStatus(Status.PARTIALLY_EXECUTED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:05"));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(childOrder, orderStatus1),
                "dateTime");
        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Assert.assertEquals(Boolean.TRUE, orderQueue.poll());
    }

    @Test
    public void testSlicingIncreaseOffsetTickChildCanceled() throws Exception {

        deployModule(epService,
                getClass().getResource("/module-algo-slicing.epl"), "SLICING_INCREASE_OFFSET_TICKS");

        final Queue<Boolean> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = epService.getEPAdministrator().getStatement("SLICING_INCREASE_OFFSET_TICKS");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final Map<?, ?> event) {
                orderQueue.add(Boolean.TRUE);
            }
        });

        epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setMinDuration(5);
        algoOrder.setMaxDuration(10);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);
        childOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-child-order");
        orderStatus1.setOrder(childOrder);
        orderStatus1.setStatus(Status.CANCELED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:03"));

        OrderStatus orderStatus2 = OrderStatus.Factory.newInstance();
        orderStatus2.setIntId("my-child-order");
        orderStatus2.setOrder(childOrder);
        orderStatus2.setStatus(Status.PARTIALLY_EXECUTED);
        orderStatus2.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:04"));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(childOrder, orderStatus1, orderStatus2),
                "dateTime");
        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Assert.assertNull(orderQueue.poll());
    }

    @Test
    public void testSlicingDecreaseOffsetTick() throws Exception {

        deployModule(epService,
                getClass().getResource("/module-algo-slicing.epl"), "SLICING_DECREASE_OFFSET_TICKS");

        final Queue<Boolean> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = epService.getEPAdministrator().getStatement("SLICING_DECREASE_OFFSET_TICKS");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final Map<?, ?> event) {
                orderQueue.add(Boolean.TRUE);
            }
        });

        epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setMinDuration(5);
        algoOrder.setMaxDuration(10);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);
        childOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-child-order");
        orderStatus1.setOrder(childOrder);
        orderStatus1.setStatus(Status.CANCELED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:05"));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(childOrder, orderStatus1),
                "dateTime");
        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Assert.assertEquals(Boolean.TRUE, orderQueue.poll());
    }

    @Test
    public void testSlicingDecreaseOffsetTickChildPartiallyExecuted() throws Exception {

        deployModule(epService,
                getClass().getResource("/module-algo-slicing.epl"), "SLICING_DECREASE_OFFSET_TICKS");

        final Queue<Boolean> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = epService.getEPAdministrator().getStatement("SLICING_DECREASE_OFFSET_TICKS");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final Map<?, ?> event) {
                orderQueue.add(Boolean.TRUE);
            }
        });

        epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setMinDuration(5);
        algoOrder.setMaxDuration(10);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(eurusd);
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

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(childOrder, orderStatus1, orderStatus2),
                "dateTime");
        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Assert.assertNull(orderQueue.poll());
    }

    @Test
    public void testSlicingInitialOrder() throws Exception {

        deployModule(epService,
                getClass().getResource("/module-current-values.epl"), "MARKET_DATA_WINDOW", "INSERT_INTO_CURRENT_MARKET_DATA_EVENT");
        deployModule(epService,
                getClass().getResource("/module-trades.epl"), "SEND_INITIAL_ALGO_ORDERS");

        final Queue<Collection<Order>> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = epService.getEPAdministrator().getStatement("SEND_INITIAL_ALGO_ORDERS");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final Collection<Order> event) {
                orderQueue.add(event);
            }
        });

        TickVO tick1 = new TickVO(0L, new Date(), FeedType.IB, eurusd.getId(),
                new BigDecimal("1.11"), new Date(epService.getEPRuntime().getCurrentTime()), new BigDecimal("1.12"), new BigDecimal("1.1"), 0, 0, 0);
        epRuntime.sendEvent(tick1);

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setMinDuration(5);
        algoOrder.setMaxDuration(10);

        epRuntime.sendEvent(algoOrder);

        Collection<Order> orders = orderQueue.poll();
        Assert.assertNotNull(orders);
        Assert.assertEquals(1, orders.size());
    }

    @Test
    public void testSlicingNextOrder() throws Exception {

        deployModule(epService,
                getClass().getResource("/module-current-values.epl"), "MARKET_DATA_WINDOW", "INSERT_INTO_CURRENT_MARKET_DATA_EVENT");
        deployModule(epService,
                getClass().getResource("/module-algo-slicing.epl"), "SLICING_NEXT_ORDER");

        final Queue<Order> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = epService.getEPAdministrator().getStatement("SLICING_NEXT_ORDER");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            public void update(final Order event) {
                orderQueue.add(event);
            }
        });

        TickVO tick1 = new TickVO(0L, new Date(), FeedType.IB, eurusd.getId(),
                new BigDecimal("1.11"), new Date(epService.getEPRuntime().getCurrentTime()), new BigDecimal("1.12"), new BigDecimal("1.1"), 0, 0, 0);

        epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setMinDuration(5);
        algoOrder.setMaxDuration(10);

        MarketOrder childOrder = MarketOrder.Factory.newInstance();
        childOrder.setIntId("my-child-order");
        childOrder.setParentOrder(algoOrder);
        childOrder.setSecurity(eurusd);
        childOrder.setQuantity(40);
        childOrder.setSide(Side.BUY);
        childOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-child-order");
        orderStatus1.setOrder(childOrder);
        orderStatus1.setStatus(Status.EXECUTED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:07"));

        Mockito.when(orderService.getStatusByIntId("my-algo-order")).thenReturn(
                new ExecutionStatusVO("my-algo-order", Status.OPEN, 40L, 199L));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(tick1, algoOrder, childOrder, orderStatus1),
                "dateTime");
        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Order nextOrder = orderQueue.poll();
        Assert.assertNotNull(nextOrder);
    }

}
