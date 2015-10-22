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
package ch.algotrader.dao.security;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.dao.AbstractDao;
import ch.algotrader.dao.NamedParam;
import ch.algotrader.dao.marketData.TickDao;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.QueryType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@Repository // Required for exception translation
public class ForexDaoImpl extends AbstractDao<Forex> implements ForexDao {

    private final TickDao tickDao;

    private final int intervalDays;

    public ForexDaoImpl(final SessionFactory sessionFactory, final TickDao tickDao, final int intervalDays) {

        super(ForexImpl.class, sessionFactory);

        Validate.notNull(tickDao);

        this.tickDao = tickDao;
        this.intervalDays = intervalDays;
    }

    @Override
    public double getRateDoubleByDate(Currency baseCurrency, Currency transactionCurrency, Date date) {

        Validate.notNull(baseCurrency, "Base currency is null");
        Validate.notNull(transactionCurrency, "Transaction currency is null");
        Validate.notNull(date, "Date is null");

        if (baseCurrency.equals(transactionCurrency)) {
            return 1.0;
        }

        Forex forex = getForex(baseCurrency, transactionCurrency);

        List<Tick> ticks = this.tickDao.findTicksBySecurityAndMaxDate(1, forex.getId(), date, this.intervalDays);
        if (ticks.isEmpty()) {
            throw new IllegalStateException("Cannot get exchangeRate for " + baseCurrency + "." + transactionCurrency + " because no last tick is available for date " + date);
        }

        Tick tick = ticks.get(0);
        if (forex.getBaseCurrency().equals(baseCurrency)) {
            // expected case
            return tick.getCurrentValueDouble();
        } else {
            // reverse case
            return 1.0 / tick.getCurrentValueDouble();
        }
    }

    @Override
    public Forex getForex(Currency baseCurrency, Currency transactionCurrency) {

        Validate.notNull(baseCurrency, "Base currency is null");
        Validate.notNull(transactionCurrency, "Transaction currency is null");

        Forex forex = findUniqueCaching("Forex.findByBaseAndTransactionCurrency", QueryType.BY_NAME, new NamedParam("baseCurrency", baseCurrency), new NamedParam("transactionCurrency", transactionCurrency));

        if (forex == null) {

            // reverse lookup
            forex = findUniqueCaching("Forex.findByBaseAndTransactionCurrency", QueryType.BY_NAME, new NamedParam("baseCurrency", transactionCurrency), new NamedParam("transactionCurrency", baseCurrency));

            if (forex == null) {
                throw new IllegalStateException("Forex does not exist: " + transactionCurrency + "." + baseCurrency);
            }
        }

        return forex;
    }

}
