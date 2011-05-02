package com.algoTrader.util;

import java.io.File;
import java.util.Collections;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.InvokerLogger;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.SystemOutLogger;

public class MavenLauncher {

    private Class<?> clazz;
    private String[] args;
    private String[] vmArgs;

    public MavenLauncher(Class<?> clazz, String[] args, String[] vmArgs) {
        this.clazz = clazz;
        this.args = args;
        this.vmArgs = vmArgs;
    }

    public void lunch() {

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File("pom.xml"));
        request.setGoals(Collections.singletonList("exec:java"));
        request.setOffline(true);
        request.setDebug(false);
        Properties props = new Properties();
        props.put("exec.mainClass", this.clazz.getName());
        props.put("exec.args", StringUtils.join(this.args, " "));


        for (String vmArg : this.vmArgs) {
            String[] vmArgSplit = vmArg.split("=");
            props.put(vmArgSplit[0], vmArgSplit[1]);
        }

        request.setProperties(props);

        Invoker invoker = new DefaultInvoker();

        InvokerLogger logger = new SystemOutLogger();
        logger.setThreshold(InvokerLogger.ERROR);
        invoker.setLogger(logger);

        try {
            InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                throw new RuntimeException(result.getExecutionException());
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
    }
}
