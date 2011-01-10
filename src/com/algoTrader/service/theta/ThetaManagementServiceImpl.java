package com.algoTrader.service.theta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.util.StrategyUtil;
import com.algoTrader.vo.MacdVO;
import com.algoTrader.vo.StochasticVO;

public class ThetaManagementServiceImpl extends ThetaManagementServiceBase {

    @SuppressWarnings("unchecked")
    protected List<MacdVO> handleGetMACD() throws Exception {

        Strategy strategy = StrategyUtil.getStartedStrategy();
        if (StrategyImpl.THETA.equals(strategy.getGroup())) {
            return getRuleService().getAllEvents(strategy.getName(), "KEEP_MACD_VO");
        } else {
            return new ArrayList<MacdVO>();
        }
    }

    @SuppressWarnings("unchecked")
    protected List<StochasticVO> handleGetStochastic() throws Exception {

        Strategy strategy = StrategyUtil.getStartedStrategy();
        if (StrategyImpl.THETA.equals(strategy.getGroup())) {
            return getRuleService().getAllEvents(strategy.getName(), "KEEP_STOCHASTIC_VO");
        } else {
            return new ArrayList<StochasticVO>();
        }
    }

    protected void handleBuySignal() {

        Strategy strategy = StrategyUtil.getStartedStrategy();
        if (StrategyImpl.THETA.equals(strategy.getGroup())) {
            Security underlaying = strategy.getUnderlaying();
            BigDecimal underlayingSpot = underlaying.getLastTick().getCurrentValue();

            getThetaService().buySignal(strategy.getName(), underlaying.getId(), underlayingSpot);
        } else {
            throw new ThetaManagementServiceException("BuySignal can only be called from THETA strategy group");
        }
    }

    protected void handleSellSignal() {

        Strategy strategy = StrategyUtil.getStartedStrategy();
        if (StrategyImpl.THETA.equals(strategy.getGroup())) {
            Security underlaying = strategy.getUnderlaying();
            BigDecimal underlayingSpot = underlaying.getLastTick().getCurrentValue();

            getThetaService().sellSignal(strategy.getName(), underlaying.getId(), underlayingSpot);
        } else {
            throw new ThetaManagementServiceException("SellSignal can only be called from THETA strategy group");
        }
    }
}
