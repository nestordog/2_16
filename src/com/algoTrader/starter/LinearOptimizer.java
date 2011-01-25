package com.algoTrader.starter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.util.MathUtils;

import com.algoTrader.util.JavaLauncher;

public class LinearOptimizer {

    @SuppressWarnings("rawtypes")
    private static final Class starterClass = SimulationStarter.class;
    private static final String commandName = "simulateBySingleParam";
    private static final String[] vmArgs = { "-Dsimulation=true", "-DdataSource.dataSet=1year" };
    private static final String dataSource = "-DdataSource.url=jdbc:mysql://127.0.0.1:3306/AlgoTrader";

    @SuppressWarnings({ "unchecked" })
    public static void main(String[] params) {

        String strategyName = params[0];
        int workers = Integer.valueOf(params[1]);

        List<String> jobs = new ArrayList<String>();
        for (int i = 2; i < params.length; i++) {

            String[] args = params[i].split("\\:");
            String parameter = args[0];
            double min = Double.parseDouble(args[1]);
            double max = Double.parseDouble(args[2]);
            double increment = Double.parseDouble(args[3]);

            for (double value = min; value <= max; value = MathUtils.round(value + increment, 3)) {

                String job = parameter + ":" + value;
                jobs.add(job);
            }
        }

        List<String>[] progArgs = new List[workers];
        for (int j = 0; j < workers; j++) {
            progArgs[j] = new ArrayList<String>();
            progArgs[j].add(commandName);
            progArgs[j].add(strategyName);
        }

        for (int i = 0; i < jobs.size(); i++) {
            int worker = i % workers;
            progArgs[worker].add(jobs.get(i));
        }

        for (int i = 0; i < workers; i++) {

            System.out.println("starting worker: " + i + " with progArgs: " + progArgs[i]);

            String[] actualProgArgs = progArgs[i].toArray(new String[0]);

            String[] actualVmArgs = Arrays.copyOf(vmArgs, vmArgs.length + 1);
            actualVmArgs[vmArgs.length] = (i == 0) ? dataSource : dataSource + i;

            JavaLauncher.launch(starterClass, actualProgArgs, actualVmArgs);
        }
    }
}
