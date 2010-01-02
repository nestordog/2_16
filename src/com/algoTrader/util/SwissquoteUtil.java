package com.algoTrader.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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
import org.w3c.tidy.Tidy;

import com.algoTrader.entity.Security;

public class SwissquoteUtil {

    private static String userId = PropertiesUtil.getProperty("swissquote.userId");
    private static String password = PropertiesUtil.getProperty("swissquote.password");

    private static Logger logger = Logger.getLogger(SwissquoteUtil.class.getName());

    private static String tickUrl = "http://www.swissquote.ch/sq_mi/public/market/Detail.action?s=";

    public static Document getSecurityDocument(Security security) throws IOException, HttpException {

        GetMethod get = new GetMethod(tickUrl + security.getIsin() + "_" + security.getMarket() + "_" + security.getCurrency());

        String content;
        try {
            HttpClient standardClient = HttpClientUtil.getSwissquotePremiumClient(userId, password, true);
            int status = standardClient.executeMethod(get);

            if (status == HttpStatus.SC_NOT_FOUND) {
                logger.warn("invalid security: " + security.getIsin());
                return null;
            }else if (status != HttpStatus.SC_OK) {
                logger.error("could not retrieve security: " + security.getIsin() + ", status: " + get.getStatusLine());
                return null;
            }

            // get the content
            InputStream in = get.getResponseBodyAsStream();
            StringBuffer out = new StringBuffer();
            byte[] b = new byte[1024];
            for (int n; (n = in.read(b)) != -1;) {
                out.append(new String(b, 0, n));
            }
            in.close();
            content = out.toString();

        } finally {
            get.releaseConnection();
        }

        // parse the Document using Tidy
        Tidy tidy = TidyUtil.getInstance();
        Document document = tidy.parseDOM(new ByteArrayInputStream(content.getBytes()), null);

        // save the file
        XmlUtil.saveDocumentToFile(document, security.getIsin() + ".xml", "results/swissquote/", false);
        return document;
    }

    public static String getValue(Node document, String expression) throws TransformerException {

        Node node = XPathAPI.selectSingleNode(document, expression);
        if (node == null || node.getFirstChild() == null) return null;

        return node.getFirstChild().getNodeValue();
    }

    public static int getNumber(String inputString) throws ParseException {

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

    public static BigDecimal getBigDecimal(double value) {

        BigDecimal decimal = new BigDecimal(value);
        return decimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public  static Date getDate(String date) throws ParseException {

        if (date.startsWith("null")) return null;
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(date);
    }
}
