package com.algoTrader.util;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.algoTrader.ServiceLocator;

public class HttpClientUtil {

    private static int workers = ServiceLocator.instance().getConfiguration().getInt("http.workers");
    private static boolean retry = ServiceLocator.instance().getConfiguration().getBoolean("http.retry");
    private static String userAgent = ServiceLocator.instance().getConfiguration().getString("http.userAgent");
    private static boolean useProxy = ServiceLocator.instance().getConfiguration().getBoolean("http.useProxy");

    private static HttpClient standardClient;

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
