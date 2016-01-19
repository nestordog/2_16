package ch.algotrader.esper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderCompletionVO;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.SlicingOrder;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.esper.io.CollectionInputAdapter;
import ch.algotrader.service.OrderService;
import ch.algotrader.util.DateTimeLegacy;

@RunWith(MockitoJUnitRunner.class)
public class TradingEsperTest extends EsperTestBase {

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
        config.getVariables().get("misc_orderAckSeconds").setInitializationValue(10);
        config.getEngineDefaults().getExpression().setMathContext(new MathContext(3, RoundingMode.HALF_EVEN));

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
    public void testFilledOrderCompletionEvent() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-trades.epl"), "INSERT_INTO_ORDER_COMPLETION");

        final Queue<OrderCompletionVO> orderStatusQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().createEPL("select * from OrderCompletionVO");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final OrderCompletionVO event) {
                orderStatusQueue.add(event);
            }
        });

        MarketOrder order = MarketOrder.Factory.newInstance();
        order.setIntId("some-int-id");
        order.setQuantity(1000L);
        order.setSide(Side.BUY);
        order.setSecurity(this.eurusd);
        order.setDateTime(new Date(this.epService.getEPRuntime().getCurrentTime()));

        this.epRuntime.sendEvent(order);

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setStatus(Status.SUBMITTED);
        orderStatus1.setOrder(order);
        orderStatus1.setIntId("some-int-id");
        orderStatus1.setFilledQuantity(0L);
        orderStatus1.setRemainingQuantity(1000L);

        Assert.assertNull(orderStatusQueue.poll());

        this.epRuntime.sendEvent(orderStatus1);

        OrderStatus orderStatus2 = OrderStatus.Factory.newInstance();
        orderStatus2.setStatus(Status.PARTIALLY_EXECUTED);
        orderStatus2.setOrder(order);
        orderStatus2.setIntId("some-int-id");
        orderStatus2.setFilledQuantity(100L);
        orderStatus2.setRemainingQuantity(900L);

        this.epRuntime.sendEvent(orderStatus2);

        Transaction transaction1 = Transaction.Factory.newInstance();
        transaction1.setIntOrderId("some-int-id");
        transaction1.setQuantity(100L);
        transaction1.setSecurity(this.eurusd);
        transaction1.setPrice(new BigDecimal("0.98"));
        transaction1.setType(TransactionType.BUY);
        transaction1.setExecutionCommission(new BigDecimal("0.003"));
        transaction1.setDateTime(new Date(this.epService.getEPRuntime().getCurrentTime()));

        this.epRuntime.sendEvent(transaction1);

        Assert.assertNull(orderStatusQueue.poll());

        OrderStatus orderStatus3 = OrderStatus.Factory.newInstance();
        orderStatus3.setStatus(Status.EXECUTED);
        orderStatus3.setOrder(order);
        orderStatus3.setIntId("some-int-id");
        orderStatus3.setFilledQuantity(1000L);
        orderStatus3.setRemainingQuantity(0L);

        this.epRuntime.sendEvent(orderStatus3);

        Transaction transaction2 = Transaction.Factory.newInstance();
        transaction2.setIntOrderId("some-int-id");
        transaction2.setQuantity(900L);
        transaction2.setSecurity(this.eurusd);
        transaction2.setPrice(new BigDecimal("0.96"));
        transaction2.setType(TransactionType.BUY);
        transaction2.setClearingCommission(new BigDecimal("0.001"));
        transaction2.setFee(new BigDecimal("0.002"));
        transaction2.setDateTime(new Date(this.epService.getEPRuntime().getCurrentTime()));

        this.epRuntime.sendEvent(transaction2);

        OrderCompletionVO orderCompletion = orderStatusQueue.poll();
        Assert.assertNotNull(orderCompletion);
        Assert.assertEquals(0L, orderCompletion.getRemainingQuantity());
        Assert.assertEquals(1000L, orderCompletion.getFilledQuantity());
        Assert.assertEquals(2, orderCompletion.getFills());
        Assert.assertEquals(new BigDecimal("0.962"), orderCompletion.getAvgPrice());
        Assert.assertEquals(new BigDecimal("0.00600"), orderCompletion.getTotalCharges());
    }

    @Test
    public void testCancelledOrderCompletionEvent() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-trades.epl"), "INSERT_INTO_ORDER_COMPLETION");

        final Queue<OrderCompletionVO> orderStatusQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().createEPL("select * from OrderCompletionVO");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final OrderCompletionVO event) {
                orderStatusQueue.add(event);
            }
        });

        MarketOrder order = MarketOrder.Factory.newInstance();
        order.setIntId("some-int-id");
        order.setQuantity(1000L);
        order.setSide(Side.BUY);
        order.setSecurity(this.eurusd);
        order.setDateTime(new Date(this.epService.getEPRuntime().getCurrentTime()));

        this.epRuntime.sendEvent(order);

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setStatus(Status.SUBMITTED);
        orderStatus1.setOrder(order);
        orderStatus1.setIntId("some-int-id");
        orderStatus1.setFilledQuantity(0L);
        orderStatus1.setRemainingQuantity(1000L);

        Assert.assertNull(orderStatusQueue.poll());

        this.epRuntime.sendEvent(orderStatus1);

        Transaction transaction1 = Transaction.Factory.newInstance();
        transaction1.setIntOrderId("some-int-id");
        transaction1.setQuantity(100L);
        transaction1.setSecurity(this.eurusd);
        transaction1.setPrice(new BigDecimal("0.98"));
        transaction1.setType(TransactionType.BUY);
        transaction1.setExecutionCommission(new BigDecimal("0.003"));
        transaction1.setDateTime(new Date(this.epService.getEPRuntime().getCurrentTime()));

        this.epRuntime.sendEvent(transaction1);

        OrderStatus orderStatus2 = OrderStatus.Factory.newInstance();
        orderStatus2.setStatus(Status.PARTIALLY_EXECUTED);
        orderStatus2.setOrder(order);
        orderStatus2.setIntId("some-int-id");
        orderStatus2.setFilledQuantity(100L);
        orderStatus2.setRemainingQuantity(900L);

        this.epRuntime.sendEvent(orderStatus2);

        Assert.assertNull(orderStatusQueue.poll());

        OrderStatus orderStatus3 = OrderStatus.Factory.newInstance();
        orderStatus3.setStatus(Status.CANCELED);
        orderStatus3.setOrder(order);
        orderStatus3.setIntId("some-int-id");
        orderStatus3.setFilledQuantity(100L);
        orderStatus3.setRemainingQuantity(900L);

        this.epRuntime.sendEvent(orderStatus3);

        OrderCompletionVO orderCompletion = orderStatusQueue.poll();
        Assert.assertNotNull(orderCompletion);
        Assert.assertEquals(900L, orderCompletion.getRemainingQuantity());
        Assert.assertEquals(100L, orderCompletion.getFilledQuantity());
        Assert.assertEquals(1, orderCompletion.getFills());
        Assert.assertEquals(new BigDecimal("0.98"), orderCompletion.getAvgPrice());
        Assert.assertEquals(new BigDecimal("0.00300"), orderCompletion.getTotalCharges());
    }

    @Test
    public void testOrderWithAck() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-trades.epl"), "NOTIFY_MISSING_ORDER_REPLY");

        final Queue<String> notificationQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("NOTIFY_MISSING_ORDER_REPLY");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final String event) {
                notificationQueue.add(event);
            }
        });

        MarketOrder order = MarketOrder.Factory.newInstance();
        order.setIntId("some-int-id");
        order.setQuantity(1000L);
        order.setSide(Side.BUY);
        order.setSecurity(this.eurusd);
        order.setTif(TIF.DAY);
        order.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setStatus(Status.SUBMITTED);
        orderStatus.setOrder(order);
        orderStatus.setIntId("some-int-id");
        orderStatus.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:10"));

        this.epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(this.epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(order, orderStatus), "dateTime");
        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Assert.assertNull(notificationQueue.poll());
    }

    @Test
    public void testOrderWithMissingAck() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-trades.epl"), "NOTIFY_MISSING_ORDER_REPLY");

        final Queue<String> notificationQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("NOTIFY_MISSING_ORDER_REPLY");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final String event) {
                notificationQueue.add(event);
            }
        });

        MarketOrder order = MarketOrder.Factory.newInstance();
        order.setIntId("some-int-id");
        order.setQuantity(1000L);
        order.setSide(Side.BUY);
        order.setSecurity(this.eurusd);
        order.setTif(TIF.DAY);
        order.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));

        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setStatus(Status.SUBMITTED);
        orderStatus.setIntId("some-other-int-id");
        orderStatus.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:11"));

        this.epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(this.epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(order, orderStatus), "dateTime");
        coordinator.coordinate(inputAdapter);
        coordinator.start();

        String msg = notificationQueue.poll();
        Assert.assertNotNull(msg);
        Assert.assertTrue(msg.startsWith("missing reply on order:"));
    }

    @Test
    public void testAlgoOrderInitialOrders() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-current-values.epl"), "MARKET_DATA_WINDOW", "INSERT_INTO_CURRENT_MARKET_DATA_EVENT");
        deployModule(this.epService,
                getClass().getResource("/module-trades.epl"), "SEND_INITIAL_ALGO_ORDERS");

        final Queue<Collection<Order>> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("SEND_INITIAL_ALGO_ORDERS");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final Collection<Order> event) {
                orderQueue.add(event);
            }
        });

        TickVO tick1 = new TickVO(0L, new Date(), FeedType.IB.name(), this.eurusd.getId(),
                new BigDecimal("1.11"), new Date(this.epService.getEPRuntime().getCurrentTime()), new BigDecimal("1.12"), new BigDecimal("1.1"), 0, 0, 0);
        this.epRuntime.sendEvent(tick1);

        SlicingOrder algoOrder = new SlicingOrder();
        algoOrder.setSecurity(this.eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(new Date(this.epService.getEPRuntime().getCurrentTime()));

        this.epRuntime.sendEvent(algoOrder);

        Collection<Order> orders = orderQueue.poll();
        Assert.assertNotNull(orders);
        Assert.assertEquals(1, orders.size());
    }

    @Test
    public void testTransactionSummary() throws Exception {

        deployModule(this.epService,
                getClass().getResource("/module-trades.epl"), "LOG_TRANSACTION_SUMMARY");

        final Queue<List<Fill>> fillListQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("LOG_TRANSACTION_SUMMARY");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(Map<?, ?>[] insertStream, Map<?, ?>[] removeStream) {
                List<Fill> fills = new ArrayList<>();
                for (Map<?, ?> element : insertStream) {
                    Fill fill = (Fill) element.get("fill");
                    fills.add(fill);
                }
                fillListQueue.add(fills);
            }
        });

        MarketOrder order = MarketOrder.Factory.newInstance();
        order.setIntId("my-order");
        order.setSecurity(this.eurusd);
        order.setQuantity(40);
        order.setSide(Side.BUY);

        this.epRuntime.sendEvent(order);

        Fill fill1 = new Fill();
        fill1.setOrder(order);
        fill1.setSide(Side.BUY);
        fill1.setQuantity(10L);
        this.epRuntime.sendEvent(fill1);

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-order");
        orderStatus1.setOrder(order);
        orderStatus1.setStatus(Status.PARTIALLY_EXECUTED);
        orderStatus1.setFilledQuantity(10L);
        orderStatus1.setRemainingQuantity(30L);
        this.epRuntime.sendEvent(orderStatus1);

        Assert.assertNull(fillListQueue.poll());

        Fill fill2 = new Fill();
        fill2.setOrder(order);
        fill2.setSide(Side.BUY);
        fill2.setQuantity(10L);
        this.epRuntime.sendEvent(fill2);

        OrderStatus orderStatus2 = OrderStatus.Factory.newInstance();
        orderStatus2.setIntId("my-order");
        orderStatus2.setOrder(order);
        orderStatus2.setStatus(Status.PARTIALLY_EXECUTED);
        orderStatus2.setFilledQuantity(20L);
        orderStatus2.setRemainingQuantity(20L);
        this.epRuntime.sendEvent(orderStatus2);

        Assert.assertNull(fillListQueue.poll());

        Fill fill3 = new Fill();
        fill3.setOrder(order);
        fill3.setSide(Side.BUY);
        fill3.setQuantity(10L);
        this.epRuntime.sendEvent(fill3);

        OrderStatus orderStatus3 = OrderStatus.Factory.newInstance();
        orderStatus3.setIntId("my-order");
        orderStatus3.setOrder(order);
        orderStatus3.setStatus(Status.PARTIALLY_EXECUTED);
        orderStatus3.setFilledQuantity(30L);
        orderStatus3.setRemainingQuantity(10L);
        this.epRuntime.sendEvent(orderStatus3);

        Assert.assertNull(fillListQueue.poll());

        Fill fill4 = new Fill();
        fill4.setOrder(null);
        fill4.setSide(Side.BUY);
        fill4.setQuantity(10L);
        this.epRuntime.sendEvent(fill4);

        OrderStatus orderStatus4 = OrderStatus.Factory.newInstance();
        orderStatus4.setIntId("some-other-order");
        orderStatus4.setOrder(null);
        orderStatus4.setStatus(Status.EXECUTED);
        orderStatus4.setFilledQuantity(40L);
        orderStatus4.setRemainingQuantity(0L);
        this.epRuntime.sendEvent(orderStatus4);

        Assert.assertNull(fillListQueue.poll());

        Fill fill5 = new Fill();
        fill5.setOrder(order);
        fill5.setSide(Side.BUY);
        fill5.setQuantity(10L);
        this.epRuntime.sendEvent(fill5);

        OrderStatus orderStatus5 = OrderStatus.Factory.newInstance();
        orderStatus5.setIntId("my-order");
        orderStatus5.setOrder(order);
        orderStatus5.setStatus(Status.EXECUTED);
        orderStatus5.setFilledQuantity(40L);
        orderStatus5.setRemainingQuantity(0L);
        this.epRuntime.sendEvent(orderStatus5);

        List<Fill> fillList = fillListQueue.poll();
        Assert.assertNotNull(fillList);
        Assert.assertEquals(4, fillList.size());
        Assert.assertSame(fill1, fillList.get(0));
        Assert.assertSame(fill2, fillList.get(1));
        Assert.assertSame(fill3, fillList.get(2));
        Assert.assertSame(fill5, fillList.get(3));
    }

}
