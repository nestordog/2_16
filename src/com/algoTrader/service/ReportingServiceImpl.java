package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.List;

import com.algoTrader.entity.PositionDao;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.TransactionDao;
import com.algoTrader.vo.BalanceVO;
import com.algoTrader.vo.PositionVO;
import com.algoTrader.vo.TransactionVO;

public class ReportingServiceImpl extends ReportingServiceBase {

    protected BigDecimal handleGetStrategyCashBalance(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getStrategyDao().getPortfolioCashBalance();
        } else {
            return strategy.getCashBalance();
        }
    }

    protected BigDecimal handleGetStrategySecuritiesCurrentValue(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getStrategyDao().getPortfolioSecuritiesCurrentValue();
        } else {
            return strategy.getSecuritiesCurrentValue();
        }

    }

    protected BigDecimal handleGetStrategyMaintenanceMargin(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getStrategyDao().getPortfolioMaintenanceMargin();
        } else {
            return strategy.getMaintenanceMargin();
        }
    }

    protected BigDecimal handleGetStrategyNetLiqValue(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getStrategyDao().getPortfolioNetLiqValue();
        } else {
            return strategy.getNetLiqValue();
        }
    }

    protected BigDecimal handleGetStrategyAvailableFunds(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getStrategyDao().getPortfolioAvailableFunds();
        } else {
            return strategy.getAvailableFunds();
        }
    }

    protected double handleGetStrategyAllocation(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        return strategy.getAllocation();
    }

    protected double handleGetStrategyLeverage(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getStrategyDao().getPortfolioLeverageDouble();
        } else {
            return strategy.getLeverage();
        }
    }

    @SuppressWarnings("unchecked")
    protected List<PositionVO> handleGetDataOpenPositions(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getPositionDao().findOpenPositions(PositionDao.TRANSFORM_POSITIONVO);
        } else {
            return getPositionDao().findOpenPositionsByStrategy(PositionDao.TRANSFORM_POSITIONVO, strategyName);
        }
    }

    @SuppressWarnings("unchecked")
    protected List<BalanceVO> handleGetDataBalances(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getStrategyDao().getPortfolioBalances();
        } else {
            return strategy.getBalances();
        }
    }

    @SuppressWarnings("unchecked")
    protected List<TransactionVO> handleGetDataTransactions(String strategyName) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy.isBase()) {
            return getTransactionDao().findLastNTransactions(TransactionDao.TRANSFORM_TRANSACTIONVO, 10);
        } else {
            return getTransactionDao().findLastNTransactionsByStrategy(TransactionDao.TRANSFORM_TRANSACTIONVO, 10, strategyName);
        }
    }
}
