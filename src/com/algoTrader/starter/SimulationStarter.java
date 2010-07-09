package com.algoTrader.starter;

import java.math.BigDecimal;
import java.util.List;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.MonthlyPerformance;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.InterpolationVO;
import com.algoTrader.vo.MaxDrawDownVO;
import com.algoTrader.vo.PerformanceKeysVO;

public class SimulationStarter {

    public static void main(String[] args) {

        start();
    }

    @SuppressWarnings("unchecked")
    public static void start() {

        ServiceLocator.instance().getSimulationService().init();

        ServiceLocator.instance().getRuleService().activateAll();
        ServiceLocator.instance().getSimulationService().run();

        BigDecimal totalValue = ServiceLocator.instance().getManagementService().getAccountTotalValue();
        System.out.println("totalValue=" + totalValue);

        InterpolationVO interpolation = ServiceLocator.instance().getSimulationService().getInterpolation();

        if (interpolation != null) {
            System.out.print("a=" + RoundUtil.getBigDecimal(interpolation.getA()));
            System.out.print(" b=" + RoundUtil.getBigDecimal(interpolation.getB()));
            System.out.println(" r=" + RoundUtil.getBigDecimal(interpolation.getR()));
        }

        List<MonthlyPerformance> monthlyPerformances = ServiceLocator.instance().getSimulationService().getMonthlyPerformances();

        System.out.print("monthlyPerformance: ");
        double maxDrawDownM = 0d;
        double bestMonthlyPerformance = 0d;
        for (MonthlyPerformance monthlyPerformance : monthlyPerformances) {
            maxDrawDownM = Math.min(maxDrawDownM, monthlyPerformance.getValue());
            bestMonthlyPerformance = Math.max(bestMonthlyPerformance, monthlyPerformance.getValue());
            System.out.print(RoundUtil.getBigDecimal(monthlyPerformance.getValue() * 100) + "% " );
        }
        System.out.println();

        PerformanceKeysVO performanceKeys = ServiceLocator.instance().getSimulationService().getPerformanceKeys();
        MaxDrawDownVO maxDrawDownVO = ServiceLocator.instance().getSimulationService().getMaxDrawDown();

        if (performanceKeys != null && maxDrawDownVO != null) {
            System.out.print("n=" + RoundUtil.getBigDecimal(performanceKeys.getN()));
            System.out.print(" avgM=" + RoundUtil.getBigDecimal(performanceKeys.getAvgM() * 100) + "%");
            System.out.print(" stdM=" + RoundUtil.getBigDecimal(performanceKeys.getStdM() * 100) + "%");
            System.out.print(" avgY=" + RoundUtil.getBigDecimal(performanceKeys.getAvgY() * 100) + "%");
            System.out.print(" stdY=" + RoundUtil.getBigDecimal(performanceKeys.getStdY() * 100) + "%");
            System.out.println(" sharpRatio=" + RoundUtil.getBigDecimal(performanceKeys.getSharpRatio()));

            System.out.print("maxDrawDownM: " + RoundUtil.getBigDecimal(-maxDrawDownM * 100) + "%");
            System.out.print(" bestMonthlyPerformance: " + RoundUtil.getBigDecimal(bestMonthlyPerformance * 100) + "%");
            System.out.print(" maxDrawDown: " + RoundUtil.getBigDecimal(maxDrawDownVO.getAmount() * 100) + "%");
            System.out.print(" maxDrawDownPeriod: " + RoundUtil.getBigDecimal(maxDrawDownVO.getPeriod() / 86400000) + "days");
            System.out.println(" colmarRatio: " + RoundUtil.getBigDecimal(performanceKeys.getAvgY() / maxDrawDownVO.getAmount()));
        }
    }
}
