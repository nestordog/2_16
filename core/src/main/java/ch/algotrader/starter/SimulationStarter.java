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
package ch.algotrader.starter;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.log4j.Logger;

import ch.algotrader.ServiceLocator;
import ch.algotrader.service.SimulationService;
import ch.algotrader.service.SimulationServiceImpl;

/**
 * Main Starter Class for running simulations.
 * <p><b>simulateWithCurrentParams</b>
 * <p>One Simulation run with the currently defined parameters.
 * <p>Example: {@code simulateWithCurrentParams}
 * <p><b>simulateBySingleParam</b>
 * <p>One Simulation run with a parameter set to the defined value. The example below will do one run with paramter a set to 0.8
 * <p>Example: {@code simulateBySingleParam a:0.8}
 * <p><b>simulateByMultiParam</b>
 * <p>One Simulation run with multiple parameters set to defined values. The example below will do one run with paramter a set to 0.8 and b set to 12.0
 * <p>Example: {@code simulateByMultiParam a:0.8,b:12.0}
 * <p><b>optimizeSingleParamLinear</b>
 * <p>Multiple Simulation runs by incrementing the value of one parameter within a defined interval. The example below will increment the value of parameter a starting at 0.1 to 0.9, incrementing by 0.1 for each run
 * <p>Example: {@code optimizeSingleParamLinear a:0.1:0.9:0.1}
 * <p><b>optimizeSingleParamByValues</b>
 * <p>Multiple Simulation runs by iterating the value of one parameter according to defined list. The example below will iterate the value of parameter a through the following list: 0.2, 0.8, 0.9 and 1.2
 * <p>Example: {@code optimizeSingleParamByValues a:0.2:0.8:0.9:1.2}
 * <p><b>optimizeSingleParam</b>
 * <p>Multiple Simulation runs by setting the value of one parameter within the defined range and trying to find the maximum ShareRatio. The optimizer being used is <code>UnivariateRealOptimizer</code>. The example below will set the value of parameter a between 0.1 and 1.0 (accuracy 0.01).
 * <p>Example: {@code optimizeSingleParam a:0.1:1.0:0.01}
 * <p><b>optimizeMultiParamLinear</b>
 * <p>Multiple Simulation runs by doing a matrix Optimization of 2 or 3 parameters by incrementing their values within a defined intervals. The example below will iterate through all possible combinations by incrementing the value of parameter a starting at 0.1 to 0.9 (increment: 0.1), and incrementing the value of parameter b starting at 10.0 to 100.0 (increment: 5.0)
 * <p>Example: {@code optimizeMultiParamLinear a:0.1:0.9:0.1 b:10.0:100.0:5.0}
 * <p><b>optimizeMultiParam</b>
 * <p>Multiple Simulation runs by adjusting the value of multiple parameters around their start values and trying to find the maximum ShareRatio. The example below will start the optimiziation by settting the value of parameter a to 85.0 and parameter b to 150.0
 * <p>Example: {@code optimizeMultiParam SMI a:85.0 b:150.0}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SimulationStarter {

    public static Logger logger = Logger.getLogger(SimulationServiceImpl.class.getName());

    public static void main(String[] args) throws ConvergenceException, FunctionEvaluationException {

        ServiceLocator.instance().init(ServiceLocator.SIMULATION_BEAN_REFERENCE_LOCATION);

        if (args[0].equals("simulateWithCurrentParams")) {

            ServiceLocator.instance().getService("simulationService", SimulationService.class).simulateWithCurrentParams();


        } else if (args[0].equals("simulateBySingleParam")) {

            for (int i = 1; i < args.length; i++) {
                String[] params = args[i].split(":");
                ServiceLocator.instance().getService("simulationService", SimulationService.class).simulateBySingleParam(params[0], params[1]);
            }

        } else if (args[0].equals("simulateByMultiParam")) {

            for (int i = 1; i < args.length; i++) {
                String[] touples = args[i].split(",");
                String[] parameters = new String[touples.length];
                String[] values = new String[touples.length];
                for (int j = 0; j < touples.length; j++) {
                    parameters[j] = touples[j].split(":")[0];
                    values[j] = touples[j].split(":")[1];
                }
                ServiceLocator.instance().getService("simulationService", SimulationService.class).simulateByMultiParam(parameters, values);
            }

        } else if (args[0].equals("optimizeSingleParamLinear")) {

            for (int i = 1; i < args.length; i++) {
                String[] params = args[i].split(":");
                String parameter = params[0];
                double min = Double.parseDouble(params[1]);
                double max = Double.parseDouble(params[2]);
                double increment = Double.parseDouble(params[3]);

                ServiceLocator.instance().getService("simulationService", SimulationService.class).optimizeSingleParamLinear(parameter, min, max, increment);

            }

        } else if (args[0].equals("optimizeSingleParamByValues")) {

            for (int i = 1; i < args.length; i++) {
                String[] params = args[i].split(":");
                String parameter = params[0];
                double[] values = new double[params.length - 1];
                for (int j = 1; j < params.length; j++) {
                    values[j-1] = Double.valueOf(params[j]);
                }

                ServiceLocator.instance().getService("simulationService", SimulationService.class).optimizeSingleParamByValues(parameter, values);

            }
        } else if (args[0].equals("optimizeSingleParam")) {

            String[] params = args[1].split(":");
            String parameter = params[0];
            double min = Double.valueOf(params[1]);
            double max = Double.valueOf(params[2]);
            double accuracy = Double.valueOf(params[3]);

            ServiceLocator.instance().getService("simulationService", SimulationService.class).optimizeSingleParam(parameter, min, max, accuracy);

        } else if (args[0].equals("optimizeMultiParamLinear")) {

            String[] parameters = new String[args.length - 1];
            double[] mins = new double[args.length - 1];
            double[] maxs = new double[args.length - 1];
            double[] increments = new double[args.length - 1];
            for (int i = 1; i < args.length; i++) {

                String[] params = args[i].split(":");
                String parameter = params[0];
                double min = Double.valueOf(params[1]);
                double max = Double.valueOf(params[2]);
                double increment = Double.valueOf(params[3]);

                parameters[i - 1] = parameter;
                mins[i - 1] = min;
                maxs[i - 1] = max;
                increments[i - 1] = increment;
            }

            ServiceLocator.instance().getService("simulationService", SimulationService.class).optimizeMultiParamLinear(parameters, mins, maxs, increments);

        } else if (args[0].equals("optimizeMultiParam")) {

            String[] parameters = new String[args.length - 1];
            double[] starts = new double[args.length - 1];
            for (int i = 1; i < args.length; i++) {

                String[] params = args[i].split(":");
                String parameter = params[0];
                double start = Double.valueOf(params[1]);
                parameters[i - 1] = parameter;
                starts[i - 1] = start;
            }

            ServiceLocator.instance().getService("simulationService", SimulationService.class).optimizeMultiParam(parameters, starts);

        } else {
            logger.info("invalid command " + args[0]);
            return;
        }

        ServiceLocator.instance().shutdown();
    }
}
