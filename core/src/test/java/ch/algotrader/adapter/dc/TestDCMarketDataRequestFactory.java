package ch.algotrader.adapter.dc;

import org.junit.Assert;
import org.junit.Test;

import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.security.StockImpl;
import ch.algotrader.enumeration.Currency;
import quickfix.Group;
import quickfix.field.MDEntryType;
import quickfix.field.NoMDEntryTypes;
import quickfix.field.NoRelatedSym;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.fix44.MarketDataRequest;

public class TestDCMarketDataRequestFactory {

    @Test
    public void testRequestForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        DCFixMarketDataRequestFactory requestFactory = new DCFixMarketDataRequestFactory();

        MarketDataRequest marketDataRequest = requestFactory.create(forex, new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));

        Assert.assertEquals(1, marketDataRequest.getGroupCount(NoRelatedSym.FIELD));
        Group symGroup = marketDataRequest.getGroup(1, NoRelatedSym.FIELD);
        Assert.assertEquals("EUR/USD", symGroup.getString(Symbol.FIELD));

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
        stock.setSymbol("MSFT");

        DCFixMarketDataRequestFactory requestFactory = new DCFixMarketDataRequestFactory();

        requestFactory.create(stock, new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));
    }

}
