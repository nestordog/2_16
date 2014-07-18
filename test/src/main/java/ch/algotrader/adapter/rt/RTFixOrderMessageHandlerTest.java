package ch.algotrader.adapter.rt;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.algotrader.adapter.fix.DefaultFixApplication;
import ch.algotrader.adapter.fix.DefaultFixSessionLifecycle;
import ch.algotrader.adapter.fix.FixConfigUtils;
import ch.algotrader.adapter.fix.NoopSessionStateListener;
import ch.algotrader.adapter.rt.RTFixOrderMessageFactory;
import ch.algotrader.adapter.rt.RTFixOrderMessageHandler;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.security.StockImpl;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.LimitOrderImpl;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.AbstractEngine;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.service.LookupService;
import quickfix.DefaultSessionFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelRequest;

public class RTFixOrderMessageHandlerTest {

    private LinkedBlockingQueue<Object> eventQueue;
    private LookupService lookupService;
    private RTFixOrderMessageFactory messageFactory;
    private RTFixOrderMessageHandler messageHandler;
    private Session session;
    private SocketInitiator socketInitiator;

    @Before
    public void setup() throws Exception {

        final LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
        this.eventQueue = queue;

        EngineLocator.instance().setEngine(StrategyImpl.BASE, new AbstractEngine() {

            @Override
            public void sendEvent(Object obj) {
                try {
                    queue.put(obj);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public List executeQuery(String query) {
                return null;
            }
        });


        SessionSettings settings = FixConfigUtils.loadSettings();
        SessionID sessionId = FixConfigUtils.getSessionID(settings, "RTT");

        this.lookupService = Mockito.mock(LookupService.class);
        RTFixOrderMessageHandler messageHandlerImpl = new RTFixOrderMessageHandler();
        messageHandlerImpl.setLookupService(lookupService);
        this.messageHandler = Mockito.spy(messageHandlerImpl);
        this.messageFactory = new RTFixOrderMessageFactory();

        DefaultFixApplication fixApplication = new DefaultFixApplication(sessionId, messageHandler, null, new DefaultFixSessionLifecycle());

        LogFactory logFactory = new ScreenLogFactory(true, true, true);

        // RealTick does not support SeqNum reset on logon. Need to use persistent store.
        DefaultSessionFactory sessionFactory = new DefaultSessionFactory(fixApplication, new FileStoreFactory(settings), logFactory);

        SocketInitiator socketInitiator = new SocketInitiator(sessionFactory, settings);
        socketInitiator.start();

        socketInitiator.createDynamicSession(sessionId);

        this.session = Session.lookupSession(sessionId);

        final CountDownLatch latch = new CountDownLatch(1);

        this.session.addStateListener(new NoopSessionStateListener() {

            @Override
            public void onDisconnect() {
                latch.countDown();
            }

            @Override
            public void onLogon() {
                latch.countDown();
            }

        });

        if (!this.session.isLoggedOn()) {
            latch.await(30, TimeUnit.SECONDS);
        }

        if (!this.session.isLoggedOn()) {
            Assert.fail("Session logon failed");
        }

        // Purge the queue
        while (eventQueue.poll(5, TimeUnit.SECONDS) != null) {
        }
    }

    @After
    public void shutDown() throws Exception {

        if (this.session != null) {
            if (this.session.isLoggedOn()) {
                this.session.logout("Testing");
            }
            this.session.close();
            this.session = null;
        }
        if (this.socketInitiator != null) {
            this.socketInitiator.stop();
            this.socketInitiator = null;
        }
    }

    @Test
    public void testMarketOrder() throws Exception {

        String orderId = Long.toHexString(System.currentTimeMillis());

        SecurityFamily securityFamily = new SecurityFamilyImpl();
        securityFamily.setCurrency(Currency.USD);

        Stock stock = new StockImpl();
        stock.setSymbol("MSFT");
        stock.setSecurityFamily(securityFamily);

        Account testAccount = new AccountImpl();
        testAccount.setBroker(Broker.RT);
        testAccount.setExtAccount("20580736-2");

        MarketOrder order = new MarketOrderImpl();
        order.setAccount(testAccount);
        order.setSecurity(stock);
        order.setQuantity(100);
        order.setSide(Side.BUY);

        NewOrderSingle message = messageFactory.createNewOrderMessage(order, orderId);

        Mockito.when(lookupService.getOpenOrderByRootIntId(orderId)).thenReturn(order);

        session.send(message);

        Object event1 = eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals(orderId, orderStatus1.getIntId());
        Assert.assertNotNull(orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());

        Object event2 = eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event2 instanceof OrderStatus);
        OrderStatus orderStatus2 = (OrderStatus) event2;
        Assert.assertEquals(orderId, orderStatus2.getIntId());
        Assert.assertNotNull(orderStatus2.getExtId());
        Assert.assertEquals(Status.EXECUTED, orderStatus2.getStatus());
        Assert.assertSame(order, orderStatus2.getOrder());
        Assert.assertEquals(100, orderStatus2.getFilledQuantity());

        Object event3 = eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event3 instanceof Fill);
        Fill fill1 = (Fill) event3;
        Assert.assertEquals(orderStatus2.getExtId(), fill1.getExtId());
        Assert.assertSame(order, fill1.getOrder());
        Assert.assertNotNull(fill1.getExtDateTime());
        Assert.assertEquals(ch.algotrader.enumeration.Side.BUY, fill1.getSide());
        Assert.assertEquals(100L, fill1.getQuantity());
        Assert.assertNotNull(fill1.getPrice());

        Object event4 = eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event4);
    }

    @Test
    public void testInvalidOrder() throws Exception {

        String orderId = Long.toHexString(System.currentTimeMillis());

        SecurityFamily securityFamily = new SecurityFamilyImpl();
        securityFamily.setCurrency(Currency.USD);

        Stock stock = new StockImpl();
        stock.setSymbol("grass");
        stock.setSecurityFamily(securityFamily);

        Account testAccount = new AccountImpl();
        testAccount.setBroker(Broker.RT);
        testAccount.setExtAccount("20580736-2");

        MarketOrder order = new MarketOrderImpl();
        order.setAccount(testAccount);
        order.setSecurity(stock);
        order.setQuantity(100);
        order.setSide(Side.BUY);

        NewOrderSingle message = messageFactory.createNewOrderMessage(order, orderId);

        session.send(message);

        Object event4 = eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event4);
    }

    @Test
    public void testLimitOrderCancel() throws Exception {

        String orderId1 = Long.toHexString(System.currentTimeMillis());

        SecurityFamily securityFamily = new SecurityFamilyImpl();
        securityFamily.setCurrency(Currency.USD);

        Stock stock = new StockImpl();
        stock.setSymbol("MSFT");
        stock.setSecurityFamily(securityFamily);

        Account testAccount = new AccountImpl();
        testAccount.setBroker(Broker.RT);
        testAccount.setExtAccount("20580736-2");

        LimitOrder order = new LimitOrderImpl();
        order.setAccount(testAccount);
        order.setSecurity(stock);
        order.setQuantity(100);
        order.setSide(Side.BUY);
        order.setLimit(new BigDecimal("35.0"));

        NewOrderSingle message1 = messageFactory.createNewOrderMessage(order, orderId1);

        Mockito.when(lookupService.getOpenOrderByRootIntId(orderId1)).thenReturn(order);

        session.send(message1);

        Object event1 = eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals(orderId1, orderStatus1.getIntId());
        Assert.assertNotNull(orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());

        String orderId2 = Long.toHexString(System.currentTimeMillis());

        order.setIntId(orderId1);
        OrderCancelRequest message2 = messageFactory.createOrderCancelMessage(order, orderId2);

        Mockito.when(lookupService.getOpenOrderByRootIntId(orderId2)).thenReturn(order);

        session.send(message2);

        Object event2 = eventQueue.poll(20, TimeUnit.SECONDS);

        Assert.assertTrue(event2 instanceof OrderStatus);
        OrderStatus orderStatus2 = (OrderStatus) event2;
        Assert.assertEquals(orderId2, orderStatus2.getIntId());
        Assert.assertNotNull(orderStatus2.getExtId());
        Assert.assertEquals(Status.CANCELED, orderStatus2.getStatus());
        Assert.assertSame(order, orderStatus2.getOrder());
        Assert.assertEquals(0, orderStatus2.getFilledQuantity());
        Assert.assertEquals(100, orderStatus2.getRemainingQuantity());

        Object event3 = eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event3);
    }

}
