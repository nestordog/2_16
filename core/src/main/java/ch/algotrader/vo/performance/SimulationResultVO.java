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
package ch.algotrader.vo.performance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * A ValueObject representing the results of a Simulation Run
 */
public class SimulationResultVO implements Serializable {

    private static final long serialVersionUID = 7016214562454039508L;

    /**
     * The number of minutes the Simulation Run took.
     */
    private double mins;

    /**
     * The {@code dataSet} used for this Simulation Run.
     */
    private String dataSet;

    /**
     * The Net-Liq-Value of the Portfolio at the end of the Simulation Run.
     */
    private double netLiqValue;

    /**
     * The Strategy specific Simulation Results returned as a Map.
     */
    private Map strategyResults;

    /**
     * A ValueObject representing the performance during a particular period (Month or Year).
     */
    private Collection<PeriodPerformanceVO> monthlyPerformances;

    /**
     * A ValueObject representing the performance during a particular period (Month or Year).
     */
    private Collection<PeriodPerformanceVO> yearlyPerformances;

    /**
     * A ValueObject representing various performance figures related to a Simulation Run.
     */
    private PerformanceKeysVO performanceKeys;

    /**
     * A ValueObject representing the Maximum Draw Down during a Simulation Run.
     */
    private MaxDrawDownVO maxDrawDown;

    /**
     * A ValueObject representing a collection of Trades related to a Simulation Run. The collection
     * can represent either all winning, all loosing or all trades.
     */
    private TradesVO allTrades;

    /**
     * A ValueObject representing a collection of Trades related to a Simulation Run. The collection
     * can represent either all winning, all loosing or all trades.
     */
    private TradesVO winningTrades;

    /**
     * A ValueObject representing a collection of Trades related to a Simulation Run. The collection
     * can represent either all winning, all loosing or all trades.
     */
    private TradesVO loosingTrades;

    /**
     * Default Constructor
     */
    public SimulationResultVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor taking only required properties
     * @param minsIn double The number of minutes the Simulation Run took.
     * @param dataSetIn String The {@code dataSet} used for this Simulation Run.
     * @param netLiqValueIn double The Net-Liq-Value of the Portfolio at the end of the Simulation Run.
     * @param strategyResultsIn Map The Strategy specific Simulation Results returned as a Map.
     * @param performanceKeysIn PerformanceKeysVO A ValueObject representing various performance figures related to a Simulation Run.
     * @param maxDrawDownIn MaxDrawDownVO A ValueObject representing the Maximum Draw Down during a Simulation Run.
     * @param allTradesIn TradesVO A ValueObject representing a collection of Trades related to a Simulation Run. The collection can
     * represent either all winning, all loosing or all trades.
     * @param winningTradesIn TradesVO A ValueObject representing a collection of Trades related to a Simulation Run. The collection can
     * represent either all winning, all loosing or all trades.
     * @param loosingTradesIn TradesVO A ValueObject representing a collection of Trades related to a Simulation Run. The collection can
     * represent either all winning, all loosing or all trades.
     */
    public SimulationResultVO(final double minsIn, final String dataSetIn, final double netLiqValueIn, final Map strategyResultsIn, final PerformanceKeysVO performanceKeysIn,
            final MaxDrawDownVO maxDrawDownIn, final TradesVO allTradesIn, final TradesVO winningTradesIn, final TradesVO loosingTradesIn) {

        this.mins = minsIn;
        this.dataSet = dataSetIn;
        this.netLiqValue = netLiqValueIn;
        this.strategyResults = strategyResultsIn;
        this.performanceKeys = performanceKeysIn;
        this.maxDrawDown = maxDrawDownIn;
        this.allTrades = allTradesIn;
        this.winningTrades = winningTradesIn;
        this.loosingTrades = loosingTradesIn;
    }

    /**
     * Constructor with all properties
     * @param minsIn double
     * @param dataSetIn String
     * @param netLiqValueIn double
     * @param strategyResultsIn Map
     * @param monthlyPerformancesIn Collection<PeriodPerformanceVO>
     * @param yearlyPerformancesIn Collection<PeriodPerformanceVO>
     * @param performanceKeysIn PerformanceKeysVO
     * @param maxDrawDownIn MaxDrawDownVO
     * @param allTradesIn TradesVO
     * @param winningTradesIn TradesVO
     * @param loosingTradesIn TradesVO
     */
    public SimulationResultVO(final double minsIn, final String dataSetIn, final double netLiqValueIn, final Map strategyResultsIn, final Collection<PeriodPerformanceVO> monthlyPerformancesIn,
            final Collection<PeriodPerformanceVO> yearlyPerformancesIn, final PerformanceKeysVO performanceKeysIn, final MaxDrawDownVO maxDrawDownIn, final TradesVO allTradesIn,
            final TradesVO winningTradesIn, final TradesVO loosingTradesIn) {

        this.mins = minsIn;
        this.dataSet = dataSetIn;
        this.netLiqValue = netLiqValueIn;
        this.strategyResults = strategyResultsIn;
        this.monthlyPerformances = monthlyPerformancesIn;
        this.yearlyPerformances = yearlyPerformancesIn;
        this.performanceKeys = performanceKeysIn;
        this.maxDrawDown = maxDrawDownIn;
        this.allTrades = allTradesIn;
        this.winningTrades = winningTradesIn;
        this.loosingTrades = loosingTradesIn;
    }

    /**
     * Copies constructor from other SimulationResultVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public SimulationResultVO(final SimulationResultVO otherBean) {

        this.mins = otherBean.getMins();
        this.dataSet = otherBean.getDataSet();
        this.netLiqValue = otherBean.getNetLiqValue();
        this.strategyResults = otherBean.getStrategyResults();
        this.monthlyPerformances = otherBean.getMonthlyPerformances();
        this.yearlyPerformances = otherBean.getYearlyPerformances();
        this.performanceKeys = otherBean.getPerformanceKeys();
        this.maxDrawDown = otherBean.getMaxDrawDown();
        this.allTrades = otherBean.getAllTrades();
        this.winningTrades = otherBean.getWinningTrades();
        this.loosingTrades = otherBean.getLoosingTrades();
    }

    /**
     * The number of minutes the Simulation Run took.
     * @return mins double
     */
    public double getMins() {

        return this.mins;
    }

    /**
     * The number of minutes the Simulation Run took.
     * @param value double
     */
    public void setMins(final double value) {

        this.mins = value;
    }

    /**
     * The {@code dataSet} used for this Simulation Run.
     * @return dataSet String
     */
    public String getDataSet() {

        return this.dataSet;
    }

    /**
     * The {@code dataSet} used for this Simulation Run.
     * @param value String
     */
    public void setDataSet(final String value) {

        this.dataSet = value;
    }

    /**
     * The Net-Liq-Value of the Portfolio at the end of the Simulation Run.
     * @return netLiqValue double
     */
    public double getNetLiqValue() {

        return this.netLiqValue;
    }

    /**
     * The Net-Liq-Value of the Portfolio at the end of the Simulation Run.
     * @param value double
     */
    public void setNetLiqValue(final double value) {

        this.netLiqValue = value;
    }

    /**
     * The Strategy specific Simulation Results returned as a Map.
     * @return strategyResults Map
     */
    public Map getStrategyResults() {

        return this.strategyResults;
    }

    /**
     * The Strategy specific Simulation Results returned as a Map.
     * @param value Map
     */
    public void setStrategyResults(final Map value) {

        this.strategyResults = value;
    }

    /**
     * A ValueObject representing the performance during a particular period (Month or Year).
     * Get the monthlyPerformances Association
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the object.
     * @return this.monthlyPerformances Collection<PeriodPerformanceVO>
     */
    public Collection<PeriodPerformanceVO> getMonthlyPerformances() {

        if (this.monthlyPerformances == null) {

            this.monthlyPerformances = new ArrayList<>();
        }
        return this.monthlyPerformances;
    }

    /**
     * Sets the monthlyPerformances
     * @param value Collection<PeriodPerformanceVO>
     */
    public void setMonthlyPerformances(Collection<PeriodPerformanceVO> value) {

        this.monthlyPerformances = value;
    }

    /**
     * A ValueObject representing the performance during a particular period (Month or Year).
     * Get the yearlyPerformances Association
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the object.
     * @return this.yearlyPerformances Collection<PeriodPerformanceVO>
     */
    public Collection<PeriodPerformanceVO> getYearlyPerformances() {

        if (this.yearlyPerformances == null) {

            this.yearlyPerformances = new ArrayList<>();
        }
        return this.yearlyPerformances;
    }

    /**
     * Sets the yearlyPerformances
     * @param value Collection<PeriodPerformanceVO>
     */
    public void setYearlyPerformances(Collection<PeriodPerformanceVO> value) {

        this.yearlyPerformances = value;
    }

    /**
     * A ValueObject representing various performance figures related to a Simulation Run.
     * Get the performanceKeys Association
     * @return this.performanceKeys PerformanceKeysVO
     */
    public PerformanceKeysVO getPerformanceKeys() {

        return this.performanceKeys;
    }

    /**
     * Sets the performanceKeys
     * @param value PerformanceKeysVO
     */
    public void setPerformanceKeys(PerformanceKeysVO value) {

        this.performanceKeys = value;
    }

    /**
     * A ValueObject representing the Maximum Draw Down during a Simulation Run.
     * @return this.maxDrawDown MaxDrawDownVO
     */
    public MaxDrawDownVO getMaxDrawDown() {

        return this.maxDrawDown;
    }

    /**
     * Sets the maxDrawDown
     * @param value MaxDrawDownVO
     */
    public void setMaxDrawDown(MaxDrawDownVO value) {

        this.maxDrawDown = value;
    }

    /**
     * A ValueObject representing a collection of Trades related to a Simulation Run. The collection
     * can represent either all winning, all loosing or all trades.
     * @return this.allTrades TradesVO
     */
    public TradesVO getAllTrades() {

        return this.allTrades;
    }

    /**
     * Sets the allTrades
     * @param value TradesVO
     */
    public void setAllTrades(TradesVO value) {

        this.allTrades = value;
    }

    /**
     * A ValueObject representing a collection of Trades related to a Simulation Run. The collection
     * can represent either all winning, all loosing or all trades.
     * @return this.winningTrades TradesVO
     */
    public TradesVO getWinningTrades() {

        return this.winningTrades;
    }

    /**
     * Sets the winningTrades
     * @param value TradesVO
     */
    public void setWinningTrades(TradesVO value) {

        this.winningTrades = value;
    }

    /**
     * A ValueObject representing a collection of Trades related to a Simulation Run. The collection
     * can represent either all winning, all loosing or all trades.
     * @return this.loosingTrades TradesVO
     */
    public TradesVO getLoosingTrades() {

        return this.loosingTrades;
    }

    /**
     * Sets the loosingTrades
     * @param value TradesVO
     */
    public void setLoosingTrades(TradesVO value) {

        this.loosingTrades = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("SimulationResultVO [mins=");
        builder.append(mins);
        builder.append(", dataSet=");
        builder.append(dataSet);
        builder.append(", netLiqValue=");
        builder.append(netLiqValue);
        builder.append(", strategyResults=");
        builder.append(strategyResults);
        builder.append(", monthlyPerformances=");
        builder.append(monthlyPerformances);
        builder.append(", yearlyPerformances=");
        builder.append(yearlyPerformances);
        builder.append(", performanceKeys=");
        builder.append(performanceKeys);
        builder.append(", maxDrawDown=");
        builder.append(maxDrawDown);
        builder.append(", allTrades=");
        builder.append(allTrades);
        builder.append(", winningTrades=");
        builder.append(winningTrades);
        builder.append(", loosingTrades=");
        builder.append(loosingTrades);
        builder.append("]");

        return builder.toString();
    }

}
