package com.algoTrader.starter;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.time.DateUtils;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.TidyUtil;
import com.algoTrader.util.XmlUtil;

public class IVolatilityDownloader {

    private static final String userAgent = ConfigurationUtil.getString("swissquote.loggedInUserAgent");

    private static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private static DateFormat fileFormat = new SimpleDateFormat("dd-MM-yyyy");

    public static void main(String[] args) throws HttpException, IOException, TransformerException, ParseException {

        HttpClient httpclient = new HttpClient();

        // login
        login(httpclient);

        Date startDate = dateFormat.parse("01/01/2005");
        Date endDate = dateFormat.parse("07/27/2011");

        Date date = startDate;
        while (date.compareTo(endDate) < 0) {

            download(httpclient, date);

            date = DateUtils.addMonths(date, 1);
        }

        logout(httpclient);
    }

    // @formatter:off
    private static void login(HttpClient httpclient) throws IOException, HttpException {

        // cookie settings http.protocol.cookie-policy
        httpclient.getParams().setParameter(HttpMethodParams.SINGLE_COOKIE_HEADER, new Boolean(true));
        httpclient.getParams().setParameter(HttpMethodParams.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

        // user agent
        httpclient.getParams().setParameter(HttpMethodParams.USER_AGENT, userAgent);
        PostMethod post = new PostMethod("https://www.ivolatility.com/login.j");
        post.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        NameValuePair[] date = {
                new NameValuePair("username", "andyflury"),
                new NameValuePair("password", "password"),
                new NameValuePair("step", "1"),
                new NameValuePair("login__is__sent", "1"),
                new NameValuePair("login_go", "")
            };

        post.setRequestBody(date);

        try {
            httpclient.executeMethod(post);

            Document document = TidyUtil.parse(post.getResponseBodyAsStream());
            XmlUtil.saveDocumentToFile(document, "login.xml", "results/ivol/");

        } finally {
            post.releaseConnection();
        }
    }

    // @formatter:off
    private static void download(HttpClient httpclient, Date date) throws IOException, HttpException, TransformerException {

        Calendar startCal = new GregorianCalendar();
        startCal.setTime(date);

        Calendar endCal = new GregorianCalendar();
        endCal.setTime(startCal.getTime());
        endCal.add(Calendar.MONTH, 1);
        endCal.add(Calendar.DAY_OF_YEAR, -1);

        // sessionUid
        NameValuePair[] sessionId = {};
        String sessionUid = post(httpclient, "sessionId", sessionId, "//input[@name='session_uid']/@value");

        // acccept
        NameValuePair[] accept = {
                new NameValuePair("start_date_day", String.valueOf(startCal.get(Calendar.DAY_OF_MONTH))),
                new NameValuePair("start_date_month", String.valueOf(startCal.get(Calendar.MONTH))),
                new NameValuePair("start_date_year", String.valueOf(startCal.get(Calendar.YEAR))),
                new NameValuePair("freq_type","0"),
                new NameValuePair("enable_daily_download","0"),
                new NameValuePair("send_zip_file_saved","0"),
                new NameValuePair("step","2"),
                new NameValuePair("template_to_change",""),
                new NameValuePair("session_uid", sessionUid),
                new NameValuePair("symbols","SPX:CBOE"),
                new NameValuePair("region_id",""),
                new NameValuePair("favorites",""),
                new NameValuePair("data_sets","5"),
                new NameValuePair("min_date","11/03/2000"),
                new NameValuePair("max_date","07/27/2011"),
                new NameValuePair("start_date_saved", dateFormat.format(startCal.getTime())),
                new NameValuePair("end_date_saved", dateFormat.format(endCal.getTime())),
                new NameValuePair("freq_type_saved","0"),
                new NameValuePair("stocks_id","9327"),
                new NameValuePair("is_sum_show","1"),
                new NameValuePair("stocks_select__is__sent","1"),
                new NameValuePair("end_date_day", String.valueOf(endCal.get(Calendar.DAY_OF_MONTH))),
                new NameValuePair("end_date_month", String.valueOf(endCal.get(Calendar.MONTH))),
                new NameValuePair("end_date_year", String.valueOf(endCal.get(Calendar.YEAR))),
                new NameValuePair("download_accept.x","47"),
                new NameValuePair("download_accept.y","8")
            };

        String orderId = post(httpclient, "accept", accept, "//input[@name='order_id']/@value");

        // file download
        if (orderId != null) {

            retrieve(httpclient, orderId, date);
        }
    }

    private static void logout(HttpClient httpclient) throws IOException, HttpException {
        //logout
        GetMethod logoutGet = new GetMethod("https://www.ivolatility.com/logoff.j");
        logoutGet.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        NameValuePair[] logoutData = { new NameValuePair("logoff", "1") };

        logoutGet.setQueryString(logoutData);

        try {
            httpclient.executeMethod(logoutGet);

            Document document = TidyUtil.parse(logoutGet.getResponseBodyAsStream());
            XmlUtil.saveDocumentToFile(document, "logout.xml", "results/ivol/");
        } finally {
            logoutGet.releaseConnection();
        }
    }

    private static void retrieve(HttpClient httpclient, String orderId, Date date) throws IOException, HttpException, FileNotFoundException {

        GetMethod fileGet = new GetMethod("http://www.ivolatility.com/data_download.csv");
        fileGet.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        NameValuePair[] fileData = { new NameValuePair("order_id", orderId) };

        fileGet.setQueryString(fileData);

        try {
            int status = httpclient.executeMethod(fileGet);

            if (status == HttpStatus.SC_OK) {

                BufferedInputStream inputStream = new BufferedInputStream(fileGet.getResponseBodyAsStream());
                FileOutputStream outputStream = new FileOutputStream("results/ivol/file-" + fileFormat.format(date) + ".csv");

                int input;
                while ((input = inputStream.read()) != -1) {
                    outputStream.write(input);
                }

                outputStream.close();
                inputStream.close();
            }
        } finally {
            fileGet.releaseConnection();
        }
    }

    private static String post(HttpClient httpclient, String name, NameValuePair[] acceptData, String xpath) throws IOException, HttpException,
            TransformerException {

        PostMethod post = new PostMethod("http://www.ivolatility.com/data_download.j");
        post.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        post.setRequestBody(acceptData);
        post.setRequestHeader("Referer", "http://www.ivolatility.com/data_download.j");

        String result = null;
        try {

            httpclient.executeMethod(post);

            Document document = TidyUtil.parse(post.getResponseBodyAsStream());
            XmlUtil.saveDocumentToFile(document, name + ".xml", "results/ivol/");

            if (xpath != null) {
                Node node = XPathAPI.selectSingleNode(document, xpath);
                if (node != null) {
                    result = node.getNodeValue();
                }
            }

        } finally {
            post.releaseConnection();
        }

        return result;
    }
}
