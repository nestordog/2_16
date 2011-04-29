package com.algoTrader.starter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.util.MathUtils;

import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.MavenLauncher;

public class LinearOptimizer {

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
     * example call: SMI 4 99:99to10 flatRange:0.001:0.004:0.0001 putVolaPeriod:0.4:0.45:0.01
     * strategy workers datasource1:datasource2 param1:start:end:increment param2:start:end:increment
     *
     * Note: algotrader-code needs to be deployed with the correct log-level and SimulationService.roundDigits to the local repo
     */
    @SuppressWarnings({ "unchecked" })
    public static void main(String[] params) {

        String strategyName = params[0];
        int workers = Integer.valueOf(params[1]);

        String[] dataSources = params[2].split("\\:");

        List<String> jobs = new ArrayList<String>();
        for (int i = 3; i < params.length; i++) {

            String[] args = params[i].split("\\:");
            String parameter = args[0];
            double min = Double.parseDouble(args[1]);
            double max = Double.parseDouble(args[2]);
            double increment = Double.parseDouble(args[3]);

            for (double value = min; value <= max; value += increment) {

                for (String dataSource : dataSources) {
                    String job = "dataSource.dataSet:" + dataSource + "," + parameter + ":" + format.format(MathUtils.round(value, roundDigits));
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

            MavenLauncher.launch(starterClass, actualProgArgs, actualVmArgs);
        }
    }
}
