package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.algoTrader.entity.PositionDao;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.TransactionDao;
import com.algoTrader.vo.BalanceVO;
import com.algoTrader.vo.PositionVO;
import com.algoTrader.vo.TransactionVO;

public class ReportingServiceImpl extends ReportingServiceBase {

    private static final int maxTransactionCount = 20;

    @Override
    protected BigDecimal handleGetStrategyCashBalance(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getStrategyDao().getPortfolioCashBalance();
        } else {
            return strategy.getCashBalance();
        }
    }

    @Override
    protected BigDecimal handleGetStrategySecuritiesCurrentValue(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getStrategyDao().getPortfolioSecuritiesCurrentValue();
        } else {
            return strategy.getSecuritiesCurrentValue();
        }
    }

    @Override
    protected BigDecimal handleGetStrategyMaintenanceMargin(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getStrategyDao().getPortfolioMaintenanceMargin();
        } else {
            return strategy.getMaintenanceMargin();
        }
    }

    @Override
    protected BigDecimal handleGetStrategyNetLiqValue(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getStrategyDao().getPortfolioNetLiqValue();
        } else {
            return strategy.getNetLiqValue();
        }
    }

    @Override
    protected BigDecimal handleGetStrategyAvailableFunds(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getStrategyDao().getPortfolioAvailableFunds();
        } else {
            return strategy.getAvailableFunds();
        }
    }

    @Override
    protected double handleGetStrategyAllocation(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        return strategy.getAllocation();
    }

    @Override
    protected double handleGetStrategyLeverage(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getStrategyDao().getPortfolioLeverage();
        } else {
            return strategy.getLeverage();
        }
    }

    @Override
    protected BigDecimal handleGetStrategyBenchmark(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        return strategy.getBenchmark();
    }

    @Override
    protected double handleGetStrategyPerformance(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getStrategyDao().getPortfolioPerformance();
        } else {
            return strategy.getPerformance();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<PositionVO> handleGetDataOpenPositions(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return (List<PositionVO>) getPositionDao().findOpenPositions(PositionDao.TRANSFORM_POSITIONVO);
        } else {
            return (List<PositionVO>) getPositionDao().findOpenPositionsByStrategy(PositionDao.TRANSFORM_POSITIONVO, strategyName);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<BalanceVO> handleGetDataBalances(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getStrategyDao().getPortfolioBalances();
        } else {
            return new ArrayList<BalanceVO>();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<TransactionVO> handleGetDataTransactions(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return (List<TransactionVO>) getTransactionDao().findTransactionsDesc(TransactionDao.TRANSFORM_TRANSACTIONVO, 0, maxTransactionCount);
        } else {
            return (List<TransactionVO>) getTransactionDao().findTransactionsByStrategyDesc(TransactionDao.TRANSFORM_TRANSACTIONVO, 0, maxTransactionCount,
                    strategyName);
        }
    }
}
