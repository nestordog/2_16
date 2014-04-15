/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
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
import org.apache.maven.shared.invoker.SystemOutLogger;

/**
 * Utility class used to launch a simulation process in a separate JVM
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
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

    public void lunch() throws Exception {

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

        InvocationResult result = invoker.execute(request);
        if (result.getExitCode() != 0) {
            if (result.getExecutionException() != null) {
                throw result.getExecutionException();
            } else {
                throw new RuntimeException("unknown error execting maven exec:java");
            }
        }
    }
}
