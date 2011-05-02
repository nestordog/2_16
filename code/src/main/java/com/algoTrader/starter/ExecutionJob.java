package com.algoTrader.starter;

import java.util.Arrays;

import org.codehaus.plexus.util.cli.CommandLineException;

import com.algoTrader.starter.CustomThreadFactory.CustomThread;
import com.algoTrader.util.MavenLauncher;

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
