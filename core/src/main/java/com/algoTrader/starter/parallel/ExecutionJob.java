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
package com.algoTrader.starter.parallel;

import java.util.Arrays;

import org.codehaus.plexus.util.cli.CommandLineException;

import com.algoTrader.starter.SimulationStarter;
import com.algoTrader.starter.parallel.CustomThreadFactory.CustomThread;

/**
 * Worker Class used in conjunction with {@link MavenLauncher} to start a simulation process in a separate JVM.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ExecutionJob implements Runnable {

    private static final Class<?> starterClass = SimulationStarter.class;

    private String[] progArgs;
    private String[] vmArgs;
    private String dataSource;

    public ExecutionJob(String[] progArgs, String[] vmArgs, String dataSource) {
        this.progArgs = progArgs;
        this.vmArgs = vmArgs;
        this.dataSource = dataSource;
    }

    @Override
    public void run() {
        CustomThread thread = (CustomThread) Thread.currentThread();
        int number = thread.getNumber();

        this.vmArgs = Arrays.copyOf(this.vmArgs, this.vmArgs.length + 1);
        this.vmArgs[this.vmArgs.length - 1] = (number == 0) ? this.dataSource : this.dataSource + number;

        try {
            MavenLauncher launcher = new MavenLauncher(starterClass, this.progArgs, this.vmArgs);
            launcher.lunch();
        } catch (CommandLineException e) {
            e.printStackTrace();
        }
    }
}
