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
package ch.algotrader.starter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;

import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimePatterns;

/**
 * Utility class to download multiple market data files from finance.google.com
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class GoogleDailyDownloader {

    private static final DateFormat fileFormat = new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH);

    private HttpClient httpclient;

    public static void main(String[] args) throws HttpException, IOException, ParseException {

        (new GoogleDailyDownloader()).run(args);
    }

    public GoogleDailyDownloader() {

        this.httpclient = new HttpClient();
    }

    public void run(String[] args) throws HttpException, FileNotFoundException, IOException, ParseException {

        String startDate = args[0];
        String endDate = "none".equals(args[1]) ? null : args[1];
        String exchange = args[2];

        for (int i = 3; i < args.length; i++) {
            retrieve(this.httpclient, args[i], startDate, endDate, exchange);
        }
    }

    private void retrieve(HttpClient httpclient, String symbol, String startDate, String endDate, String exchange) throws IOException, HttpException, FileNotFoundException, ParseException {

        GetMethod fileGet = new GetMethod(
                "https://www.google.com/finance/historical?q=" + exchange + ":" + symbol + "&output=csv&startdate=" + startDate + (endDate == null ? "" : "&endDate=" + endDate));

        fileGet.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        try {
            int status = httpclient.executeMethod(fileGet);

            if (status == HttpStatus.SC_OK) {

                BufferedReader reader = new BufferedReader(new InputStreamReader(fileGet.getResponseBodyAsStream()));

                File parent = new File("files" + File.separator + "google");
                if (!parent.exists()) {
                    FileUtils.forceMkdir(parent);
                }

                Writer writer = new OutputStreamWriter(new FileOutputStream(new File(parent, symbol + ".csv")));

                try {

                    reader.readLine();

                    String line;
                    List<String> lines = new ArrayList<String>();
                    while ((line = reader.readLine()) != null) {

                        String tokens[] = line.split(",");

                        Date dateTime = fileFormat.parse(tokens[0]);

                        StringBuffer buffer = new StringBuffer();
                        buffer.append(DateTimePatterns.LOCAL_DATE_TIME.format(DateTimeLegacy.toLocalDateTime(dateTime)));
                        buffer.append(",");
                        buffer.append(tokens[1].equals("-") ? "" : tokens[1]);
                        buffer.append(",");
                        buffer.append(tokens[2].equals("-") ? "" : tokens[2]);
                        buffer.append(",");
                        buffer.append(tokens[3].equals("-") ? "" : tokens[3]);
                        buffer.append(",");
                        buffer.append(tokens[4].equals("-") ? "" : tokens[4]);
                        buffer.append(",");
                        buffer.append(tokens[5]);
                        buffer.append("\n");

                        lines.add(buffer.toString());
                    }

                    writer.write("dateTime,open,high,low,close,vol\n");

                    // write in reverse order
                    for (int i = lines.size() - 1; i > 0; i--) {
                        writer.append(lines.get(i));
                    }

                } finally {
                    reader.close();
                    writer.close();
                }
            }
        } finally {
            fileGet.releaseConnection();
        }
    }
}
