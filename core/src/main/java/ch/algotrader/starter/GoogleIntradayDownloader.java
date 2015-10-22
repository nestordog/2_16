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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;

/**
 * Utility class to download multiple intraday market data files from finance.google.com
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class GoogleIntradayDownloader {

    private HttpClient httpclient;

    public static void main(String[] args) throws HttpException, IOException {

        (new GoogleIntradayDownloader()).run(args);
    }

    public GoogleIntradayDownloader() {

        this.httpclient = new HttpClient();
    }

    public void run(String[] args) throws HttpException, FileNotFoundException, IOException {

        int interval = Integer.parseInt(args[0]);
        int tradingDays = Integer.parseInt(args[1]);

        for (int i = 2; i < args.length; i++) {
            retrieve(this.httpclient, args[i], interval, tradingDays);
        }
    }

    private void retrieve(HttpClient httpclient, String symbol, int interval, int tradingDays) throws IOException, HttpException, FileNotFoundException {

        GetMethod fileGet = new GetMethod("http://www.google.com/finance/getprices?" + "q=" + symbol + "&i=" + interval + "&p=" + tradingDays + "d" + "&f=d,o,h,l,c,v");

        fileGet.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        try {
            int status = httpclient.executeMethod(fileGet);

            if (status == HttpStatus.SC_OK) {

                BufferedReader reader = new BufferedReader(new InputStreamReader(fileGet.getResponseBodyAsStream()));

                File parent = new File("files" + File.separator + "google");
                if (!parent.exists()) {
                    FileUtils.forceMkdir(parent);
                }

                Writer writer = new OutputStreamWriter(new FileOutputStream(new File(parent, symbol + "-" + (interval / 60) + "min.csv")));

                writer.write("dateTime,open,high,low,close,vol,barSize,security\n");

                try {

                    reader.readLine();
                    reader.readLine();
                    reader.readLine();
                    reader.readLine();
                    reader.readLine();
                    reader.readLine();
                    reader.readLine();

                    String line;
                    long timestamp = 0;
                    while ((line = reader.readLine()) != null) {
                        String tokens[] = line.split(",");

                        long time;
                        String timeStampString = tokens[0];
                        if (timeStampString.startsWith("a")) {
                            timestamp = Long.parseLong(timeStampString.substring(1)) * 1000;
                            time = timestamp;
                        } else {
                            time = timestamp + Integer.parseInt(timeStampString) * interval * 1000;
                        }

                        writer.write(Long.toString(time));
                        writer.write(",");
                        writer.write(tokens[1]);
                        writer.write(",");
                        writer.write(tokens[2]);
                        writer.write(",");
                        writer.write(tokens[3]);
                        writer.write(",");
                        writer.write(tokens[4]);
                        writer.write(",");
                        writer.write(tokens[5]);
                        writer.write(",");
                        writer.write("MIN_" + interval / 60);
                        writer.write(",");
                        writer.write(symbol);
                        writer.write("\n");
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
