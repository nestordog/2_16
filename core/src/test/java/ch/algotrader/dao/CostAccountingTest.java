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
 * Cost Accounting Test.
 *
 * Compare with "IT\Cost Accounting\Position Properties.xlsx"
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class CostAccountingTest {

    private PositionTracker positionTracker;
    private Strategy strategy;
    private Security security;

    @Before
    public void setup() throws Exception {

        this.positionTracker = PositionTrackerImpl.INSTANCE;

        this.strategy = new StrategyImpl();

        SecurityFamily family = new SecurityFamilyImpl();
        family.setContractSize(10.0);
        family.setScale(3);

        this.security = new StockImpl();
        this.security.setSecurityFamily(family);
    }

    @Test
    public void test() throws ParseException {

        Position position = this.positionTracker.processFirstTransaction(createTransaction("08:00:00", TransactionType.BUY, 10, "100.000", "10.0"));

        assertPosition(position, "100.000", 10, "10010.00", "100.100", "10000.00", "-10.00", "0.00");
        assertPosition(position, "105.000", 10, "10010.00", "100.100", "10500.00", "490.00", "0.00");

        this.positionTracker.processTransaction(position, createTransaction("09:00:00", TransactionType.BUY, 2, "110.000", "2.0"));

        assertPosition(position, "110.000", 12, "12212.00", "101.767", "13200.00", "988.00", "0.00");
        assertPosition(position, "115.000", 12, "12212.00", "101.767", "13800.00", "1588.00", "0.00");

        this.positionTracker.processTransaction(position, createTransaction("10:00:00", TransactionType.SELL, -12, "120.000", "12.0"));

        assertPosition(position, "120.000", 0, "0.00", null, "0.00", "0.00", "2176.00");
        assertPosition(position, "125.000", 0, "0.00", null, "0.00", "0.00", "2176.00");

        this.positionTracker.processTransaction(position, createTransaction("11:00:00", TransactionType.SELL, -20, "130.000", "20.0"));

        assertPosition(position, "130.000", -20, "-25980.00", "129.900", "-26000.00", "-20.00", "2176.00");
        assertPosition(position, "135.000", -20, "-25980.00", "129.900", "-27000.00", "-1020.00", "2176.00");

        this.positionTracker.processTransaction(position, createTransaction("12:00:00", TransactionType.BUY, 12, "140.000", "12.0"));

        assertPosition(position, "140.000", -8, "-10392.00", "129.900", "-11200.00", "-808.00", "952.00");
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

    private void assertPosition(Position position, String price, long eQty, String eCost, String eAvgPrice, String eMarketValue, String eUnrealizedPL, String eRealizedPL) {

        Tick tick = Tick.Factory.newInstance(null, null, null, new BigDecimal(price), null, null, null, 0, 0, 0);

        Assert.assertEquals(eQty, position.getQuantity());
        Assert.assertEquals(getBigDecimal(eCost), position.getCost());
        Assert.assertEquals(getBigDecimal(eMarketValue), position.getMarketValue(tick));
        Assert.assertEquals(getBigDecimal(eUnrealizedPL), position.getUnrealizedPL(tick));
        Assert.assertEquals(getBigDecimal(eAvgPrice), position.getAveragePrice());
        Assert.assertEquals(getBigDecimal(eRealizedPL), position.getRealizedPL());
    }

    private Object getBigDecimal(String input) {
        return input == null ? input : new BigDecimal(input);
    }

}
