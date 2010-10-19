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

            ServiceLocator.instance().getSimulationService().simulateByUnderlayings();
        } else if (args[0].equals("simulateByActualOrders")) {

            ServiceLocator.instance().getSimulationService().simulateByActualTransactions();
        } else if (args[0].equals("optimizeSingle")) {

            String[] parameters = new String[args.length -1];
            double[] mins = new double[args.length -1];
            double[] maxs = new double[args.length -1];
            double[] accuracies = new double[args.length -1];
            for (int i = 1; i < args.length; i++) {
                String[] params = args[i].split(":");
                parameters[i-1] = params[0];
                mins[i-1] = Double.valueOf(params[1]);
                maxs[i-1] = Double.valueOf(params[2]);
                accuracies[i-1] = Double.valueOf(params[3]);
            }

            ServiceLocator.instance().getSimulationService().optimizeSingles(parameters, mins, maxs, accuracies);
        } else if (args[0].equals("optimizeMulti")) {

            String[] parameters = new String[args.length - 1];
            double[] starts = new double[args.length - 1];
            for (int i = 1; i < args.length; i++) {

                String[] params = args[i].split(":");
                String parameter = params[0];
                double start = Double.valueOf(params[1]);
                parameters[i-1] = parameter;
                starts[i-1] = start;
            }

            ServiceLocator.instance().getSimulationService().optimizeMulti(parameters, starts);
        } else if (args[0].equals("optimizeLinear")) {

            for (int i = 1; i < args.length; i++) {
                String[] params = args[i].split(":");
                String parameter = params[0];
                double min = Double.parseDouble(params[1]);
                double max = Double.parseDouble(params[2]);
                double increment = Double.parseDouble(params[3]);

                ServiceLocator.instance().getSimulationService().optimizeLinear(parameter, min, max, increment);
            }
        } else {
            logger.info("please specify simulateByUnderlayings or simulateByActualOrders on the commandline");
        }
    }
}
