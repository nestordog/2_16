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
package ch.algotrader.starter.parallel;

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
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * Utility class used to launch a simulation process in a separate JVM
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class MavenLauncher {

    private Class<?> clazz;
    private String[] args;
    private String[] vmArgs;

    public MavenLauncher(Class<?> clazz, String[] args, String[] vmArgs) {
        this.clazz = clazz;
        this.args = args;
        this.vmArgs = vmArgs;
    }

    public void lunch() throws CommandLineException {

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
                if (result.getExecutionException() != null) {
                    throw result.getExecutionException();
                } else {
                    throw new RuntimeException("unknown error execting maven exec:java");
                }
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
    }
}
