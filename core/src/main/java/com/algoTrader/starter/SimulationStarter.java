package com.algoTrader.starter;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.SimulationService;
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

            ServiceLocator.instance().getService("simulationService", SimulationService.class).simulateWithCurrentParams();

        } else if (args[0].equals("runByActualTransactions")) {

            ServiceLocator.instance().getService("simulationService", SimulationService.class).runByActualTransactions();

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
