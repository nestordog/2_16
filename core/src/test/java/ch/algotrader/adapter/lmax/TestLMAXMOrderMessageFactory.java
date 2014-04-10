package ch.algotrader.adapter.lmax;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import quickfix.field.ClOrdID;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.StopPx;
import quickfix.field.TimeInForce;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;
import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.security.StockImpl;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.LimitOrderImpl;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopLimitOrderImpl;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.entity.trade.StopOrderImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TIF;

public class TestLMAXMOrderMessageFactory {

    private LMAXFix44OrderMessageFactory requestFactory;

    @Before
    public void setup() throws Exception {

        this.requestFactory = new LMAXFix44OrderMessageFactory();
    }

    @Test
    public void testMarketOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setLmaxid("4001");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(123);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new SecurityID("4001"), message.getSecurityID());
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new SecurityIDSource("8"), message.getSecurityIDSource());
        Assert.assertEquals(new OrderQty(123), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        Assert.assertFalse(message.isSetField(Price.FIELD));
        Assert.assertFalse(message.isSetField(StopPx.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test(expected = FixApplicationException.class)
    public void testOrderForexUnsupportedSecurityType() throws Exception {

        Stock stock = new StockImpl();
        stock.setSymbol("GOOG");

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(stock);
        order.setSide(Side.BUY);
        order.setQuantity(123);

        this.requestFactory.createNewOrderMessage(order, "test-id");
    }

    @Test(expected = FixApplicationException.class)
    public void testOrderForexUnsupportedForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("RUB.USD");
        forex.setBaseCurrency(Currency.fromString("RUB"));
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(123);

        this.requestFactory.createNewOrderMessage(order, "test-id");
    }

    @Test
    public void testLimitOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setLmaxid("4001");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        LimitOrder order = new LimitOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(123);
        order.setLimit(new BigDecimal("1.345"));

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new SecurityID("4001"), message.getSecurityID());
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new SecurityIDSource("8"), message.getSecurityIDSource());
        Assert.assertEquals(new OrderQty(123), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.LIMIT), message.getOrdType());
        Assert.assertEquals(new Price(1.345), message.getPrice());
        Assert.assertFalse(message.isSetField(StopPx.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testStopOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setLmaxid("4001");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        StopOrder order = new StopOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(123);
        order.setStop(new BigDecimal("1.345"));

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new SecurityID("4001"), message.getSecurityID());
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new SecurityIDSource("8"), message.getSecurityIDSource());
        Assert.assertEquals(new OrderQty(123), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.STOP), message.getOrdType());
        Assert.assertEquals(new StopPx(1.345), message.getStopPx());
        Assert.assertFalse(message.isSetField(Price.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test(expected = FixApplicationException.class)
    public void testOrderUnsupportedType() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        StopLimitOrder order = new StopLimitOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(123);
        order.setLimit(new BigDecimal("1.355"));
        order.setStop(new BigDecimal("1.345"));

        this.requestFactory.createNewOrderMessage(order, "test-id");
    }

    @Test
    public void testModifyLimitOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setLmaxid("4001");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        LimitOrder order = new LimitOrderImpl();
        order.setIntId("test-id");
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(123);
        order.setLimit(new BigDecimal("1.345"));

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id-2");

        Assert.assertNotNull(message);
        Assert.assertEquals(new SecurityID("4001"), message.getSecurityID());
        Assert.assertEquals(new OrigClOrdID("test-id"), message.getOrigClOrdID());
        Assert.assertEquals(new ClOrdID("test-id-2"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new SecurityIDSource("8"), message.getSecurityIDSource());
        Assert.assertEquals(new OrderQty(123), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.LIMIT), message.getOrdType());
        Assert.assertEquals(new Price(1.345), message.getPrice());
        Assert.assertFalse(message.isSetField(StopPx.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test(expected = FixApplicationException.class)
    public void testModifyOrderUnsupportedType() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("test-id");
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(123);

        this.requestFactory.createModifyOrderMessage(order, "test-id-2");
    }

    @Test
    public void testCancelOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setLmaxid("4001");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        LimitOrder order = new LimitOrderImpl();
        order.setIntId("test-id");
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(123);
        order.setLimit(new BigDecimal("1.345"));

        OrderCancelRequest message = this.requestFactory.createOrderCancelMessage(order, "test-id-3");

        Assert.assertNotNull(message);
        Assert.assertEquals(new SecurityID("4001"), message.getSecurityID());
        Assert.assertEquals(new OrigClOrdID("test-id"), message.getOrigClOrdID());
        Assert.assertEquals(new ClOrdID("test-id-3"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new SecurityIDSource("8"), message.getSecurityIDSource());
        Assert.assertEquals(new OrderQty(123), message.getOrderQty());
    }

    @Test(expected = FixApplicationException.class)
    public void testMarketOrderDAY() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setTif(TIF.DAY);
        this.requestFactory.resolveTimeInForce(order);
    }

    @Test(expected = FixApplicationException.class)
    public void testMarketOrderGTC() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setTif(TIF.GTC);
        this.requestFactory.resolveTimeInForce(order);
    }

    @Test(expected = FixApplicationException.class)
    public void testMarketOrderGTD() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setTif(TIF.GTD);
        this.requestFactory.resolveTimeInForce(order);
    }

    @Test
    public void testMarketOrderIOC() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setTif(TIF.IOC);
        Assert.assertEquals(new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL), this.requestFactory.resolveTimeInForce(order));
    }

    @Test
    public void testMarketOrderFOK() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setTif(TIF.FOK);
        Assert.assertEquals(new TimeInForce(TimeInForce.FILL_OR_KILL), this.requestFactory.resolveTimeInForce(order));
    }

    @Test
    public void testLimitedOrderDAY() throws Exception {

        LimitOrder order = new LimitOrderImpl();
        order.setTif(TIF.DAY);
        Assert.assertEquals(new TimeInForce(TimeInForce.DAY), this.requestFactory.resolveTimeInForce(order));
    }

    @Test
    public void testLimitedOrderGTC() throws Exception {

        LimitOrder order = new LimitOrderImpl();
        order.setTif(TIF.GTC);
        Assert.assertEquals(new TimeInForce(TimeInForce.GOOD_TILL_CANCEL), this.requestFactory.resolveTimeInForce(order));
    }

    @Test(expected = FixApplicationException.class)
    public void testLimitedOrderGTD() throws Exception {

        LimitOrder order = new LimitOrderImpl();
        order.setTif(TIF.GTD);
        this.requestFactory.resolveTimeInForce(order);
    }

    @Test
    public void testLimitedOrderIOC() throws Exception {

        LimitOrder order = new LimitOrderImpl();
        order.setTif(TIF.IOC);
        Assert.assertEquals(new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL), this.requestFactory.resolveTimeInForce(order));
    }

    @Test
    public void testLimitedOrderFOK() throws Exception {

        LimitOrder order = new LimitOrderImpl();
        order.setTif(TIF.FOK);
        Assert.assertEquals(new TimeInForce(TimeInForce.FILL_OR_KILL), this.requestFactory.resolveTimeInForce(order));
    }

    @Test
    public void testStopOrderDAY() throws Exception {

        StopOrder order = new StopOrderImpl();
        order.setTif(TIF.DAY);
        Assert.assertEquals(new TimeInForce(TimeInForce.DAY), this.requestFactory.resolveTimeInForce(order));
    }

    @Test
    public void testStopOrderGTC() throws Exception {

        StopOrder order = new StopOrderImpl();
        order.setTif(TIF.GTC);
        Assert.assertEquals(new TimeInForce(TimeInForce.GOOD_TILL_CANCEL), this.requestFactory.resolveTimeInForce(order));
    }

    @Test(expected = FixApplicationException.class)
    public void testStopOrderGTD() throws Exception {

        StopOrder order = new StopOrderImpl();
        order.setTif(TIF.GTD);
        this.requestFactory.resolveTimeInForce(order);
    }

    @Test(expected = FixApplicationException.class)
    public void testStopOrderIOC() throws Exception {

        StopOrder order = new StopOrderImpl();
        order.setTif(TIF.IOC);
        this.requestFactory.resolveTimeInForce(order);
    }

    @Test(expected = FixApplicationException.class)
    public void testStopOrderFOK() throws Exception {

        StopOrder order = new StopOrderImpl();
        order.setTif(TIF.FOK);
        this.requestFactory.resolveTimeInForce(order);
    }

}