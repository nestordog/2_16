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
package ch.algotrader.simulator;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.security.Index;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.enumeration.AssetClass;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SimulatorTest {

    Simulator simulator;

    @Before
    public void before() {

        this.simulator = new Simulator();
    }

    @Test
    public void test() {

        String strategyName = "STRAT_1";
        Strategy strategy = Strategy.Factory.newInstance(strategyName, false, 1.0);

        Currency currency = Currency.USD;

        SecurityFamily securityFamily = SecurityFamily.Factory.newInstance();
        securityFamily.setName("SECURITY_FAMILY_1");
        securityFamily.setCurrency(currency);
        securityFamily.setContractSize(1);
        securityFamily.setExecutionCommission(new BigDecimal(1.0));
        securityFamily.setClearingCommission(new BigDecimal(2.0));
        securityFamily.setFee(new BigDecimal(3.0));

        Security underlying = Index.Factory.newInstance(securityFamily, AssetClass.EQUITY);
        underlying.setIsin("INDEX_1");
        underlying.setSymbol("INDEX_1");

        Security security = Stock.Factory.newInstance(securityFamily);
        security.setIsin("STOCK_1");
        security.setSymbol("STOCK_1");
        security.setUnderlying(underlying);

        Account account = Account.Factory.newInstance();
        account.setName("ACCOUNT_1");
        account.setBroker(Broker.IB);

        this.simulator.createCashBalance(strategyName, currency, new BigDecimal(1000));

        LimitOrder order1 = LimitOrder.Factory.newInstance();
        order1.setSide(Side.BUY);
        order1.setQuantity(10);
        order1.setLimit(new BigDecimal(10.0));
        order1.setSecurity(underlying);
        order1.setStrategy(strategy);
        order1.setAccount(account);

        this.simulator.sendOrder(order1);

        Position position = this.simulator.findPositionByStrategyAndSecurity(strategyName, underlying);
        Assert.assertEquals(10, position.getQuantity());

        CashBalance cashBalance = this.simulator.findCashBalanceByStrategyAndCurrency(strategyName, currency);
        Assert.assertEquals(cashBalance.getAmount(), new BigDecimal(870));

        LimitOrder order2 = LimitOrder.Factory.newInstance();
        order2.setSide(Side.SELL);
        order2.setQuantity(10);
        order2.setLimit(new BigDecimal(20.0));
        order2.setSecurity(underlying);
        order2.setStrategy(strategy);
        order2.setAccount(account);

        this.simulator.sendOrder(order2);

        position = this.simulator.findPositionByStrategyAndSecurity(strategyName, underlying);
        Assert.assertEquals(0, position.getQuantity());
        Assert.assertEquals(40, position.getRealizedPL(), 0.01);

        cashBalance = this.simulator.findCashBalanceByStrategyAndCurrency(strategyName, currency);
        Assert.assertEquals(new BigDecimal(1040), cashBalance.getAmount());

        PortfolioValue portfolioValue = this.simulator.getPortfolioValue();
        Assert.assertEquals(1040, portfolioValue.getNetLiqValue().doubleValue(), 0.0);
    }
}
