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
package ch.algotrader.util;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

import ch.algotrader.util.ConfigurationUtil;

/**
 * Provides HttpClient utility methods.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class HttpClientUtil {

    private static int workers = ConfigurationUtil.getInt("http.workers");
    private static boolean retry = ConfigurationUtil.getBoolean("http.retry");
    private static String userAgent = ConfigurationUtil.getString("http.userAgent");
    private static boolean useProxy = ConfigurationUtil.getBoolean("http.useProxy");

    private static HttpClient standardClient;

    /**
     * Returns a HttpClient configured with the following features:
     * <ul>
     * <li>MultiThreading</li>
     * <li>configured number of workders</li>
     * <li>proxy</li>
     * <li>retryHandler</li>
     * <li>cookyPolicy</li>
     * <li>userAgent</li>
     * </ul>
     */
    public static HttpClient getStandardClient() {

        if (standardClient != null) {
            return standardClient;
        }

        // allow the same number of connections as we have workers
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, workers);
        connectionManager.setParams(params);

        // init the client
        standardClient = new HttpClient(new MultiThreadedHttpConnectionManager());

        if (useProxy) {
            standardClient.getHostConfiguration().setProxy("localhost", 8082); // proxomitron
            standardClient.getState().setProxyCredentials(new AuthScope("localhost", 8082), new UsernamePasswordCredentials("", ""));
        }

        // set the retry Handler
        if (retry) {
            standardClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(5, true));
        }

        // cookie settings http.protocol.cookie-policy
        standardClient.getParams().setParameter(HttpMethodParams.SINGLE_COOKIE_HEADER, new Boolean(true));
        standardClient.getParams().setParameter(HttpMethodParams.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

        // user agent
        standardClient.getParams().setParameter(HttpMethodParams.USER_AGENT, userAgent);

        return standardClient;
    }
}
