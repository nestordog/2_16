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
package ch.algotrader.entity;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.enumeration.TransactionType;

/**
 * @author <a href="mailto:amasood@algotrader.ch">Ahmad Masood</a>
 *
 * @version $Revision$ $Date$
 */
public class TransactionTest extends EntityTest{

    private TransactionDao transactionDao;

    @Before
    public void before() {

        this.transactionDao = ServiceLocator.instance().getService("transactionDao", TransactionDao.class);
    }

    @Test
    public void testFindAllCashflows() {

        this.transactionDao.findAllCashflows();
    }

    @Test
    public void testFindAllTradesInclSecurity() {

        this.transactionDao.findAllTradesInclSecurity();
    }

    @Test
    public void testFindByDateTimePriceTypeAndDescription() {

        this.transactionDao.findByDateTimePriceTypeAndDescription(new Date(), new BigDecimal("0.0"), TransactionType.BUY, new String());
    }

    @Test
    public void testFindByDescriptionAndMaxDate() {

        this.transactionDao.findByDescriptionAndMaxDate(null, null);
    }

    @Test
    public void testFindByExtId() {

        this.transactionDao.findByExtId(null);
    }

    @Test
    public void testFindByMaxDate() {

        this.transactionDao.findByMaxDate(null);
    }

    @Test
    public void testFindByMaxDateExclFees() {

        this.transactionDao.findByMaxDateExclFees(null);
    }

    @Test
    public void testFindBySecurityAndDate() {

        this.transactionDao.findBySecurityAndDate(0, null);
    }

    @Test
    public void testFindByStrategy() {

        this.transactionDao.findByStrategy(null);
    }

    @Test
    public void testFindByStrategyAndMaxDate() {

        this.transactionDao.findByStrategyAndMaxDate(null, null);
    }

    @Test
    public void testFindCashFlowsByStrategyAndMinDate() {

        this.transactionDao.findCashflowsByStrategyAndMinDate(null, null);
    }

    @Test
    public void testFindTradesByMinDateAndMaxDate() {

        this.transactionDao.findTradesByMinDateAndMaxDate(null, null);
    }

    @Test
    public void testFindTransactionsAsc() {

        this.transactionDao.findTransactionsAsc();
    }

    @Test
    public void testFindTransactionsByStrategyDesc() {

        this.transactionDao.findTransactionsByStrategyDesc(null);
    }

    @Test
    public void testFindTransactionsDesc() {

        this.transactionDao.findTransactionsDesc();
    }

}
