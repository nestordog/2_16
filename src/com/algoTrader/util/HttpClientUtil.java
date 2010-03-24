package com.algoTrader.util;

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
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;

public class HttpClientUtil {

    private static String premiumUserId = PropertiesUtil.getProperty("swissquote.premium.userId");
    private static String premiumPassword = PropertiesUtil.getProperty("swissquote.premium.password");
    private static String premiumHost = PropertiesUtil.getProperty("swissquote.premium.loginUrl");

    private static String tradeHost = PropertiesUtil.getProperty("swissquote.trade.host");
    private static String tradeLoginUrl = PropertiesUtil.getProperty("swissquote.trade.loginUrl");
    private static String tradePasswordUrl = PropertiesUtil.getProperty("swissquote.trade.passwordUrl");
    private static String tradeLevel3Url = PropertiesUtil.getProperty("swissquote.trade.level3Url");
    private static String tradeUserId = PropertiesUtil.getProperty("swissquote.trade.userId");
    private static String tradePassword = PropertiesUtil.getProperty("swissquote.trade.password");

    private static String proxyHost = System.getProperty("http.proxyHost");
    private static String proxyPort = System.getProperty("http.proxyPort");
    private static boolean useProxy = (proxyHost != null) ? true : false;

    private static int workers = PropertiesUtil.getIntProperty("workers");
    private static String userAgent = PropertiesUtil.getProperty("userAgent");
    private static boolean retry = PropertiesUtil.getBooleanProperty("retry");

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_kkmmss");

    private static HttpClient standardClient;
    private static HttpClient loggedInClient;

    public static HttpClient getStandardClient() {

        if (standardClient != null) return standardClient;

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
        standardClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        if (useProxy) {
            standardClient.getHostConfiguration().setProxy(proxyHost, Integer.parseInt(proxyPort)); // proxomitron
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

    public static HttpClient getSwissquotePremiumClient() {

        if (loggedInClient != null) return loggedInClient;

        HttpClient loggedInClient = getStandardClient();

        loggedInClient.getState().setCredentials(
                new AuthScope(premiumHost, 80),
                new UsernamePasswordCredentials(premiumUserId, premiumPassword)
        );

        return loggedInClient;
    }

    public static HttpClient getSwissquoteTradeClient() throws Exception {

        HttpClient client = getStandardClient();

        // set the Basic-Auth credentials
        client.getState().setCredentials(
                new AuthScope(tradeHost, 443, "Online Trading"),
                new UsernamePasswordCredentials(tradeUserId, tradePassword)
        );

        // login screen
        String loginPath = null;
        Document loginDocument = null;
        {
            GetMethod method = new GetMethod(tradeLoginUrl);
            int status = client.executeMethod(method);

            if (status != HttpStatus.SC_OK) {
                throw new LoginException("error on login screen: " + method.getStatusLine());
            }

            loginPath = method.getPath();
            loginDocument = TidyUtil.parse(method.getResponseBodyAsStream());
            XmlUtil.saveDocumentToFile(loginDocument, format.format(new Date()) + "_loginGet.xml", "results/login/");

            method.releaseConnection();
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
                int status = client.executeMethod(method);

                if ((status != HttpStatus.SC_MOVED_TEMPORARILY) ||
                    !method.getResponseHeader("location").getValue().contains(tradeLevel3Url)) {
                        throw new LoginException("after password screen did not get redirect");
                }

                redirectUrl = method.getResponseHeader("location").getValue();
                method.releaseConnection();
            }

            {
                GetMethod method = new GetMethod(redirectUrl);
                int status = client.executeMethod(method);

                if (status != HttpStatus.SC_OK) {
                    throw new LoginException("error on password screen: " + method.getStatusLine());
                }

                passwordPath = method.getPath();
                passwordDocument = TidyUtil.parse(method.getResponseBodyAsStream());
                XmlUtil.saveDocumentToFile(passwordDocument, format.format(new Date()) + "_passwordPost.xml", "results/login/");

                method.releaseConnection();
            }
        }

        // level3 screen
        if (((loginPath != null) && tradeLevel3Url.contains(loginPath)) ||
                ((passwordPath != null) &&tradeLevel3Url.contains(passwordPath))) {

            Document document = tradeLevel3Url.contains(loginPath) ? loginDocument : passwordDocument;

            String rec = XPathAPI.selectSingleNode(document, "//input[@name='rec']/@value").getNodeValue();
            String rqu = XPathAPI.selectSingleNode(document, "//input[@name='rqu']/@value").getNodeValue();
            String rse = XPathAPI.selectSingleNode(document, "//input[@name='rse']/@value").getNodeValue();
            String level3Key = XPathAPI.selectSingleNode(document, "//strong[2]").getFirstChild().getNodeValue();

            PostMethod method = new PostMethod(tradeLevel3Url);

            method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            NameValuePair[] loginData = {
                    new NameValuePair("rec", rec),
                    new NameValuePair("rqu", rqu),
                    new NameValuePair("rse", rse),
                    new NameValuePair("tpw", getLevel3Code(level3Key))
                };
            method.setRequestBody(loginData);

            int status = client.executeMethod(method);

            if ((status != HttpStatus.SC_MOVED_TEMPORARILY) ||
                !method.getResponseHeader("location").getValue().contains(tradeLoginUrl)) {
                    throw new LoginException("after level-3 screen did not get redirect");
            }

            method.releaseConnection();
        }

        return client;
    }

    private static String getLevel3Code(String level3Key) throws IOException {

        int h = level3Key.charAt(0)-97;
        int v = Integer.parseInt(level3Key.substring(1)) - 1;

        InputStream in = HttpClientUtil.class.getResourceAsStream("/level3.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String[][] level3Card = new String[10][10];

        for (int i = 0; i < level3Card.length; i++){
            level3Card[i] = br.readLine().split("\\s");
        }
        in.close();

        return level3Card[v][h];
    }
}
