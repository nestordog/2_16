package ch.algotrader.adapter.lmax;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.Index;
import ch.algotrader.entity.security.IndexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.security.StockImpl;
import ch.algotrader.enumeration.Currency;
import quickfix.Group;
import quickfix.field.MDEntryType;
import quickfix.field.NoMDEntryTypes;
import quickfix.field.NoRelatedSym;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.SubscriptionRequestType;
import quickfix.fix44.MarketDataRequest;

public class TestLMAXMarketDataRequestFactory {

    private LMAXInstrumentCodeMapper mapper;
    private LMAXFixMarketDataRequestFactory requestFactory;

    @Before
    public void setup() throws Exception {

        mapper = LMAXInstrumentCodeMapper.load();
        requestFactory = new LMAXFixMarketDataRequestFactory(mapper);
    }

    @Test
    public void testRequestForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketDataRequest marketDataRequest = requestFactory.create(forex, new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));

        Assert.assertEquals(1, marketDataRequest.getGroupCount(NoRelatedSym.FIELD));
        Group symGroup = marketDataRequest.getGroup(1, NoRelatedSym.FIELD);
        Assert.assertEquals(mapper.mapToCode("EUR/USD"), symGroup.getString(SecurityID.FIELD));
        Assert.assertEquals("8", symGroup.getString(SecurityIDSource.FIELD));

        Assert.assertEquals(2, marketDataRequest.getGroupCount(NoMDEntryTypes.FIELD));
        Group bidGroup = marketDataRequest.getGroup(1, NoMDEntryTypes.FIELD);
        Assert.assertEquals(MDEntryType.BID, bidGroup.getChar(MDEntryType.FIELD));

        Group offerGroup = marketDataRequest.getGroup(2, NoMDEntryTypes.FIELD);
        Assert.assertEquals(MDEntryType.OFFER, offerGroup.getChar(MDEntryType.FIELD));

        Assert.assertEquals(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES), marketDataRequest.getSubscriptionRequestType());
    }

    @Test
    public void testRequestIndex() throws Exception {

        Index idx = new IndexImpl();
        idx.setSymbol("NDX");

        MarketDataRequest marketDataRequest = requestFactory.create(idx, new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));

        Assert.assertEquals(1, marketDataRequest.getGroupCount(NoRelatedSym.FIELD));
        Group symGroup = marketDataRequest.getGroup(1, NoRelatedSym.FIELD);
        Assert.assertEquals(mapper.mapToCode("NDX"), symGroup.getString(SecurityID.FIELD));
        Assert.assertEquals("8", symGroup.getString(SecurityIDSource.FIELD));

        Assert.assertEquals(2, marketDataRequest.getGroupCount(NoMDEntryTypes.FIELD));
        Group bidGroup = marketDataRequest.getGroup(1, NoMDEntryTypes.FIELD);
        Assert.assertEquals(MDEntryType.BID, bidGroup.getChar(MDEntryType.FIELD));

        Group offerGroup = marketDataRequest.getGroup(2, NoMDEntryTypes.FIELD);
        Assert.assertEquals(MDEntryType.OFFER, offerGroup.getChar(MDEntryType.FIELD));

        Assert.assertEquals(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES), marketDataRequest.getSubscriptionRequestType());
    }

    @Test(expected = FixApplicationException.class)
    public void testRequestUnsupportedSecurity() throws Exception {

        Stock stock = new StockImpl();
        stock.setSymbol("stuff");

        requestFactory.create(stock, new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));
    }

}
