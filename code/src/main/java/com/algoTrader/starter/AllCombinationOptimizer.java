package com.algoTrader.starter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.util.MathUtils;

import com.algoTrader.util.AntLauncher;
import com.algoTrader.util.ConfigurationUtil;

public class AllCombinationOptimizer {

    @SuppressWarnings("rawtypes")
    private static final Class starterClass = SimulationStarter.class;
    private static final String commandName = "simulateByMultiParam";
    private static final String[] vmArgs = { "simulation=true" };
    private static final String dataSource = "dataSource.url=jdbc:mysql://127.0.0.1:3306/AlgoTrader";
    private static final int roundDigits = ConfigurationUtil.getBaseConfig().getInt("simulation.roundDigits");
    private static final NumberFormat format = NumberFormat.getInstance();

    static {
        format.setMinimumFractionDigits(roundDigits);
    }

    /**
     * example call: SMI 4 99:99to10 macdFast:5:100:5 macdSlow:5:100:5 macdSignal:5:100:5
     * strategy workers datasource1:datasource2 param1:start:end:increment param2:start:end:increment param3:start:end:increment

     * Note: algotrader-code needs to be deployed with the correct log-level and SimulationService.roundDigits to the local repo
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] params) {

        String strategyName = params[0];
        int workers = Integer.valueOf(params[1]);

        String[] dataSources = params[2].split("\\:");

        List<String> jobs = new ArrayList<String>();

        String[] args1 = params[3].split("\\:");
        String parameter1 = args1[0];
        double min1 = Double.parseDouble(args1[1]);
        double max1 = Double.parseDouble(args1[2]);
        double increment1 = Double.parseDouble(args1[3]);
        for (double value1 = min1; value1 <= max1; value1 += increment1) {

            String job1 = parameter1 + ":" + format.format(MathUtils.round(value1, roundDigits));

            String[] args2 = params[4].split("\\:");
            String parameter2 = args2[0];
            double min2 = Double.parseDouble(args2[1]);
            double max2 = Double.parseDouble(args2[2]);
            double increment2 = Double.parseDouble(args2[3]);
            for (double value2 = min2; value2 <= max2; value2 += increment2) {

                String job2 = "," + parameter2 + ":" + format.format(MathUtils.round(value2, roundDigits));

                String[] args3 = params[5].split("\\:");
                String parameter3 = args3[0];
                double min3 = Double.parseDouble(args3[1]);
                double max3 = Double.parseDouble(args3[2]);
                double increment3 = Double.parseDouble(args3[3]);
                for (double value3 = min3; value3 <= max3; value3 += increment3) {

                    String job3 = "," + parameter3 + ":" + format.format(MathUtils.round(value3, roundDigits));
                    for (String dataSource : dataSources) {
                        String job = "dataSource.dataSet:" + dataSource + "," + job1 + job2 + job3;
                        jobs.add(job);
                    }
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

            AntLauncher.launch(starterClass, actualProgArgs, actualVmArgs);
        }
    }
}
