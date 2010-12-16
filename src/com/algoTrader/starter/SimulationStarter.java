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

        if (args[0].equals("simulateByUnderlayings")) {

            ServiceLocator.serverInstance().getSimulationService().simulateByUnderlayings();
        } else if (args[0].equals("simulateByActualOrders")) {

            ServiceLocator.serverInstance().getSimulationService().simulateByActualTransactions();
        } else if (args[0].equals("optimizeSingle")) {

            String strategyName = args[0];
            String[] parameters = new String[args.length - 2];
            double[] mins = new double[args.length - 2];
            double[] maxs = new double[args.length - 2];
            double[] accuracies = new double[args.length - 2];
            for (int i = 2; i < args.length; i++) {
                String[] params = args[i].split(":");
                parameters[i - 2] = params[0];
                mins[i - 2] = Double.valueOf(params[1]);
                maxs[i - 2] = Double.valueOf(params[2]);
                accuracies[i - 2] = Double.valueOf(params[3]);
            }

            ServiceLocator.serverInstance().getSimulationService().optimizeSingles(strategyName, parameters, mins, maxs, accuracies);

        } else if (args[0].equals("optimizeMulti")) {

            String strategyName = args[0];
            String[] parameters = new String[args.length - 2];
            double[] starts = new double[args.length - 2];
            for (int i = 2; i < args.length; i++) {

                String[] params = args[i].split(":");
                String parameter = params[0];
                double start = Double.valueOf(params[1]);
                parameters[i - 2] = parameter;
                starts[i - 2] = start;
            }

            ServiceLocator.serverInstance().getSimulationService().optimizeMulti(strategyName, parameters, starts);

        } else if (args[0].equals("optimizeLinear")) {

            String strategyName = args[0];
            for (int i = 2; i < args.length; i++) {
                String[] params = args[i].split(":");
                String parameter = params[0];
                double min = Double.parseDouble(params[1]);
                double max = Double.parseDouble(params[2]);
                double increment = Double.parseDouble(params[3]);

                ServiceLocator.serverInstance().getSimulationService().optimizeLinear(strategyName, parameter, min, max, increment);
            }
        } else {
            logger.info("please specify simulateByUnderlayings or simulateByActualOrders on the commandline");
        }
    }
}
