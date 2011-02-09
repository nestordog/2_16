package com.algoTrader.service.theta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.StrategyUtil;
import com.algoTrader.vo.MacdVO;
import com.algoTrader.vo.StochasticVO;

public class ThetaManagementServiceImpl extends ThetaManagementServiceBase {

    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");
    private static String thetaStrategyName = ConfigurationUtil.getBaseConfig().getString("thetaStrategyName");

    @SuppressWarnings("unchecked")
    protected List<MacdVO> handleGetMACD() throws Exception {

        Strategy strategy = StrategyUtil.getStartedStrategy();
        if (simulation == true && thetaStrategyName != null) {
            return getRuleService().getAllEvents(thetaStrategyName, "KEEP_MACD_VO");
        } else if (StrategyImpl.THETA.equals(strategy.getGroup())) {
            return getRuleService().getAllEvents(strategy.getName(), "KEEP_MACD_VO");
        } else {
            return new ArrayList<MacdVO>();
        }
    }

    @SuppressWarnings("unchecked")
    protected List<StochasticVO> handleGetStochastic() throws Exception {

        Strategy strategy = StrategyUtil.getStartedStrategy();
        if (simulation == true && thetaStrategyName != null) {
            return getRuleService().getAllEvents(thetaStrategyName, "KEEP_STOCHASTIC_VO");
        } else if (StrategyImpl.THETA.equals(strategy.getGroup())) {
            return getRuleService().getAllEvents(strategy.getName(), "KEEP_STOCHASTIC_VO");
        } else {
            return new ArrayList<StochasticVO>();
        }
    }

    protected void handleGoLong() {

        Strategy strategy = StrategyUtil.getStartedStrategy();
        if (StrategyImpl.THETA.equals(strategy.getGroup())) {
            Security underlaying = strategy.getUnderlaying();
            BigDecimal underlayingSpot = underlaying.getLastTick().getCurrentValue();

            getThetaService().goLong(strategy.getName(), underlaying.getId(), underlayingSpot);
        } else {
            throw new ThetaManagementServiceException("BuySignal can only be called from THETA strategy group");
        }
    }

    protected void handleGoShort() {

        Strategy strategy = StrategyUtil.getStartedStrategy();
        if (StrategyImpl.THETA.equals(strategy.getGroup())) {
            Security underlaying = strategy.getUnderlaying();
            BigDecimal underlayingSpot = underlaying.getLastTick().getCurrentValue();

            getThetaService().goShort(strategy.getName(), underlaying.getId(), underlayingSpot);
        } else {
            throw new ThetaManagementServiceException("SellSignal can only be called from THETA strategy group");
        }
    }
}
