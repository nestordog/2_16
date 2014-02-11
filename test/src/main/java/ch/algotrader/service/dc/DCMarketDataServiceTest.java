package ch.algotrader.service.dc;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import quickfix.Group;
import quickfix.Message;
import quickfix.field.MDEntryType;
import quickfix.field.NoMDEntryTypes;
import quickfix.field.NoRelatedSym;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.fix44.MarketDataRequest;
import ch.algotrader.ServiceLocator;
import ch.algotrader.adapter.fix.DefaultFixSessionLifecycle;
import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.fix.FixSessionLifecycle;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.marketData.TickDao;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.hibernate.GenericDao;
import ch.algotrader.service.LocalServiceTest;
import ch.algotrader.vo.SubscribeTickVO;

public class DCMarketDataServiceTest extends LocalServiceTest {

    private static HibernateTransactionManager transactionManager;
    private TransactionStatus transaction;


    @BeforeClass
    public static void setupClass() {

        transactionManager = ServiceLocator.instance().getService("transactionManager",HibernateTransactionManager.class);
    }

    @Before
    public void setup() {

        this.transaction = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_MANDATORY & TransactionDefinition.ISOLATION_READ_UNCOMMITTED));
    }

    @After
    public void after() {

        this.transaction.setRollbackOnly();
        transactionManager.rollback(this.transaction);
    }

    @AfterClass
    public static void afterClass() {

        ServiceLocator.instance().shutdown();
    }

    @Test
    public void testSecurityLookup() throws Exception {

        GenericDao dao = ServiceLocator.instance().getService("genericDao",GenericDao.class);
        Security security = (Security) dao.get(SecurityImpl.class, 4);

        Assert.assertNotNull(security);
        Assert.assertEquals(4, security.getId());
        Assert.assertEquals("SMI", security.getSymbol());
    }

    @Test
    public void testInitialSubscriptions() throws Exception {

        // get Dao's
        SecurityDao securityDao = ServiceLocator.instance().getService("securityDao", SecurityDao.class);
        TickDao tickDao = ServiceLocator.instance().getService("tickDao", TickDao.class);

        // mock FixAdapter
        FixAdapter fixAdapter = Mockito.mock(FixAdapter.class);
        Engine engine = Mockito.mock(Engine.class);
        EngineLocator.instance().setEngine("BASE", engine);
        FixSessionLifecycle lifeCycle = new DefaultFixSessionLifecycle();

        lifeCycle.create();
        lifeCycle.logon();

        Assert.assertEquals(ConnectionState.LOGGED_ON, lifeCycle.getConnectionState());

        // create externalMarketDataServiceImpl
        DCFixMarketDataServiceImpl impl = new DCFixMarketDataServiceImpl();
        impl.setFixAdapter(fixAdapter);
        impl.setFixSessionLifecycle(lifeCycle);

        impl.setSecurityDao(securityDao);
        impl.setTickDao(tickDao);

        DCFixMarketDataServiceImpl externalMarketDataService = Mockito.spy(impl);

        // do initSubscriptions
        externalMarketDataService.initSubscriptions();

        // verify externalMarketDataService.subscribe
        ArgumentCaptor<Security> securityArgument = ArgumentCaptor.forClass(Security.class);
        Mockito.verify(externalMarketDataService).subscribe(securityArgument.capture());

        List<Security> allSecurities = securityArgument.getAllValues();

        Assert.assertNotNull(allSecurities);
        Assert.assertEquals(1, allSecurities.size());
        Security security = allSecurities.get(0);

        Assert.assertNotNull(security);
        Assert.assertEquals("EUR.USD", security.getSymbol());

        // verify engine.sendEvent
        ArgumentCaptor<Object> argumentCaptor2 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(engine).sendEvent(argumentCaptor2.capture());

        List<Object> allEvents = argumentCaptor2.getAllValues();

        Assert.assertNotNull(allEvents);
        Assert.assertEquals(1, allEvents.size());
        Object event = allEvents.get(0);

        Assert.assertTrue(event instanceof SubscribeTickVO);
        SubscribeTickVO subscribeTick = (SubscribeTickVO) event;
        Tick tick = subscribeTick.getTick();
        Assert.assertNotNull(tick);
        Assert.assertSame(security, tick.getSecurity());

        // verify fixAdapter.sendMessage
        ArgumentCaptor<Message> argumentCaptor3 = ArgumentCaptor.forClass(Message.class);
        Mockito.verify(fixAdapter).sendMessage(argumentCaptor3.capture(), Matchers.eq(impl.getSessionQualifier()));

        List<Message> allMessages = argumentCaptor3.getAllValues();

        Assert.assertNotNull(allMessages);
        Assert.assertEquals(1, allMessages.size());
        Message message = allMessages.get(0);

        Assert.assertTrue(message instanceof MarketDataRequest);

        MarketDataRequest marketDataRequest = (MarketDataRequest) message;

        Assert.assertEquals(1, marketDataRequest.getGroupCount(NoRelatedSym.FIELD));
        Group symGroup = marketDataRequest.getGroup(1, NoRelatedSym.FIELD);
        Assert.assertEquals("EUR/USD", symGroup.getString(Symbol.FIELD));

        Assert.assertEquals(2, marketDataRequest.getGroupCount(NoMDEntryTypes.FIELD));
        Group bidGroup = marketDataRequest.getGroup(1, NoMDEntryTypes.FIELD);
        Assert.assertEquals(MDEntryType.BID, bidGroup.getChar(MDEntryType.FIELD));

        Group offerGroup = marketDataRequest.getGroup(2, NoMDEntryTypes.FIELD);
        Assert.assertEquals(MDEntryType.OFFER, offerGroup.getChar(MDEntryType.FIELD));

        Assert.assertEquals(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES), marketDataRequest.getSubscriptionRequestType());

        // verify no event has been sent to the engine
        Mockito.verify(engine, Mockito.never()).executeQuery(Mockito.anyString());
    }

    @Test
    public void testSubscribeUnsubscribe() throws Exception {

        // get Dao's
        SecurityDao securityDao = ServiceLocator.instance().getService("securityDao", SecurityDao.class);
        TickDao tickDao = ServiceLocator.instance().getService("tickDao", TickDao.class);

        // mock FixAdapter
        FixAdapter fixAdapter = Mockito.mock(FixAdapter.class);
        Engine engine = Mockito.mock(Engine.class);
        EngineLocator.instance().setEngine("BASE", engine);
        FixSessionLifecycle lifeCycle = new DefaultFixSessionLifecycle();

        lifeCycle.create();
        lifeCycle.logon();
        lifeCycle.subscribe();

        Assert.assertEquals(ConnectionState.SUBSCRIBED, lifeCycle.getConnectionState());

        // create externalMarketDataServiceImpl
        DCFixMarketDataServiceImpl externalMarketDataServiceImpl = new DCFixMarketDataServiceImpl();
        externalMarketDataServiceImpl.setFixAdapter(fixAdapter);
        externalMarketDataServiceImpl.setFixSessionLifecycle(lifeCycle);

        externalMarketDataServiceImpl.setSecurityDao(securityDao);
        externalMarketDataServiceImpl.setTickDao(tickDao);

        DCFixMarketDataServiceImpl marketDataService = Mockito.spy(externalMarketDataServiceImpl);

        // select test security
        String symbol = "EUR.CHF";
        String dcSymbol = "EUR/CHF";
        int id = 8;

        Security forex = securityDao.get(id);
        Assert.assertEquals(symbol, forex.getSymbol());

        // Do subscribe
        marketDataService.subscribe(forex);

        // verify engine.sendEvent
        ArgumentCaptor<Object> argumentCaptor3 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(engine).sendEvent(argumentCaptor3.capture());

        List<Object> allEvents = argumentCaptor3.getAllValues();

        Assert.assertNotNull(allEvents);
        Assert.assertEquals(1, allEvents.size());
        Object event = allEvents.get(0);

        Assert.assertTrue(event instanceof SubscribeTickVO);
        SubscribeTickVO subscribeTick = (SubscribeTickVO) event;
        Tick tick = subscribeTick.getTick();
        Assert.assertNotNull(tick);
        Assert.assertSame(forex, tick.getSecurity());

        // verify fixSessionFactory.sendMessage
        ArgumentCaptor<Message> argumentCaptor4 = ArgumentCaptor.forClass(Message.class);
        Mockito.verify(fixAdapter).sendMessage(argumentCaptor4.capture(), Matchers.eq(externalMarketDataServiceImpl.getSessionQualifier()));

        List<Message> allMessages1 = argumentCaptor4.getAllValues();

        Assert.assertNotNull(allMessages1);
        Assert.assertEquals(1, allMessages1.size());
        Message message1 = allMessages1.get(0);

        Assert.assertTrue(message1 instanceof MarketDataRequest);

        MarketDataRequest marketDataRequest1 = (MarketDataRequest) message1;

        Assert.assertEquals(1, marketDataRequest1.getGroupCount(NoRelatedSym.FIELD));
        Group symGroup1 = marketDataRequest1.getGroup(1, NoRelatedSym.FIELD);
        Assert.assertEquals(dcSymbol, symGroup1.getString(Symbol.FIELD));

        Assert.assertEquals(2, marketDataRequest1.getGroupCount(NoMDEntryTypes.FIELD));
        Group bidGroup1 = marketDataRequest1.getGroup(1, NoMDEntryTypes.FIELD);
        Assert.assertEquals(MDEntryType.BID, bidGroup1.getChar(MDEntryType.FIELD));

        Group offerGroup1 = marketDataRequest1.getGroup(2, NoMDEntryTypes.FIELD);
        Assert.assertEquals(MDEntryType.OFFER, offerGroup1.getChar(MDEntryType.FIELD));

        Assert.assertEquals(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES), marketDataRequest1.getSubscriptionRequestType());

        // verify engine.executeQuery does not get called
        Mockito.verify(engine, Mockito.never()).executeQuery(Mockito.anyString());

        // reset the engine
        Mockito.reset(fixAdapter, engine);

        // Do unsubscribe
        marketDataService.unsubscribe(forex);

        // verify no event has been sent to the engine
        Mockito.verify(engine, Mockito.never()).sendEvent(Mockito.any());

        // verify fixSessionFactory.sendMessage
        ArgumentCaptor<Message> argumentCaptor6 = ArgumentCaptor.forClass(Message.class);
        Mockito.verify(fixAdapter).sendMessage(argumentCaptor6.capture(), Matchers.eq(externalMarketDataServiceImpl.getSessionQualifier()));

        List<Message> allMessages2 = argumentCaptor6.getAllValues();

        Assert.assertNotNull(allMessages2);
        Assert.assertEquals(1, allMessages2.size());
        Message message2 = allMessages2.get(0);

        Assert.assertTrue(message2 instanceof MarketDataRequest);

        MarketDataRequest marketDataRequest2 = (MarketDataRequest) message2;

        Assert.assertEquals(1, marketDataRequest2.getGroupCount(NoRelatedSym.FIELD));
        Group symGroup2 = marketDataRequest2.getGroup(1, NoRelatedSym.FIELD);
        Assert.assertEquals(dcSymbol, symGroup2.getString(Symbol.FIELD));

        Assert.assertEquals(2, marketDataRequest2.getGroupCount(NoMDEntryTypes.FIELD));
        Group bidGroup2 = marketDataRequest2.getGroup(1, NoMDEntryTypes.FIELD);
        Assert.assertEquals(MDEntryType.BID, bidGroup2.getChar(MDEntryType.FIELD));

        Group offerGroup2 = marketDataRequest2.getGroup(2, NoMDEntryTypes.FIELD);
        Assert.assertEquals(MDEntryType.OFFER, offerGroup2.getChar(MDEntryType.FIELD));

        Assert.assertEquals(new SubscriptionRequestType(SubscriptionRequestType.DISABLE_PREVIOUS_SNAPSHOT_PLUS_UPDATE_REQUEST), marketDataRequest2.getSubscriptionRequestType());

        // verify the esper delete statement has been executed
        Mockito.verify(engine).executeQuery("delete from TickWindow where security.id = " + id);
    }
}
