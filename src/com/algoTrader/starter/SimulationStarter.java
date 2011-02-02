package com.algoTrader.starter;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.SimulationServiceImpl;
import com.algoTrader.util.MyLogger;

public class SimulationStarter {

    public static Logger logger = MyLogger.getLogger(SimulationServiceImpl.class.getName());

    public static void main(String[] args) throws ConvergenceException, FunctionEvaluationException {

        if (args[0].equals("simulateWithCurrentParams")) {

            ServiceLocator.serverInstance().getSimulationService().simulateWithCurrentParams();

        } else if (args[0].equals("runByActualTransactions")) {

            ServiceLocator.serverInstance().getSimulationService().runByActualTransactions();

        } else if (args[0].equals("simulateBySingleParam")) {

            String strategyName = args[1];
            for (int i = 2; i < args.length; i++) {
                String[] params = args[i].split(":");
                ServiceLocator.serverInstance().getSimulationService().simulateBySingleParam(strategyName, params[0], Double.valueOf(params[1]));
            }

        } else if (args[0].equals("simulateByMultiParam")) {

            String strategyName = args[1];
            for (int i = 2; i < args.length; i++) {
                String[] touples = args[i].split(",");
                String[] parameters = new String[touples.length];
                double[] values = new double[touples.length];
                for (int j = 0; j < touples.length; j++) {
                    parameters[j] = touples[j].split(":")[0];
                    values[j] = Double.valueOf(touples[j].split(":")[1]);
                }
                ServiceLocator.serverInstance().getSimulationService().simulateByMultiParam(strategyName, parameters, values);
            }

        } else if (args[0].equals("optimizeSingleParamLinear")) {

            String strategyName = args[1];
            for (int i = 2; i < args.length; i++) {
                String[] params = args[i].split(":");
                String parameter = params[0];
                double min = Double.parseDouble(params[1]);
                double max = Double.parseDouble(params[2]);
                double increment = Double.parseDouble(params[3]);

                ServiceLocator.serverInstance().getSimulationService().optimizeSingleParamLinear(strategyName, parameter, min, max, increment);

            }
        } else if (args[0].equals("optimizeSingleParam")) {

            String strategyName = args[1];

            String[] params = args[2].split(":");
            String parameter = params[0];
            double min = Double.valueOf(params[1]);
            double max = Double.valueOf(params[2]);
            double accuracy = Double.valueOf(params[3]);

            ServiceLocator.serverInstance().getSimulationService().optimizeSingleParam(strategyName, parameter, min, max, accuracy);

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

            ServiceLocator.serverInstance().getSimulationService().optimizeMultiParam(strategyName, parameters, starts);

        } else {
            logger.info("invalid command " + args[0]);
            return;
        }

        ServiceLocator.serverInstance().shutdown();
    }
}
