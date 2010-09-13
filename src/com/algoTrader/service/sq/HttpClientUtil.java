package com.algoTrader.service.sq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.w3c.dom.Document;

import com.algoTrader.util.LoginException;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.TidyUtil;
import com.algoTrader.util.XmlUtil;

public class HttpClientUtil {

    private static String premiumUserId = PropertiesUtil.getProperty("swissquote.premium.userId");
    private static String premiumPassword = PropertiesUtil.getProperty("swissquote.premium.password");
    private static String premiumHost = PropertiesUtil.getProperty("swissquote.premium.host");

    private static String tradeHost = PropertiesUtil.getProperty("swissquote.trade.host");
    private static String tradeLoginUrl = PropertiesUtil.getProperty("swissquote.trade.loginUrl");
    private static String tradePasswordUrl = PropertiesUtil.getProperty("swissquote.trade.passwordUrl");
    private static String tradeLevel3Url = PropertiesUtil.getProperty("swissquote.trade.level3Url");
    private static String tradeUserId = PropertiesUtil.getProperty("swissquote.trade.userId");
    private static String tradePassword = PropertiesUtil.getProperty("swissquote.trade.password");

    private static int workers = PropertiesUtil.getIntProperty("swissquote.http-workers");
    private static boolean retry = PropertiesUtil.getBooleanProperty("swissquote.http-retry");
    private static String standardUserAgent = PropertiesUtil.getProperty("swissquote.standardUserAgent");
    private static String loggedInUserAgent = PropertiesUtil.getProperty("swissquote.loggedInUserAgent");
    private static boolean useProxy = PropertiesUtil.getBooleanProperty("swissquote.useProxy");

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_kkmmss");

    private static HttpClient standardClient;
    private static HttpClient premiumClient;

    public static HttpClient getStandardClient() {

        if (standardClient != null) return standardClient;

        // allow the same number of connections as we have workers
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, workers);
        connectionManager.setParams(params);

        // init the client
        standardClient = new HttpClient(new MultiThreadedHttpConnectionManager());

        if (useProxy) {
            standardClient.getHostConfiguration().setProxy("localhost", 8082); // proxomitron
            standardClient.getState().setProxyCredentials(
                    new AuthScope("localhost", 8082),
                    new UsernamePasswordCredentials("","")
            );
        }

        // set the retry Handler
        if (retry) {
            standardClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(5, true));
        }

        // cookie settings http.protocol.cookie-policy
        standardClient.getParams().setParameter(HttpMethodParams.SINGLE_COOKIE_HEADER, new Boolean(true));
        standardClient.getParams().setParameter(HttpMethodParams.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

        // user agent
        standardClient.getParams().setParameter(HttpMethodParams.USER_AGENT, standardUserAgent);

        return standardClient;
    }

    public static HttpClient getSwissquotePremiumClient() {

        if (premiumClient != null) return premiumClient;

        premiumClient = getStandardClient();

        premiumClient.getParams().setParameter(HttpMethodParams.USER_AGENT, loggedInUserAgent);

        premiumClient.getParams().setAuthenticationPreemptive(true);
        premiumClient.getState().setCredentials(
                new AuthScope(premiumHost, 80),
                new UsernamePasswordCredentials(premiumUserId, premiumPassword)
        );

        return premiumClient;
    }

    public static HttpClient getSwissquoteTradeClient() throws Exception {

        HttpClient tradeClient = getStandardClient();
        tradeClient.getParams().setParameter(HttpMethodParams.USER_AGENT, loggedInUserAgent);

        // set the Basic-Auth credentials
        tradeClient.getParams().setAuthenticationPreemptive(true);
        tradeClient.getState().setCredentials(
                new AuthScope(tradeHost, 443, "Online Trading"),
                new UsernamePasswordCredentials(tradeUserId, tradePassword)
        );

        // login screen
        String loginPath = null;
        Document loginDocument = null;
        {
            GetMethod method = new GetMethod(tradeLoginUrl);

            try {
                int status = tradeClient.executeMethod(method);

                loginPath = method.getPath();
                loginDocument = TidyUtil.parse(method.getResponseBodyAsStream());

                XmlUtil.saveDocumentToFile(loginDocument, format.format(new Date()) + "_loginGet.xml", "results/login/");

                if (status != HttpStatus.SC_OK) {
                    throw new LoginException("error on login screen: " + method.getStatusLine());
                }

            } finally {
                method.releaseConnection();
            }
        }

        // password screen
        String passwordPath = null;
        Document passwordDocument = null;
        if (tradePasswordUrl.contains(loginPath)) {

            String redirectUrl;
            {
                PostMethod method = new PostMethod(tradePasswordUrl);
                method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
                NameValuePair[] loginData = {
                        new NameValuePair("language", "1"),
                        new NameValuePair("pr", "1"),
                        new NameValuePair("st", "1"),
                        new NameValuePair("passwd", tradePassword)
                    };

                method.setRequestBody(loginData);

                try {
                    int status = tradeClient.executeMethod(method);

                    if ((status != HttpStatus.SC_MOVED_TEMPORARILY) || !method.getResponseHeader("location").getValue().contains(tradeLevel3Url)) {
                        throw new LoginException("after password screen did not get redirect");
                    }

                    redirectUrl = method.getResponseHeader("location").getValue();
                } finally {
                    method.releaseConnection();
                }
            }

            {
                GetMethod method = new GetMethod(redirectUrl);
                try {
                    int status = tradeClient.executeMethod(method);

                    passwordPath = method.getPath();
                    passwordDocument = TidyUtil.parse(method.getResponseBodyAsStream());

                    XmlUtil.saveDocumentToFile(passwordDocument, format.format(new Date()) + "_passwordPost.xml", "results/login/");

                    if (status != HttpStatus.SC_OK) {
                        throw new LoginException("error on password screen: " + method.getStatusLine());
                    }
                } finally {
                    method.releaseConnection();
                }
            }
        }

        // level3 screen
        if (((loginPath != null) && tradeLevel3Url.contains(loginPath)) ||
                ((passwordPath != null) &&tradeLevel3Url.contains(passwordPath))) {

            Document document = tradeLevel3Url.contains(loginPath) ? loginDocument : passwordDocument;

            String rec = SqUtil.getValue(document, "//input[@name='rec']/@value");
            String rqu = SqUtil.getValue(document, "//input[@name='rqu']/@value");
            String rse = SqUtil.getValue(document, "//input[@name='rse']/@value");
            String level3Key = SqUtil.getValue(document, "//strong[2]");

            PostMethod method = new PostMethod(tradeLevel3Url);

            method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            NameValuePair[] loginData = {
                    new NameValuePair("rec", rec),
                    new NameValuePair("rqu", rqu),
                    new NameValuePair("rse", rse),
                    new NameValuePair("tpw", getLevel3Code(level3Key))
                };
            method.setRequestBody(loginData);

            try {
                int status = tradeClient.executeMethod(method);

                if ((status != HttpStatus.SC_MOVED_TEMPORARILY) || !method.getResponseHeader("location").getValue().contains(tradeLoginUrl)) {
                    throw new LoginException("after level-3 screen did not get redirect");
                }

            } finally {
                method.releaseConnection();
            }
        }

        return tradeClient;
    }

    private static String getLevel3Code(String level3Key) throws IOException {

        int h = level3Key.charAt(0)-97;
        int v = Integer.parseInt(level3Key.substring(1)) - 1;

        InputStream in = HttpClientUtil.class.getResourceAsStream("com/algoTrader/service/sq/level3.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String[][] level3Card = new String[10][10];

        for (int i = 0; i < level3Card.length; i++){
            level3Card[i] = br.readLine().split("\\s");
        }
        in.close();

        return level3Card[v][h];
    }
}
