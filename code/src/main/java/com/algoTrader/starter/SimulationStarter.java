package com.algoTrader.starter;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.SimulationServiceImpl;
import com.algoTrader.util.MyLogger;

public class SimulationStarter {

    public static Logger logger = MyLogger.getLogger(SimulationServiceImpl.class.getName());

    /**
     * example calls:
     *         simulateWithCurrentParams
     *         runByActualTransactions
     *
     *         simulateBySingleParam SMI putTrigger:0.8 putTrigger:0.9
     *        simulateByMultiParam SMI macdFast:85.0,macdSlow:150.0 macdFast:80.0,macdSlow:140.0
     *
     *        optimizeSingleParamLinear SMI flatRange:0.001:0.004:0.0005 putVolaPeriod:0.4:0.45:0.01
     *         optimizeSingleParam SMI putVolaPeriod:0.4:0.45:0.01
     *         optimizeMultiParamLinear SMI flatRange:0.001:0.004:0.0005 putVolaPeriod:0.4:0.45:0.01
     *         optimizeMultiParam SMI macdFast:85.0 macdSlow:150.0
     */
    public static void main(String[] args) throws ConvergenceException, FunctionEvaluationException {

        ServiceLocator.instance().init(ServiceLocator.SIMULATION_BEAN_REFERENCE_LOCATION);

        if (args[0].equals("simulateWithCurrentParams")) {

            ServiceLocator.instance().getSimulationService().simulateWithCurrentParams();

        } else if (args[0].equals("runByActualTransactions")) {

            ServiceLocator.instance().getSimulationService().runByActualTransactions();

        } else if (args[0].equals("simulateBySingleParam")) {

            String strategyName = args[1];
            for (int i = 2; i < args.length; i++) {
                String[] params = args[i].split(":");
                ServiceLocator.instance().getSimulationService().simulateBySingleParam(strategyName, params[0], params[1]);
            }

        } else if (args[0].equals("simulateByMultiParam")) {

            String strategyName = args[1];
            for (int i = 2; i < args.length; i++) {
                String[] touples = args[i].split(",");
                String[] parameters = new String[touples.length];
                String[] values = new String[touples.length];
                for (int j = 0; j < touples.length; j++) {
                    parameters[j] = touples[j].split(":")[0];
                    values[j] = touples[j].split(":")[1];
                }
                ServiceLocator.instance().getSimulationService().simulateByMultiParam(strategyName, parameters, values);
            }

        } else if (args[0].equals("optimizeSingleParamLinear")) {

            String strategyName = args[1];
            for (int i = 2; i < args.length; i++) {
                String[] params = args[i].split(":");
                String parameter = params[0];
                double min = Double.parseDouble(params[1]);
                double max = Double.parseDouble(params[2]);
                double increment = Double.parseDouble(params[3]);

                ServiceLocator.instance().getSimulationService().optimizeSingleParamLinear(strategyName, parameter, min, max, increment);

            }
        } else if (args[0].equals("optimizeSingleParam")) {

            String strategyName = args[1];

            String[] params = args[2].split(":");
            String parameter = params[0];
            double min = Double.valueOf(params[1]);
            double max = Double.valueOf(params[2]);
            double accuracy = Double.valueOf(params[3]);

            ServiceLocator.instance().getSimulationService().optimizeSingleParam(strategyName, parameter, min, max, accuracy);

        } else if (args[0].equals("optimizeMultiParamLinear")) {

            String strategyName = args[1];
            String[] parameters = new String[args.length - 2];
            double[] mins = new double[args.length - 2];
            double[] maxs = new double[args.length - 2];
            double[] increments = new double[args.length - 2];
            for (int i = 2; i < args.length; i++) {

                String[] params = args[i].split(":");
                String parameter = params[0];
                double min = Double.valueOf(params[1]);
                double max = Double.valueOf(params[2]);
                double increment = Double.valueOf(params[3]);

                parameters[i - 2] = parameter;
                mins[i - 2] = min;
                maxs[i - 2] = max;
                increments[i - 2] = increment;
            }

            ServiceLocator.instance().getSimulationService().optimizeMultiParamLinear(strategyName, parameters, mins, maxs, increments);

        } else if (args[0].equals("optimizeMultiParam")) {

            String strategyName = args[1];
            String[] parameters = new String[args.length - 2];
            double[] starts = new double[args.length - 2];
            for (int i = 2; i < args.length; i++) {

                String[] params = args[i].split(":");
                String parameter = params[0];
                double start = Double.valueOf(params[1]);
                parameters[i - 2] = parameter;
                starts[i - 2] = start;
            }

            ServiceLocator.instance().getSimulationService().optimizeMultiParam(strategyName, parameters, starts);

        } else {
            logger.info("invalid command " + args[0]);
            return;
        }

        ServiceLocator.instance().shutdown();
    }
}
