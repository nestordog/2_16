package ch.algotrader.esper;

import java.math.BigDecimal;
import java.util.Arrays;
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
import ch.algotrader.entity.marketData.TickVOBuilder;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.algo.TrailingLimitOrder;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.io.CollectionInputAdapter;
import ch.algotrader.service.OrderService;
import ch.algotrader.util.DateTimeLegacy;

@RunWith(MockitoJUnitRunner.class)
public class TrailingOrderEsperTest extends EsperTestBase {

    @Mock
    private OrderService orderService;

    private EPServiceProvider epService;
    private EPRuntime epRuntime;

    private SecurityFamily usdFx;
    private Exchange exchange;
    private Forex eurusd;

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
        CONFIG_MAP.put("report.disabled", "true");
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
    }

    @After
    public void cleanUpEsper() {
        if (this.epService != null) {
            this.epService.destroy();
        }
    }

    @Test
    public void testAdjustLimit() throws Exception {

        deployModule(this.epService, getClass().getResource("/module-algo-trailing-limit.epl"), "TRAILING_LIMIT_ADJUST_LIMIT");

        final Queue<Order> orderQueue = new ConcurrentLinkedQueue<>();
        EPStatement statement1 = this.epService.getEPAdministrator().getStatement("TRAILING_LIMIT_ADJUST_LIMIT");
        Assert.assertNotNull(statement1);
        statement1.setSubscriber(new Object() {
            @SuppressWarnings("unused")
            public void update(final Order event, final BigDecimal last) {
                orderQueue.add(event);
            }
        });

        this.epRuntime.sendEvent(new CurrentTimeEvent(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:00").getTime()));

        TrailingLimitOrder algoOrder = new TrailingLimitOrder();
        algoOrder.setIntId("my-algo-order");
        algoOrder.setSecurity(this.eurusd);
        algoOrder.setSide(Side.BUY);
        algoOrder.setQuantity(200);
        algoOrder.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:01"));
        algoOrder.setTrailingAmount(new BigDecimal("0.5"));
        algoOrder.setIncrement(new BigDecimal("0.1"));

        TickVO tick1 = TickVOBuilder.create() //
                .setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:02")) //
                .setFeedType(FeedType.IB.name()) //
                .setSecurityId(this.eurusd.getId()) //
                .setLast(new BigDecimal("1.11")) //
                .build();

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setIntId("my-algo-order");
        orderStatus1.setOrder(algoOrder);
        orderStatus1.setStatus(Status.EXECUTED);
        orderStatus1.setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:03"));

        TickVO tick2 = TickVOBuilder.create() //
                .setDateTime(DateTimeLegacy.parseAsLocalDateTime("2015-01-01 12:00:04")) //
                .setFeedType(FeedType.IB.name()) //
                .setSecurityId(this.eurusd.getId()) //
                .setLast(new BigDecimal("1.11")) //
                .build();

        AdapterCoordinator coordinator = new AdapterCoordinatorImpl(this.epService, true, true, true);
        CollectionInputAdapter inputAdapter = new CollectionInputAdapter(Arrays.asList(algoOrder, tick1, orderStatus1, tick2), "dateTime");

        coordinator.coordinate(inputAdapter);
        coordinator.start();

        Order nextOrder = orderQueue.poll();
        Assert.assertSame(algoOrder, nextOrder);

        Assert.assertNull(orderQueue.poll());
    }

}
