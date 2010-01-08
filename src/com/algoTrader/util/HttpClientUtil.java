package com.algoTrader.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;

public class HttpClientUtil {

    private static String ebaySiteId = PropertiesUtil.getProperty("ebay.siteId");
    private static String ebayLoginUrl = PropertiesUtil.getProperty("ebay.loginUrl");
    private static String swissquoteLoginUrl = PropertiesUtil.getProperty("swissquote.loginUrl");

    private static String proxyHost = System.getProperty("http.proxyHost");
    private static String proxyPort = System.getProperty("http.proxyPort");
    private static boolean useProxy = (proxyHost != null) ? true : false;

    private static int workers = Integer.parseInt(PropertiesUtil.getProperty("workers"));

    private static String userAgent = PropertiesUtil.getProperty("userAgent");

    private static Logger logger = MyLogger.getLogger(HttpClientUtil.class.getName());

    private static HttpClient _standardClient;
    private static Map _loggedInClients = new HashMap();

    public static HttpClient getStandardClient(boolean retry) {

        if (_standardClient != null) return _standardClient;

        // EasySSLProtocolSocketFactory if testing with proxomitron
        if (useProxy) {
            ProtocolSocketFactory factory = new EasySSLProtocolSocketFactory();
            Protocol protocol = new Protocol("https", factory, 443);
            Protocol.registerProtocol("https", protocol);
        }

        // allow the same number of connections as we have workers
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, workers);
        connectionManager.setParams(params);

        // init the client
        _standardClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        if (useProxy) {
            _standardClient.getHostConfiguration().setProxy(proxyHost, Integer.parseInt(proxyPort)); // proxomitron
        }

        // set the retry Handler
        if (retry) {
            _standardClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(5, true));
        }

        // cookie settings http.protocol.cookie-policy
        _standardClient.getParams().setParameter(HttpMethodParams.SINGLE_COOKIE_HEADER, new Boolean(true));
        _standardClient.getParams().setParameter(HttpMethodParams.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

        // user agent
        _standardClient.getParams().setParameter(HttpMethodParams.USER_AGENT, userAgent);

        return _standardClient;
    }

    public static HttpClient getEbayLoggedInClient(String userId, String password, boolean retry, boolean forceLogin) {

        if (!forceLogin && _loggedInClients.containsKey(userId))
            return (HttpClient)_loggedInClients.get(userId);

        HttpClient client = getStandardClient(retry);

        // get the login screen
        GetMethod loginScreenGet = new GetMethod(ebayLoginUrl + "?SignIn");
        try {
            int loginScreenStatus = client.executeMethod(loginScreenGet);

            if (loginScreenStatus != HttpStatus.SC_OK) {
                logger.error("could not get login screen: " + loginScreenGet.getStatusLine());
                return client; // no need to go any further
            }
        } catch (IOException e) {
            logger.error("could not get login screen", e);
            return client; // no need to go any further
        } finally {
            loginScreenGet.releaseConnection();
        }

        // log in
        PostMethod loginPost = new PostMethod(ebayLoginUrl);
        loginPost.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        NameValuePair[] loginData = {
                new NameValuePair("MfcISAPICommand", "SignInWelcome"),
                new NameValuePair("siteid", ebaySiteId),
                new NameValuePair("UsingSSL", "1"),
                new NameValuePair("userid", userId),
                new NameValuePair("pass", password)
            };
        loginPost.setRequestBody(loginData);
        try {
            int loginPostStatus = client.executeMethod(loginPost);

            if (loginPostStatus != HttpStatus.SC_OK) {
                logger.error("could not log in: " + loginPost.getStatusLine());
            }
        } catch (IOException e) {
            logger.error("could not log in ", e);
        } finally {
            loginPost.releaseConnection();
        }

        _loggedInClients.put(userId, client);

        return client;
    }

    public static HttpClient getSwissquotePremiumClient(String userId, String password, boolean retry) {

        if (_loggedInClients.containsKey(userId))
            return (HttpClient)_loggedInClients.get(userId);

        HttpClient client = getStandardClient(retry);

        client.getState().setCredentials(
                new AuthScope(swissquoteLoginUrl, 80),
                new UsernamePasswordCredentials(userId, password)
        );

        _loggedInClients.put(userId, client);

        return client;
    }
}
