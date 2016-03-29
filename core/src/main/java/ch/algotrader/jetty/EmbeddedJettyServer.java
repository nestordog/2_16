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

import java.awt.Desktop;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.service.InitializationPriority;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.util.collection.Pair;

@InitializationPriority(value = InitializingServiceType.CONNECTOR, priority = 0)
public class EmbeddedJettyServer implements InitializingServiceI {

    private final Logger LOGGER = LogManager.getLogger(EmbeddedJettyServer.class);

    private final int port;
    private final String requestLog;
    private final List<Pair<String, String>> simpleCreds;
    private final SSLContext serverSSLContext;
    private final ApplicationContext applicationContext;
    private final Server server;

    public EmbeddedJettyServer(
            final int port,
            final String requestLog,
            final List<Pair<String, String>> simpleCreds,
            final SSLContext serverSSLContext,
            final ApplicationContext applicationContext) {
        this.port = port;
        this.requestLog = requestLog;
        this.simpleCreds = simpleCreds;
        this.serverSSLContext = serverSSLContext;
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

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new DispatcherServlet(webAppContext)), "/*");

        if (this.simpleCreds != null && !this.simpleCreds.isEmpty()) {
            Constraint constraint = new Constraint();
            constraint.setName(Constraint.__BASIC_AUTH);
            constraint.setRoles(new String[] { "user" });
            constraint.setAuthenticate(true);

            ConstraintMapping constraintMapping = new ConstraintMapping();
            constraintMapping.setConstraint(constraint);
            constraintMapping.setPathSpec("/*");

            ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
            securityHandler.addConstraintMapping(constraintMapping);

            HashLoginService loginService = new HashLoginService();
            for (Pair<String, String> cred: this.simpleCreds) {
                loginService.putUser(cred.getFirst(), new Password(cred.getSecond()), new String[] {"user"});
            }
            securityHandler.setLoginService(loginService);
            securityHandler.setAuthenticator(new BasicAuthenticator());
            context.setSecurityHandler(securityHandler);
        }

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

        if (this.LOGGER.isInfoEnabled()) {
            this.LOGGER.info("HTTP connector on port {}", this.port);
        }

        SslContextFactory sslContextFactory = null;
        if (this.serverSSLContext != null) {
            sslContextFactory = new SslContextFactory();
            sslContextFactory.setSslContext(this.serverSSLContext);
            sslContextFactory.addExcludeProtocols("SSLv2", "SSLv3");
        }

        NetworkTrafficServerConnector channelConnector = new NetworkTrafficServerConnector(this.server, sslContextFactory);
        channelConnector.setPort(this.port);

        this.server.addConnector(channelConnector);
        if (!this.server.isRunning()) {
            this.server.start();
        }
        channelConnector.start();

        URL startPage = getClass().getResource("/html5/index.html");
        if (startPage != null && this.LOGGER.isInfoEnabled()) {
            String hostName;
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                hostName = localHost.getHostName();
            } catch (IOException ex) {
                hostName = "localhost";
            }

            URI uri = new URI(this.serverSSLContext == null ? "http" : "https", null, hostName, this.port, "/", null, null);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(uri);
            } else {
                this.LOGGER.info("Web UI available at {}", uri);
            }
        }
    }

    public void stop() throws Exception {

        this.server.stop();
    }

    public void awaitTermination() throws InterruptedException {

        this.server.join();
    }

}
