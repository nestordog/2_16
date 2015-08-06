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
package ch.algotrader.simulation;

import ch.algotrader.service.groups.StrategyGroup;
import ch.algotrader.vo.performance.OptimizationResultVO;
import ch.algotrader.vo.performance.SimulationResultVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface SimulationExecutor {

    /**
     * Starts a Simulation Run. The following steps are processed:
     * <ul>
     * <li>The database is reset to its original state via the {@link ch.algotrader.service.ResetService}</li>
     * <li>Esper Engines are initialized for the AlgoTrader Server as well as all strategies marked as autoActivate</li>
     * <li>All Esper Modules defined in column initModules and runModules of the table strategy are deployed</li>
     * <li>A Portfolio Rebalance is executed to distribute the initial CREDIT (of 1'000'000 USD) to Strategies according to their {@link ch.algotrader.entity.trade.Allocation Allocation}</li>
     * <li>Platform event dispatcher emits {@link ch.algotrader.enumeration.LifecyclePhase#INIT} event</li>
     * <li>Platform event dispatcher emits {@link ch.algotrader.enumeration.LifecyclePhase#PREFEED} event</li>
     * <li>The Market Data Feed is started</li>
     * <li>Platform event dispatcher emits {@link ch.algotrader.enumeration.LifecyclePhase#EXIT} event</li>
     * <li>At the end of each simulation run, metrics are printed to the console (if enabled)</li>
     * <li>All open positions are closed</li>
     * <li>General Performance Statistics as well as Strategy specific Performance Statistics (gathered through
     * {@link ch.algotrader.simulation.SimulationResultsProducer#getSimulationResults}) are printed to the console</li>
     * <li>Esper Engines are destroyed</li>
     * <li>The second-level cache is cleared</li>
     * <li>A Garbage Collection is performed</li>
     * </ul>
     */
    public SimulationResultVO runSimulation(final StrategyGroup strategyGroup);

    /**
     * Starts a Simulation Run with the currently defined parameters. See {@link
     * ch.algotrader.starter.SimulationStarter}
     */
    public void simulateWithCurrentParams(StrategyGroup strategyGroup);

    /**
     * Starts a Simulation Run with a parameter set to the defined value. See {@link
     * ch.algotrader.starter.SimulationStarter}
     */
    public void simulateBySingleParam(StrategyGroup strategyGroup, String parameter, String value);

    /**
     * Starts a Simulation Run with multiple parameters set to defined values. See {@link
     * ch.algotrader.starter.SimulationStarter}
     */
    public void simulateByMultiParam(StrategyGroup strategyGroup, String[] parameters, String[] values);

    /**
     * Starts multiple Simulation Runs by incrementing the value of one parameter within a defined
     * interval. See {@link ch.algotrader.starter.SimulationStarter}
     */
    public void optimizeSingleParamLinear(StrategyGroup strategyGroup, String parameter, double min, double max, double increment);

    /**
     * Starts multiple Simulation Runs by iterating the value of one parameter according to defined
     * list. See {@link ch.algotrader.starter.SimulationStarter}
     */
    public void optimizeSingleParamByValues(StrategyGroup strategyGroup, String parameter, double[] values);

    /**
     * Starts multiple Simulation Runs by setting the value of one parameter within the defined
     * range and trying to find the maximum ShareRatio. The optimizer being used is
     * <code>UnivariateRealOptimizer</code>. See {@link ch.algotrader.starter.SimulationStarter}
     */
    public OptimizationResultVO optimizeSingleParam(StrategyGroup strategyGroup, String parameter, double min, double max, double accuracy);

    /**
     * Starts multiple Simulation Runs by doing a matrix Optimization of 2 or 3 parameters by
     * incrementing their values within a defined intervals. See {@link
     * ch.algotrader.starter.SimulationStarter}
     */
    public void optimizeMultiParamLinear(StrategyGroup strategyGroup, String[] parameters, double[] mins, double[] maxs, double[] increments);

    /**
     * Starts multiple Simulation Runs by adjusting the value of multiple parameters around their
     * start values and trying to find the maximum ShareRatio. See {@link
     * ch.algotrader.starter.SimulationStarter}
     */
    public void optimizeMultiParam(StrategyGroup strategyGroup, String[] parameters, double[] starts);

}
