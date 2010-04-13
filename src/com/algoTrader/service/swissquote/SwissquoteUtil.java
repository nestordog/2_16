package com.algoTrader.service.swissquote;

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
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.algoTrader.entity.Security;
import com.algoTrader.util.TidyUtil;
import com.algoTrader.util.XmlUtil;

public class SwissquoteUtil {

    private static String tickUrl = "http://www.swissquote.ch/sq_mi/public/market/Detail.action?s=";

    public static Document getSecurityDocument(Security security) throws IOException, HttpException {

        GetMethod get = new GetMethod(tickUrl + security.getIsin() + "_" + security.getMarket() + "_" + security.getCurrency());

        HttpClient standardClient = HttpClientUtil.getSwissquotePremiumClient();
        int status = standardClient.executeMethod(get);

        if (status != HttpStatus.SC_OK) {
            throw new HttpException("could not retrieve security: " + security.getIsin() + ", status: " + get.getStatusLine());
        }

        // parse the content using Tidy
        Document document = TidyUtil.parse(get.getResponseBodyAsStream());

        get.releaseConnection();

        // save the file
        XmlUtil.saveDocumentToFile(document, security.getIsin() + ".xml", "results/security/");

        return document;
    }

    public static String getValue(Node document, String expression) throws TransformerException {

        Node node = XPathAPI.selectSingleNode(document, expression);

        if (node == null ) return null;

        if (node.getFirstChild() != null) return node.getFirstChild().getNodeValue();

        return node.getNodeValue();
    }

    public static int getInt(String inputString) throws ParseException {

        if (inputString == null) return 0;

        if ("-".equals(inputString)) return 0;

        return NumberFormat.getNumberInstance().parse(inputString).intValue();
    }

    public static double getDouble(String inputString) throws ParseException {

        if (inputString == null) return 0;

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
