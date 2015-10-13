/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.simulation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import ch.algotrader.accounting.PositionTrackerImpl;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.TransactionVO;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.Index;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.LimitOrderVO;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.enumeration.AssetClass;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Direction;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.service.MarketDataCache;
import ch.algotrader.vo.ClosePositionVO;
import ch.algotrader.vo.OpenPositionVO;
import ch.algotrader.vo.TradePerformanceVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SimulatorTest {

    //under test
    private Simulator simulator;
    private EventDispatcher eventDispatcher;

    @Before
    public void before() {

        final EngineManager engineManager = mock(EngineManager.class);

        this.eventDispatcher = mock(EventDispatcher.class);

        this.simulator = new Simulator(mock(MarketDataCache.class), PositionTrackerImpl.INSTANCE, this.eventDispatcher, engineManager);

        doReturn(new Date()).when(engineManager).getCurrentEPTime();
    }

    @Test
    public void test() {

        // setup reference data
        String strategyName = "STRAT_1";
        Strategy strategy = Strategy.Factory.newInstance(strategyName, false, 1.0);

        Currency currency = Currency.USD;

        SecurityFamily securityFamily = SecurityFamily.Factory.newInstance();
        securityFamily.setId(1);
        securityFamily.setName("SECURITY_FAMILY_1");
        securityFamily.setCurrency(currency);
        securityFamily.setContractSize(1);
        securityFamily.setExecutionCommission(new BigDecimal(1.0));
        securityFamily.setClearingCommission(new BigDecimal(2.0));
        securityFamily.setFee(new BigDecimal(3.0));

        Security underlying = Index.Factory.newInstance(securityFamily, AssetClass.EQUITY);
        underlying.setId(1);
        underlying.setIsin("INDEX_1");
        underlying.setSymbol("INDEX_1");

        Security security = Stock.Factory.newInstance(securityFamily);
        security.setId(2);
        security.setIsin("STOCK_1");
        security.setSymbol("STOCK_1");
        security.setUnderlying(underlying);

        Account account = Account.Factory.newInstance();
        account.setId(1);
        account.setName("ACCOUNT_1");
        account.setBroker(Broker.IB.name());

        Exchange exchange = Exchange.Factory.newInstance();
        exchange.setId(1);
        exchange.setName("NASDAQ");
        exchange.setTimeZone(TimeZone.getDefault().getDisplayName());

        // initial cash balance
        this.simulator.createCashBalance(strategyName, currency, new BigDecimal(1000));

        // place first order
        LimitOrder order1 = LimitOrder.Factory.newInstance();
        order1.setSide(Side.BUY);
        order1.setDateTime(new Date());
        order1.setQuantity(10);
        order1.setLimit(new BigDecimal(10.0));
        order1.setSecurity(underlying);
        order1.setStrategy(strategy);
        order1.setAccount(account);
        order1.setExchange(exchange);

        this.simulator.sendOrder(order1);

        // verify first order
        ArgumentCaptor<Object> argCaptor1 = ArgumentCaptor.forClass(Object.class);
        verify(this.eventDispatcher, Mockito.atLeastOnce()).sendEvent(any(String.class), argCaptor1.capture());

        List<Object> values1 = argCaptor1.getAllValues();
        Assert.assertEquals(5, values1.size());

        Assert.assertEquals(LimitOrderVO.class, values1.get(0).getClass());
        LimitOrderVO orderVO1 = (LimitOrderVO) values1.get(0);
        Assert.assertEquals(order1.convertToVO(), orderVO1);

        Assert.assertEquals(Fill.class, values1.get(1).getClass());
        Fill fill1 = (Fill) values1.get(1);
        Assert.assertEquals(order1.getQuantity(), fill1.getQuantity());
        Assert.assertEquals(order1.getLimit(), fill1.getPrice());
        Assert.assertEquals(order1, fill1.getOrder());

        Assert.assertEquals(OrderStatusVO.class, values1.get(2).getClass());
        OrderStatusVO orderStatus1 = (OrderStatusVO) values1.get(2);
        Assert.assertEquals(Status.EXECUTED, orderStatus1.getStatus());
        Assert.assertEquals(order1.getQuantity(), orderStatus1.getFilledQuantity());
        Assert.assertEquals(0, orderStatus1.getRemainingQuantity());
        Assert.assertEquals(order1.getLimit(), orderStatus1.getAvgPrice());

        Assert.assertEquals(OpenPositionVO.class, values1.get(3).getClass());
        OpenPositionVO openPositionVO1 = (OpenPositionVO) values1.get(3);
        Assert.assertEquals(Direction.LONG, openPositionVO1.getDirection());
        Assert.assertEquals(order1.getQuantity(), openPositionVO1.getQuantity());
        Assert.assertEquals(order1.getSecurity().getId(), openPositionVO1.getSecurityId());
        Assert.assertEquals(order1.getStrategy().getName(), openPositionVO1.getStrategy());

        Assert.assertEquals(TransactionVO.class, values1.get(4).getClass());
        TransactionVO transactionVO1 = (TransactionVO) values1.get(4);
        Assert.assertEquals(order1.getQuantity(), transactionVO1.getQuantity());
        Assert.assertEquals(order1.getLimit(), transactionVO1.getPrice());
        Assert.assertEquals(TransactionType.BUY, transactionVO1.getType());
        Assert.assertEquals(order1.getSecurity().getSecurityFamily().getCurrency(), transactionVO1.getCurrency());
        Assert.assertEquals(order1.getStrategy().getId(), transactionVO1.getStrategyId());
        Assert.assertEquals(order1.getAccount().getId(), transactionVO1.getAccountId());

        // verify positions and cashBalances
        Position position = this.simulator.findPositionByStrategyAndSecurity(strategyName, underlying);
        Assert.assertEquals(10, position.getQuantity());

        CashBalance cashBalance = this.simulator.findCashBalanceByStrategyAndCurrency(strategyName, currency);
        Assert.assertEquals(cashBalance.getAmount(), new BigDecimal(870));

        reset(this.eventDispatcher);

        // send second order
        LimitOrder order2 = LimitOrder.Factory.newInstance();
        order2.setDateTime(new Date());
        order2.setSide(Side.SELL);
        order2.setQuantity(10);
        order2.setLimit(new BigDecimal(20.0));
        order2.setSecurity(underlying);
        order2.setStrategy(strategy);
        order2.setAccount(account);
        order2.setExchange(exchange);

        this.simulator.sendOrder(order2);

        // verify second order
        ArgumentCaptor<Object> argCaptor2 = ArgumentCaptor.forClass(Object.class);
        verify(this.eventDispatcher, Mockito.atLeastOnce()).sendEvent(any(String.class), argCaptor2.capture());

        List<Object> values2 = argCaptor2.getAllValues();
        Assert.assertEquals(6, values2.size());

        Assert.assertEquals(LimitOrderVO.class, values2.get(0).getClass());
        LimitOrderVO orderVO2 = (LimitOrderVO) values2.get(0);
        Assert.assertEquals(order2.convertToVO(), orderVO2);

        Assert.assertEquals(Fill.class, values2.get(1).getClass());
        Fill fill2 = (Fill) values2.get(1);
        Assert.assertEquals(order2.getQuantity(), fill2.getQuantity());
        Assert.assertEquals(order2.getLimit(), fill2.getPrice());
        Assert.assertEquals(order2, fill2.getOrder());

        Assert.assertEquals(OrderStatusVO.class, values2.get(2).getClass());
        OrderStatusVO orderStatus2 = (OrderStatusVO) values2.get(2);
        Assert.assertEquals(Status.EXECUTED, orderStatus2.getStatus());
        Assert.assertEquals(order2.getQuantity(), orderStatus2.getFilledQuantity());
        Assert.assertEquals(0, orderStatus2.getRemainingQuantity());
        Assert.assertEquals(order2.getLimit(), orderStatus2.getAvgPrice());

        Assert.assertEquals(ClosePositionVO.class, values2.get(3).getClass());
        ClosePositionVO closePositionVO2 = (ClosePositionVO) values2.get(3);
        Assert.assertEquals(Direction.LONG, closePositionVO2.getDirection());
        Assert.assertEquals(order2.getQuantity(), closePositionVO2.getQuantity());
        Assert.assertEquals(order2.getSecurity().getId(), closePositionVO2.getSecurityId());
        Assert.assertEquals(order2.getStrategy().getName(), closePositionVO2.getStrategy());

        Assert.assertEquals(TransactionVO.class, values2.get(4).getClass());
        TransactionVO transactionVO2 = (TransactionVO) values2.get(4);
        Assert.assertEquals(-order2.getQuantity(), transactionVO2.getQuantity());
        Assert.assertEquals(order2.getLimit(), transactionVO2.getPrice());
        Assert.assertEquals(TransactionType.SELL, transactionVO2.getType());
        Assert.assertEquals(order2.getSecurity().getSecurityFamily().getCurrency(), transactionVO2.getCurrency());
        Assert.assertEquals(order2.getStrategy().getId(), transactionVO2.getStrategyId());
        Assert.assertEquals(order2.getAccount().getId(), transactionVO2.getAccountId());

        Assert.assertEquals(TradePerformanceVO.class, values2.get(5).getClass());
        TradePerformanceVO tradePerformanceVO = (TradePerformanceVO) values2.get(5);
        Assert.assertEquals(40, tradePerformanceVO.getProfit(), 0.001);
        Assert.assertEquals(true, tradePerformanceVO.isWinning());

        // verify positions and cashBalances
        position = this.simulator.findPositionByStrategyAndSecurity(strategyName, underlying);
        Assert.assertEquals(0, position.getQuantity());
        Assert.assertEquals(40, position.getRealizedPL(), 0.01);

        cashBalance = this.simulator.findCashBalanceByStrategyAndCurrency(strategyName, currency);
        Assert.assertEquals(new BigDecimal(1040), cashBalance.getAmount());

        // verify portfolioValue
        PortfolioValue portfolioValue = this.simulator.getPortfolioValue();
        Assert.assertEquals(1040, portfolioValue.getNetLiqValue().doubleValue(), 0.0);
    }
}
