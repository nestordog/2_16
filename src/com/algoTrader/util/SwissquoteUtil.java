package com.algoTrader.util;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.algoTrader.entity.Security;

public class SwissquoteUtil {


    private static Logger logger = MyLogger.getLogger(SwissquoteUtil.class.getName());

    private static String tickUrl = "http://www.swissquote.ch/sq_mi/public/market/Detail.action?s=";

    public static Document getSecurityDocument(Security security) throws IOException, HttpException {

        GetMethod get = new GetMethod(tickUrl + security.getIsin() + "_" + security.getMarket() + "_" + security.getCurrency());

        HttpClient standardClient = HttpClientUtil.getSwissquotePremiumClient();
        int status = standardClient.executeMethod(get);

        if (status == HttpStatus.SC_NOT_FOUND) {
            logger.warn("invalid security: " + security.getIsin());
            return null;
        }else if (status != HttpStatus.SC_OK) {
            logger.error("could not retrieve security: " + security.getIsin() + ", status: " + get.getStatusLine());
            return null;
        }

        // parse the content using Tidy
        Document document = TidyUtil.parse(get.getResponseBodyAsStream());

        get.releaseConnection();

        // save the file
        XmlUtil.saveDocumentToFile(document, security.getIsin() + ".xml", "results/option/");

        return document;
    }

    public static String getValue(Node document, String expression) throws TransformerException {

        Node node = XPathAPI.selectSingleNode(document, expression);
        if (node == null || node.getFirstChild() == null) return null;

        return node.getFirstChild().getNodeValue();
    }

    public static int getNumber(String inputString) throws ParseException {

        if (inputString == null) return 0;
        if ("-".equals(inputString)) return 0;

        return NumberFormat.getNumberInstance().parse(inputString).intValue();
    }

    public static double getAmount(String inputString) throws ParseException {

        if (inputString.contains("-")) return 0;

        int index = inputString.indexOf(" ");
        if (index == -1) {
            index = inputString.length();
        }
        return NumberFormat.getNumberInstance().parse(inputString.substring(0,index)).doubleValue();
    }

    public  static Date getDate(String date) throws ParseException {

        if (date.startsWith("null")) return null;
        return new SimpleDateFormat("dd-MM-yyyy kk:mm:ss").parse(date);
    }
}
