package com.algoTrader.starter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math.util.MathUtils;

import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.ListPartitioner;

public class LinearOptimizer {

    private static final String commandName = "simulateByMultiParam";
    private static final String[] vmArgs = { "simulation=true", "roundDigits=" + ConfigurationUtil.getInt("simulation.roundDigits") };
    private static final String dataSource = "dataSource.url=jdbc:mysql://127.0.0.1:3306/AlgoTrader";
    private static final int roundDigits = ConfigurationUtil.getInt("simulation.roundDigits");
    private static final NumberFormat format = NumberFormat.getInstance();
    private static final int partitionSize = 10;

    static {
        format.setMinimumFractionDigits(roundDigits);
    }

    /**
     * example call: SMI 4 99:99to10 flatRange:0.001:0.004:0.0001 putVolaPeriod:0.4:0.45:0.01
     * strategy workers datasource1:datasource2 param1:start:end:increment param2:start:end:increment
     *
     * Note: algotrader-code % strategy needs to be deployed with the correct log-level to the local repo
     */
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

        System.out.println("executing " + jobs.size() + " jobs");

        ExecutorService service = Executors.newFixedThreadPool(workers, new CustomThreadFactory());

        // partition the jobs into chunks of maximum partitionSize
        List<List<String>> partitions = ListPartitioner.partition(jobs, partitionSize);

        // create ExecutionJobs
        for (List<String> partition : partitions) {

            List<String> progArgs = new ArrayList<String>();
            progArgs.add(commandName);
            progArgs.add(strategyName);
            progArgs.addAll(partition);

            ExecutionJob executionJob = new ExecutionJob(progArgs.toArray(new String[0]), vmArgs, dataSource);
            service.execute(executionJob);

            System.out.println("submitted job: " + progArgs);
        }
    }
}
