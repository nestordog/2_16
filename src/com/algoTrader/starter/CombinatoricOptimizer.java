package com.algoTrader.starter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.util.MathUtils;

import com.algoTrader.util.JavaLauncher;

public class CombinatoricOptimizer {

    @SuppressWarnings("rawtypes")
    private static final Class starterClass = SimulationStarter.class;
    private static final String commandName = "simulateByMultiParam";
    private static final String[] vmArgs = { "-Dsimulation=true", "-DdataSource.dataSet=1year" };
    private static final String dataSource = "-DdataSource.url=jdbc:mysql://127.0.0.1:3306/AlgoTrader";

    @SuppressWarnings("unchecked")
    public static void main(String[] params) {

        String strategyName = params[0];
        int workers = Integer.valueOf(params[1]);

        List<String> jobs = new ArrayList<String>();

        String[] args1 = params[2].split("\\:");
        String parameter1 = args1[0];
        double min1 = Double.parseDouble(args1[1]);
        double max1 = Double.parseDouble(args1[2]);
        double increment1 = Double.parseDouble(args1[3]);
        for (double value1 = min1; value1 <= max1; value1 = MathUtils.round(value1 + increment1, 3)) {

            String job1 = parameter1 + ":" + value1;

            String[] args2 = params[3].split("\\:");
            String parameter2 = args2[0];
            double min2 = Double.parseDouble(args2[1]);
            double max2 = Double.parseDouble(args2[2]);
            double increment2 = Double.parseDouble(args2[3]);
            for (double value2 = min2; value2 <= max2; value2 = MathUtils.round(value2 + increment2, 3)) {

                String job2 = "," + parameter2 + ":" + value2;

                String[] args3 = params[4].split("\\:");
                String parameter3 = args3[0];
                double min3 = Double.parseDouble(args3[1]);
                double max3 = Double.parseDouble(args3[2]);
                double increment3 = Double.parseDouble(args3[3]);
                for (double value3 = min3; value3 <= max3; value3 = MathUtils.round(value3 + increment3, 3)) {

                    String job3 = "," + parameter3 + ":" + value3;
                    String job = job1 + job2 + job3;
                    jobs.add(job);
                }
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
