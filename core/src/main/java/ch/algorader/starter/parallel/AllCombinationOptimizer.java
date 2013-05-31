/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algorader.starter.parallel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math.util.MathUtils;

import ch.algorader.util.ConfigurationUtil;
import ch.algorader.util.ListPartitioner;


/**
 * Starter Class for running several simulations in parallel
 * <p>
 * Usage: {@code strategy workers datasource1:datasource2 param1:start:end:increment param2:start:end:increment param3:start:end:increment}
 * <p>
 * Exmaple: {@code SMI 4 99:99to10 macdFast:5:100:5 macdSlow:5:100:5 macdSignal:5:100:5}
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class AllCombinationOptimizer {

    private static final String commandName = "simulateByMultiParam";
    private static final String[] vmArgs = { "simulation=true", "roundDigits=" + ConfigurationUtil.getInt("simulation.roundDigits") };
    private static final String dataSource = "dataSource.url=jdbc:mysql://127.0.0.1:3306/AlgoTrader";
    private static final int roundDigits = ConfigurationUtil.getInt("simulation.roundDigits");
    private static final NumberFormat format = NumberFormat.getInstance();
    private static final int partitionSize = 10;

    static {
        format.setMinimumFractionDigits(roundDigits);
    }

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

            if (params.length > 4) {
                String[] args2 = params[4].split("\\:");
                String parameter2 = args2[0];
                double min2 = Double.parseDouble(args2[1]);
                double max2 = Double.parseDouble(args2[2]);
                double increment2 = Double.parseDouble(args2[3]);
                for (double value2 = min2; value2 <= max2; value2 += increment2) {

                    String job2 = "," + parameter2 + ":" + format.format(MathUtils.round(value2, roundDigits));

                    if (params.length > 5) {
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
                    } else {
                        for (String dataSource : dataSources) {
                            String job = "dataSource.dataSet:" + dataSource + "," + job1 + job2;
                            jobs.add(job);
                        }
                    }
                }
            } else {
                for (String dataSource : dataSources) {
                    String job = "dataSource.dataSet:" + dataSource + "," + job1;
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
