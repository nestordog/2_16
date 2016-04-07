/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.dao;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.accounting.PositionTracker;
import ch.algotrader.accounting.PositionTrackerImpl;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.security.StockImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.util.DateTimeLegacy;

/**
 * Cost Accounting Test according to the example provided by Trading Technologies
 *
 * https://www.tradingtechnologies.com/help/fix-adapter-reference/pl-calculation-algorithm/understanding-pl-calculations/
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class CostAccountingTestTT {

    private PositionTracker positionTracker;
    private Strategy strategy;
    private Security security;
    private Position position;

    @Before
    public void setup() throws Exception {

        this.positionTracker = PositionTrackerImpl.INSTANCE;

        this.strategy = new StrategyImpl();

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setContractSize(1.0);
        family1.setScale(2);

        this.security = new StockImpl();
        this.security.setSecurityFamily(family1);

        this.position = this.positionTracker.processFirstTransaction(createTransaction("08:00:00", TransactionType.BUY, 12, "100.00", "0.0"));
        this.positionTracker.processTransaction(this.position, createTransaction("08:00:00", TransactionType.BUY, 17, "99.00", "0.0"));
        this.positionTracker.processTransaction(this.position, createTransaction("08:00:00", TransactionType.BUY, 3, "103.00", "0.0"));

        this.positionTracker.processTransaction(this.position, createTransaction("08:00:00", TransactionType.SELL, -9, "101.00", "0.0"));
        this.positionTracker.processTransaction(this.position, createTransaction("08:00:00", TransactionType.SELL, -4, "105.00", "0.0"));
    }

    @Test
    public void testScenario1() throws ParseException {

        assertPosition(this.position, "99.00", 19, "99.75", "-14.25", "32.25");

        this.positionTracker.processTransaction(this.position, createTransaction("09:00:00", TransactionType.BUY, 10, "100.00", "0.0"));
        assertPosition(this.position, 29, "99.84");
    }

    @Test
    public void testScenario2() throws ParseException {

        this.positionTracker.processTransaction(this.position, createTransaction("09:00:00", TransactionType.SELL, -12, "101.00", "0.0"));
        assertPosition(this.position, "99.00", 7, "99.75", "-5.25", "47.25");
    }

    @Test
    public void testScenario3() throws ParseException {

        this.positionTracker.processTransaction(this.position, createTransaction("09:00:00", TransactionType.SELL, -19, "101.00", "0.0"));
        assertPosition(this.position, "99.00", 0, null, "0.00", "56.00");
    }

    @Test
    public void testScenario4() throws ParseException {

        this.positionTracker.processTransaction(this.position, createTransaction("09:00:00", TransactionType.SELL, -22, "101.00", "0.0"));
        assertPosition(this.position, "99.00", -3, "101.00", "6.00", "56.00");
    }

    private Transaction createTransaction(String time, TransactionType type, long quantity, String price, String executionCommission) {

        Transaction transaction = Transaction.Factory.newInstance();
        transaction.setUuid(UUID.randomUUID().toString());
        transaction.setDateTime(DateTimeLegacy.parseAsTimeGMT(time));
        transaction.setQuantity(quantity);
        transaction.setPrice(new BigDecimal(price));
        transaction.setCurrency(Currency.USD);
        transaction.setType(type);
        transaction.setStrategy(this.strategy);
        transaction.setSecurity(this.security);
        transaction.setExecutionCommission(new BigDecimal(executionCommission));
        return transaction;
    }

    private void assertPosition(Position position, String price, long eQty, String eAvgPrice, String eUnrealizedPL, String eRealizedPL) {

        Tick tick = Tick.Factory.newInstance(null, null, null, new BigDecimal(price), null, null, null, 0, 0, 0);

        Assert.assertEquals(eQty, position.getQuantity());
        Assert.assertEquals(getBigDecimal(eAvgPrice), position.getAveragePrice());
        Assert.assertEquals(getBigDecimal(eUnrealizedPL), position.getUnrealizedPL(tick));
        Assert.assertEquals(getBigDecimal(eRealizedPL), position.getRealizedPL());
    }

    private void assertPosition(Position position, long eQty, String eAvgPrice) {

        Assert.assertEquals(eQty, position.getQuantity());
        Assert.assertEquals(getBigDecimal(eAvgPrice), position.getAveragePrice());
    }

    private Object getBigDecimal(String input) {
        return input == null ? input : new BigDecimal(input);
    }

}
