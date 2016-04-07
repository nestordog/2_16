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
package ch.algotrader.simulation;

import ch.algotrader.service.groups.StrategyGroup;
import ch.algotrader.vo.performance.OptimizationResultVO;
import ch.algotrader.vo.performance.SimulationResultVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface SimulationExecutor {

    /**
     * Starts a Simulation Run. The following steps are processed:
     * <ul>
     * <li><p>Create strategy entries in the database</p></li>
     * <li><p>The database is reset to its original state via the ResetService</p></li>
     * <li><p>All server Esper modules are deployed</p></li>
     * <li><p>The life cycle phase INIT is broadcasted to all strategies. During this phase potential initiation steps can be invoked.</p></li>
     * <li><p>All strategy {@code initModules} Modules are deployed </p></li>
     * <li><p>The life cycle phase {@code PREFEED} is broadcasted to all strategies. During this phase technical indicators can be initialized using historical data</p></li>
     * <li><p>All strategy {@code runModules} Modules are deployed </p></li>
     * <li><p>The life cycle phase {@code START} is broadcasted to all strategies. During this phase eventual actions like security subscriptions can be taken care of</p></li>
     * <li><p>At that time the actual simulation starts and market data events are starting to be sent into the Esper Engines</p></li>
     * <li><p>The life cycle phase {@code EXIT} is broadcasted to all strategies. During this phase eventual cleanup actions can be taken care of</p></li>
     * <li><p>At the end of each simulation run, metrics are printed to the console (if enabled)</p></li>
     * <li><p>All open positions are closed</p></li>
     * <li><p>An {@code EndOfSimulationVO} event is sent to all strategies</p></li>
     * <li><p>Esper Engines are destroyed</p></li>
     * <li><p>The second-level cache is cleared</p></li>
     * <li><p>All reports are closed</p></li>
     * <li><p>The Excel based back test report is created and statistics are displayed to the console</p></li>
     * </ol>
     */
    public SimulationResultVO runSimulation(final StrategyGroup strategyGroup);

    /**
     * Starts a Simulation Run with the currently defined parameters. See {@link
     * ch.algotrader.starter.SimulationStarter}
     */
    public SimulationResultVO simulateWithCurrentParams(StrategyGroup strategyGroup);

    /**
     * Starts a Simulation Run with a parameter set to the defined value. See {@link
     * ch.algotrader.starter.SimulationStarter}
     */
    public SimulationResultVO simulateBySingleParam(StrategyGroup strategyGroup, String parameter, String value);

    /**
     * Starts a Simulation Run with multiple parameters set to defined values. See {@link
     * ch.algotrader.starter.SimulationStarter}
     */
    public SimulationResultVO simulateByMultiParam(StrategyGroup strategyGroup, String[] parameters, String[] values);

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
     * {@code UnivariateRealOptimizer}. See {@link ch.algotrader.starter.SimulationStarter}
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
