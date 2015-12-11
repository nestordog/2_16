/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/

package ch.algotrader.jetty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.service.InitializationPriority;
import ch.algotrader.service.InitializingServiceI;

@InitializationPriority(value = InitializingServiceType.CONNECTOR, priority = 0)
public class EmbeddedJettyServer implements InitializingServiceI {

    private final Logger LOGGER = LogManager.getLogger(EmbeddedJettyServer.class);

    private final int port;
    private final String requestLog;
    private final ApplicationContext applicationContext;
    private final Server server;

    public EmbeddedJettyServer(final int port, final String requestLog, final ApplicationContext applicationContext) {
        this.port = port;
        this.requestLog = requestLog;
        this.applicationContext = applicationContext;
        this.server = new Server();
    }

    @Override
    public void init() throws Exception {

        start();
    }

    public void start() throws Exception {

        AnnotationConfigWebApplicationContext webAppContext = new AnnotationConfigWebApplicationContext();
        webAppContext.setParent(this.applicationContext);
        webAppContext.scan("ch.algotrader.wiring.rest");

        ServletHolder servletHolder = new ServletHolder(new DispatcherServlet(webAppContext));
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(servletHolder, "/*");

        if (this.requestLog != null) {
            RequestLogHandler requestLogHandler = new RequestLogHandler();
            NCSARequestLog requestLog = new NCSARequestLog(this.requestLog);
            requestLog.setAppend(true);
            requestLog.setExtended(false);
            requestLogHandler.setRequestLog(requestLog);
            HandlerCollection handlers = new HandlerCollection();
            handlers.setHandlers(new Handler[]{ context, requestLogHandler });
            this.server.setHandler(handlers);
        } else {
            this.server.setHandler(context);
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("HTTP connector on port {}", this.port);
        }
        NetworkTrafficServerConnector channelConnector = new NetworkTrafficServerConnector(this.server);
        channelConnector.setPort(this.port);
        this.server.addConnector(channelConnector);
        if (!this.server.isRunning()) {
            this.server.start();
        }
        channelConnector.start();

        URL startPage = getClass().getResource("/html5/index.html");
        if (startPage != null && LOGGER.isInfoEnabled()) {
            String hostName;
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                hostName = localHost.getHostName();
            } catch (IOException ex) {
                hostName = "localhost";
            }
            LOGGER.info("Web UI available at {}", new URI("http", null, hostName, this.port, "/", null, null));
        }
    }

    public void stop() throws Exception {

        this.server.stop();
    }

    public void awaitTermination() throws InterruptedException {

        this.server.join();
    }

}
