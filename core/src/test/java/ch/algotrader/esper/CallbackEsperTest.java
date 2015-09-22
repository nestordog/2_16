package ch.algotrader.esper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.OrderCompletionVO;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.enumeration.Direction;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.callback.TickCallback;
import ch.algotrader.esper.callback.TradeCallback;
import ch.algotrader.esper.callback.TradePersistedCallback;
import ch.algotrader.vo.ClosePositionVO;
import ch.algotrader.vo.OpenPositionVO;

public class CallbackEsperTest extends EsperTestBase {

    private EPServiceProvider epService;
    private EPRuntime epRuntime;

    @Before
    public void setupEsper() throws Exception {

        Configuration config = new Configuration();
        config.configure("/META-INF/esper-common.cfg.xml");
        config.configure("/META-INF/esper-core.cfg.xml");

        epService = EPServiceProviderManager.getDefaultProvider(config);
        epRuntime = epService.getEPRuntime();
    }

    @After
    public void cleanUpEsper() {
        if (epService != null) {
            epService.destroy();
        }
    }

    @Test
    public void testFirstTick() throws Exception {

        EPStatement statement = deployPreparedStatement(epService,
                getClass().getResource("/module-prepared.epl"), "ON_FIRST_TICK",
                2, new long[] {123L, 456L});

        final Queue<List<TickVO>> tickListQueue = new ConcurrentLinkedQueue<>();
        statement.setSubscriber(new TickCallback() {
            @Override
            public void onFirstTick(final String strategyName, final List<TickVO> ticks) throws Exception {
                tickListQueue.add(ticks);
            }

        });

        TickVO tick1 = new TickVO(0L, new Date(), FeedType.IB.name(), 123L, new BigDecimal("1.1"), new Date(), new BigDecimal("1.11"), new BigDecimal("1.09"), 0, 0, 0);
        epRuntime.sendEvent(tick1);

        Assert.assertNull(tickListQueue.poll());

        TickVO tick2 = new TickVO(0L, new Date(), FeedType.IB.name(), 333L, new BigDecimal("1.2"), new Date(), new BigDecimal("1.21"), new BigDecimal("1.19"), 0, 0, 0);
        epRuntime.sendEvent(tick2);

        Assert.assertNull(tickListQueue.poll());

        TickVO tick3 = new TickVO(0L, new Date(), FeedType.IB.name(), 456L, new BigDecimal("1.3"), new Date(), new BigDecimal("1.31"), new BigDecimal("1.29"), 0, 0, 0);
        epRuntime.sendEvent(tick3);

        List<TickVO> ticks = tickListQueue.poll();
        Assert.assertNotNull(ticks);
        Assert.assertEquals(2, ticks.size());
        Assert.assertSame(tick1, ticks.get(0));
        Assert.assertSame(tick3, ticks.get(1));
    }

    @Test
    public void testTradeCompleted() throws Exception {

        EPStatement statement = deployPreparedStatement(epService,
                getClass().getResource("/module-prepared.epl"), "ON_TRADE_COMPLETED",
                2, new String[] {"this-order", "that-order"});

        final Queue<List<OrderStatusVO>> orderStatusListQueue = new ConcurrentLinkedQueue<>();
        statement.setSubscriber(new TradeCallback(false) {
            @Override
            public void onTradeCompleted(final List<OrderStatusVO> orderStatusList) throws Exception {
                orderStatusListQueue.add(orderStatusList);
            }
        });

        OrderStatusVO orderStatus1 = new OrderStatusVO(0L, new Date(), Status.EXECUTED, 10L, 20L, 0L, "this-order", 0L, 0L);
        epRuntime.sendEvent(orderStatus1);

        Assert.assertNull(orderStatusListQueue.poll());

        OrderStatusVO orderStatus2 = new OrderStatusVO(0L, new Date(), Status.PARTIALLY_EXECUTED, 10L, 20L, 0L, "this-order", 0L, 0L);
        epRuntime.sendEvent(orderStatus2);

        Assert.assertNull(orderStatusListQueue.poll());

        OrderStatusVO orderStatus3 = new OrderStatusVO(0L, new Date(), Status.EXECUTED, 10L, 20L, 0L, "blah", 0L, 0L);
        epRuntime.sendEvent(orderStatus3);

        Assert.assertNull(orderStatusListQueue.poll());

        OrderStatusVO orderStatus4 = new OrderStatusVO(0L, new Date(), Status.CANCELED, 10L, 20L, 0L, "that-order", 0L, 0L);
        epRuntime.sendEvent(orderStatus4);

        List<OrderStatusVO> orderStati = orderStatusListQueue.poll();
        Assert.assertNotNull(orderStati);
        Assert.assertEquals(2, orderStati.size());
        Assert.assertSame(orderStatus1, orderStati.get(0));
        Assert.assertSame(orderStatus4, orderStati.get(1));
    }

    @Test
    public void testInternalTradeCompleted() throws Exception {

        EPStatement statement = deployPreparedStatement(epService,
                getClass().getResource("/module-server-prepared.epl"), "ON_TRADE_COMPLETED",
                "internal-order");

        final Queue<OrderStatus> orderStatusQueue = new ConcurrentLinkedQueue<>();
        statement.setSubscriber(new Object() {

            public void update(final OrderStatus orderStatus) throws Exception {
                orderStatusQueue.add(orderStatus);
            }

        });

        MarketOrder order = MarketOrder.Factory.newInstance();
        order.setIntId("internal-order");

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setStatus(Status.PARTIALLY_EXECUTED);
        orderStatus1.setIntId("internal-order");
        orderStatus1.setOrder(order);

        epRuntime.sendEvent(orderStatus1);

        Assert.assertNull(orderStatusQueue.poll());

        OrderStatus orderStatus2 = OrderStatus.Factory.newInstance();
        orderStatus2.setStatus(Status.EXECUTED);
        orderStatus2.setIntId("other-internal-order");

        epRuntime.sendEvent(orderStatus2);

        Assert.assertNull(orderStatusQueue.poll());

        OrderStatus orderStatus3 = OrderStatus.Factory.newInstance();
        orderStatus3.setStatus(Status.EXECUTED);
        orderStatus3.setIntId("internal-order");
        orderStatus3.setOrder(order);

        epRuntime.sendEvent(orderStatus3);

        Assert.assertSame(orderStatus3, orderStatusQueue.poll());
    }

    @Test
    public void testPositionOpen() throws Exception {

        EPStatement statement = deployPreparedStatement(epService,
                getClass().getResource("/module-prepared.epl"), "ON_OPEN_POSITION", 123L);

        final Queue<OpenPositionVO> openPositionQueue = new ConcurrentLinkedQueue<>();
        statement.setSubscriber(new Object() {

            public void update(final OpenPositionVO position) throws Exception {
                openPositionQueue.add(position);
            }

        });

        OpenPositionVO openPosition1 = new OpenPositionVO(0L, 111L, "blah", 10L, Direction.LONG);
        epRuntime.sendEvent(openPosition1);

        Assert.assertNull(openPositionQueue.poll());

        OpenPositionVO openPosition2 = new OpenPositionVO(0L, 123L, "blah", 10L, Direction.LONG);
        epRuntime.sendEvent(openPosition2);

        Assert.assertSame(openPosition2, openPositionQueue.poll());
    }

    @Test
    public void testPositionClosed() throws Exception {

        EPStatement statement = deployPreparedStatement(epService,
                getClass().getResource("/module-prepared.epl"), "ON_CLOSE_POSITION", 123L);

        final Queue<ClosePositionVO> closedPositionQueue = new ConcurrentLinkedQueue<>();
        statement.setSubscriber(new Object() {

            public void update(final ClosePositionVO position) throws Exception {
                closedPositionQueue.add(position);
            }

        });

        ClosePositionVO openPosition1 = new ClosePositionVO(0L, 111L, "blah", 10L, Direction.LONG);
        epRuntime.sendEvent(openPosition1);

        Assert.assertNull(closedPositionQueue.poll());

        ClosePositionVO openPosition2 = new ClosePositionVO(0L, 123L, "blah", 10L, Direction.LONG);
        epRuntime.sendEvent(openPosition2);

        Assert.assertSame(openPosition2, closedPositionQueue.poll());
    }

    @Test
    public void testTradePersisted() throws Exception {

        EPStatement statement = deployPreparedStatement(epService,
                getClass().getResource("/module-prepared.epl"), "ON_TRADE_PERSISTED",
                2, new String[] {"this-order", "that-order"});

        final Queue<List<OrderCompletionVO>> orderComlpetionListQueue = new ConcurrentLinkedQueue<>();
        statement.setSubscriber(new TradePersistedCallback() {
            @Override
            public void onTradePersisted(final List<OrderCompletionVO> orderCompletionList) throws Exception {
                orderComlpetionListQueue.add(orderCompletionList);
            }
        });

        OrderCompletionVO orderCompletion1 = new OrderCompletionVO("this-order", "blah", new Date(), Status.EXECUTED, 10L, 20L, null, null, null, null, 0, 0.0d);
        epRuntime.sendEvent(orderCompletion1);

        Assert.assertNull(orderComlpetionListQueue.poll());

        OrderCompletionVO orderCompletion2 = new OrderCompletionVO("blah", "blah", new Date(), Status.EXECUTED, 10L, 20L, null, null, null, null, 0, 0.0d);
        epRuntime.sendEvent(orderCompletion2);

        Assert.assertNull(orderComlpetionListQueue.poll());

        OrderCompletionVO orderCompletion3 = new OrderCompletionVO("that-order", "blah", new Date(), Status.CANCELED, 10L, 20L, null, null, null, null, 0, 0.0d);
        epRuntime.sendEvent(orderCompletion3);

        List<OrderCompletionVO> orderCompletionList = orderComlpetionListQueue.poll();
        Assert.assertNotNull(orderCompletionList);
        Assert.assertEquals(2, orderCompletionList.size());
        Assert.assertSame(orderCompletion1, orderCompletionList.get(0));
        Assert.assertSame(orderCompletion3, orderCompletionList.get(1));
    }

}
