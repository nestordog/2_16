package ch.algotrader.adapter.dc;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import quickfix.CompositeLogFactory;
import quickfix.DefaultSessionFactory;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.SLF4JLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.fix44.MarketDataRequest;
import ch.algotrader.adapter.fix.DefaultFixApplication;
import ch.algotrader.adapter.fix.DefaultFixSessionLifecycle;
import ch.algotrader.adapter.fix.NoopSessionStateListener;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.esper.AbstractEngine;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.vo.AskVO;
import ch.algotrader.vo.BidVO;

public class DCFixFeedMessageHandlerTest {

    private Session session;
    private SocketInitiator socketInitiator;

    @Before
    public void setup() throws Exception {

        SessionSettings settings;
        ClassLoader cl = DCFixFeedMessageHandlerTest.class.getClassLoader();
        InputStream instream = cl.getResourceAsStream("dcf-test.cfg");
        try {
            settings = new SessionSettings(instream);
        } finally {
            instream.close();
        }
        SessionID sessionId = settings.sectionIterator().next();

        DCLogonMessageHandler dcLogonHandler = new DCLogonMessageHandler();
        dcLogonHandler.setSettings(settings);

        DefaultFixApplication fixApplication = new DefaultFixApplication(sessionId, new DCFixMarketDataMessageHandler(), dcLogonHandler, new DefaultFixSessionLifecycle());

//        Log4FIX log4Fix = Log4FIX.createForLiveUpdates(settings);
//        LogFactory logFactory = new CompositeLogFactory(new LogFactory[] { new SLF4JLogFactory(settings), log4Fix.getLogFactory() });
//        log4Fix.show();

        LogFactory logFactory = new CompositeLogFactory(new LogFactory[] { new SLF4JLogFactory(settings) });

        DefaultSessionFactory sessionFactory = new DefaultSessionFactory(fixApplication, new MemoryStoreFactory(), logFactory);

        SocketInitiator socketInitiator = new SocketInitiator(sessionFactory, settings);
        socketInitiator.start();

        socketInitiator.createDynamicSession(sessionId);

        this.session = Session.lookupSession(sessionId);
    }

    @After
    public void shutDown() throws Exception {

        if (this.socketInitiator != null) {
            this.socketInitiator.stop();
            this.socketInitiator = null;
        }
    }

    private MarketDataRequest createSubscriptionRequest(Security security) {

        MarketDataRequest request = new MarketDataRequest();
        request.set(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));
        request.set(new MDReqID(DCUtil.getSymbol(security)));

        MarketDataRequest.NoMDEntryTypes bid = new MarketDataRequest.NoMDEntryTypes();
        bid.set(new MDEntryType(MDEntryType.BID));
        request.addGroup(bid);

        MarketDataRequest.NoMDEntryTypes offer = new MarketDataRequest.NoMDEntryTypes();
        offer.set(new MDEntryType(MDEntryType.OFFER));
        request.addGroup(offer);

        MarketDataRequest.NoRelatedSym symGroup = new MarketDataRequest.NoRelatedSym();
        symGroup.set(new Symbol(DCUtil.getSymbol(security)));
        request.addGroup(symGroup);

        request.set(new MarketDepth(1));
        request.set(new MDUpdateType(MDUpdateType.FULL_REFRESH));
        return request;
    }

    @Test
    public void testMarketDataFeed() throws Exception {

        final LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();

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


        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketDataRequest request = createSubscriptionRequest(forex);

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

        if (!latch.await(30, TimeUnit.SECONDS)) {
            Assert.fail("Session was not established within specific time limit");
        }

        if (!this.session.isLoggedOn()) {
            Assert.fail("Session logon failed");
        }

        this.session.send(request);

        int tickerId = DCUtil.getTickerId(forex);

        for (int i = 0; i < 10; i++) {

            Object event = queue.poll(30, TimeUnit.SECONDS);
            if (event == null) {
                Assert.fail("No event received within specific time limit");
            }

            if (event instanceof BidVO) {
                BidVO bid = (BidVO) event;
                Assert.assertEquals(tickerId, bid.getTickerId());
            } else if (event instanceof AskVO) {
                AskVO ask = (AskVO) event;
                Assert.assertEquals(tickerId, ask.getTickerId());
            } else {
                Assert.fail("Unexpected event type: " + event.getClass());
            }
        }

        this.session.logout("Testing");
        this.session.close();
    }

}
