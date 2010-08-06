package com.algoTrader.starter;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.util.MyLogger;

public class SimulationStarter {

    public static Logger logger = MyLogger.getLogger(SimulationStarter.class.getName());

    public static void main(String[] args) throws ConvergenceException, FunctionEvaluationException {

        if (args[0].equals("simulateByUnderlayings")) {
            ServiceLocator.instance().getSimulationService().simulateByUnderlayings();
        } else if (args[0].equals("simulateByActualOrders")) {
            ServiceLocator.instance().getSimulationService().simulateByActualTransactions();
        } else if (args[0].equals("optimize")) {
            ServiceLocator.instance().getSimulationService().optimize(args[1], Double.valueOf(args[2]), Double.valueOf(args[3]), Double.valueOf(args[4]));
        } else {
            logger.info("please specify simulateByUnderlayings or simulateByActualOrders on the commandline");
        }
    }
}
