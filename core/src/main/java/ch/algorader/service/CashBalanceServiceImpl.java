/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algorader.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import ch.algorader.util.MyLogger;
import ch.algorader.util.collection.BigDecimalMap;
import ch.algorader.util.collection.Pair;

import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.strategy.CashBalance;
import com.algoTrader.entity.strategy.Strategy;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.service.CashBalanceServiceBase;
import com.algoTrader.vo.CurrencyAmountVO;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CashBalanceServiceImpl extends CashBalanceServiceBase {

    private static Logger logger = MyLogger.getLogger(CashBalanceServiceImpl.class.getName());

    private @Value("#{T(ch.algorader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;
    private @Value("${misc.portfolioDigits}") int portfolioDigits;

    @Override
    protected void handleProcessTransaction(Transaction transaction) throws Exception {

        // process all currenyAmounts
        for (CurrencyAmountVO currencyAmount : transaction.getAttributions()) {
            processAmount(transaction.getStrategy().getName(), currencyAmount);
        }
    }

    @Override
    protected void handleProcessAmount(String strategyName, CurrencyAmountVO currencyAmount) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        CashBalance cashBalance = getCashBalanceDao().findByStrategyAndCurrencyLocked(strategy, currencyAmount.getCurrency());

        // create the cashBalance, if it does not exist yet
        if (cashBalance == null) {

            cashBalance = CashBalance.Factory.newInstance();

            cashBalance.setCurrency(currencyAmount.getCurrency());
            cashBalance.setAmount(currencyAmount.getAmount());

            // associate with strategy
            strategy.addCashBalances(cashBalance);

            getCashBalanceDao().create(cashBalance);

        } else {

            cashBalance.setAmount(cashBalance.getAmount().add(currencyAmount.getAmount()));
        }
    }

    @Override
    protected void handleResetCashBalances() throws Exception {

        // get all existing cashBalances
        Collection<CashBalance> existingCashBalances = getCashBalanceDao().loadAll();

        // sum all transactions
        Collection<Transaction> transactions = getTransactionDao().loadAll();
        BigDecimalMap<Pair<Strategy, Currency>> map = new BigDecimalMap<Pair<Strategy, Currency>>();
        for (Transaction transaction : transactions) {

            // process all currenyAmounts
            for (CurrencyAmountVO currencyAmount : transaction.getAttributions()) {
                map.increment(new Pair<Strategy, Currency>(transaction.getStrategy(), currencyAmount.getCurrency()), currencyAmount.getAmount());
            }
        }

        // create cash balances
        for (Map.Entry<Pair<Strategy, Currency>, BigDecimal> entry : map.entrySet()) {

            Strategy strategy = entry.getKey().getFirst();
            Currency currency = entry.getKey().getSecond();
            BigDecimal amount = entry.getValue().setScale(this.portfolioDigits, BigDecimal.ROUND_HALF_UP);

            CashBalance cashBalance = getCashBalanceDao().findByStrategyAndCurrencyLocked(strategy, currency);

            if (cashBalance != null) {

                existingCashBalances.remove(cashBalance);

                BigDecimal oldAmount = cashBalance.getAmount();
                if (oldAmount.doubleValue() != amount.doubleValue()) {

                    cashBalance.setAmount(amount);

                    logger.info("adjusted cashBalance: " + cashBalance + " from: " + oldAmount);
                }

            } else {

                cashBalance = CashBalance.Factory.newInstance();
                cashBalance.setCurrency(currency);
                cashBalance.setAmount(amount);

                // associate with strategy
                strategy.addCashBalances(cashBalance);

                getCashBalanceDao().create(cashBalance);

                logger.info("created cashBalance: " + cashBalance);
            }
        }

        // remove all obsolete cashBalances
        for (CashBalance cashBalance : existingCashBalances) {

            Strategy strategy = cashBalance.getStrategy();
            strategy.getCashBalances().remove(cashBalance);

            logger.info("removed cashBalance: " + cashBalance);
        }

        getCashBalanceDao().remove(existingCashBalances);
    }
}
