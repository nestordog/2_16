package com.algoTrader.util;

import java.io.File;
import java.util.Collections;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.InvokerLogger;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.SystemOutLogger;

public class MavenLauncher {

    public static void launch(final Class<?> clazz, final String[] args, final String[] vmArgs) {

        (new Thread(new Runnable() {
            public void run() {
                InvocationRequest request = new DefaultInvocationRequest();
                request.setPomFile(new File("pom.xml"));
                request.setGoals(Collections.singletonList("exec:java"));
                request.setOffline(true);
                request.setDebug(false);
                Properties props = new Properties();
                props.put("exec.mainClass", clazz.getName());
                props.put("exec.args", StringUtils.join(args, " "));


                for (String vmArg : vmArgs) {
                    String[] vmArgSplit = vmArg.split("=");
                    props.put(vmArgSplit[0], vmArgSplit[1]);
                }

                request.setProperties(props);

                Invoker invoker = new DefaultInvoker();

                InvokerLogger logger = new SystemOutLogger();
                logger.setThreshold(InvokerLogger.ERROR);
                invoker.setLogger(logger);

                try {
                    invoker.execute(request);
                } catch (MavenInvocationException e) {
                    e.printStackTrace();
                }
            }
        })).start();
    }
}
