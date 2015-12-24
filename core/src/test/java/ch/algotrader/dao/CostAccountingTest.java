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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
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

    @Test
    public void test() throws ParseException {

        PositionTracker positionTracker = PositionTrackerImpl.INSTANCE;

        Strategy strategy = new StrategyImpl();

        SecurityFamily family = new SecurityFamilyImpl();
        family.setContractSize(10.0);
        family.setScale(3);

        Security security = new StockImpl();
        security.setSecurityFamily(family);

        List<Transaction> transactions = new ArrayList<>();

        Transaction transaction;

        transaction = Transaction.Factory.newInstance(UUID.randomUUID().toString(), DateTimeLegacy.parseAsTimeGMT("08:00:00"), 10, new BigDecimal("100.000"), Currency.USD, TransactionType.BUY, strategy);
        transaction.setId(1);
        transaction.setSecurity(security);
        transaction.setExecutionCommission(new BigDecimal("10.0"));
        transactions.add(transaction);

        Position position = positionTracker.processFirstTransaction(transaction);

        assertPosition(position, "100.000", 10, "10010.00", "100.100", "10000.00", "-10.00", "0.00");
        assertPosition(position, "105.000", 10, "10010.00", "100.100", "10500.00", "490.00", "0.00");

        transaction = Transaction.Factory.newInstance(UUID.randomUUID().toString(), DateTimeLegacy.parseAsTimeGMT("09:00:00"), 2, new BigDecimal("110.000"), Currency.USD, TransactionType.BUY, strategy);
        transaction.setId(2);
        transaction.setSecurity(security);
        transaction.setExecutionCommission(new BigDecimal("2.0"));
        transactions.add(transaction);

        positionTracker.processTransaction(position, transaction);

        assertPosition(position, "110.000", 12, "12212.00", "101.767", "13200.00", "988.00", "0.00");
        assertPosition(position, "115.000", 12, "12212.00", "101.767", "13800.00", "1588.00", "0.00");

        transaction = Transaction.Factory.newInstance(UUID.randomUUID().toString(), DateTimeLegacy.parseAsTimeGMT("10:00:00"), -12, new BigDecimal("120.000"), Currency.USD, TransactionType.SELL, strategy);
        transaction.setId(3);
        transaction.setSecurity(security);
        transaction.setExecutionCommission(new BigDecimal("12.0"));
        transactions.add(transaction);

        positionTracker.processTransaction(position, transaction);

        assertPosition(position, "120.000", 0, "0.00", null, "0.00", "0.00", "2176.00");
        assertPosition(position, "125.000", 0, "0.00", null, "0.00", "0.00", "2176.00");

        transaction = Transaction.Factory.newInstance(UUID.randomUUID().toString(), DateTimeLegacy.parseAsTimeGMT("11:00:00"), -20, new BigDecimal("130.000"), Currency.USD, TransactionType.SELL, strategy);
        transaction.setId(4);
        transaction.setSecurity(security);
        transaction.setExecutionCommission(new BigDecimal("20.0"));
        transactions.add(transaction);

        positionTracker.processTransaction(position, transaction);

        assertPosition(position, "130.000", -20, "-25980.00", "129.900", "-26000.00", "-20.00", "2176.00");
        assertPosition(position, "135.000", -20, "-25980.00", "129.900", "-27000.00", "-1020.00", "2176.00");

        transaction = Transaction.Factory.newInstance(UUID.randomUUID().toString(), DateTimeLegacy.parseAsTimeGMT("12:00:00"), 12, new BigDecimal("140.000"), Currency.USD, TransactionType.BUY, strategy);
        transaction.setId(5);
        transaction.setSecurity(security);
        transaction.setExecutionCommission(new BigDecimal("12.0"));
        transactions.add(transaction);

        positionTracker.processTransaction(position, transaction);

        assertPosition(position, "140.000", -8, "-10392.00", "129.900", "-11200.00", "-808.00", "952.00");
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
