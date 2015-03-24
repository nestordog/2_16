/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
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
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

/**
 * Provides methods for HTML Cleaning.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TidyUtil {

    //@formatter:off
    private static final String[] regexs = new String[] {
        "<script(.*?)</script>",
        "<noscript(.*?)</noscript>",
        "<style(.*?)</style>",
        "<!--(.*?)-->", "<!(.*?)>",
        "<\\?(.*?)\\?>"
    };
    //@formatter:on

    private static class NullOutputStream extends OutputStream {

        @Override
        public synchronized void write(byte[] b, int off, int len) {
        }

        @Override
        public synchronized void write(int b) {
        }

        @Override
        public void write(byte[] b) throws IOException {
        }
    }

    private static Tidy _tidy;

    private static Tidy getInstance() {

        if (_tidy == null) {

            _tidy = new Tidy();
            _tidy.setXmlOut(true);
            _tidy.setXHTML(true);
            _tidy.setWord2000(true);
            _tidy.setTidyMark(false);
            _tidy.setLogicalEmphasis(true);
            _tidy.setEncloseText(true);
            _tidy.setQuiet(true);
            _tidy.setShowWarnings(false);
            _tidy.setErrout(new PrintWriter(new NullOutputStream()));
        }

        return _tidy;
    }

    /**
     * Parses an arbitrary {@link InputStream} and returns a cleaned {@link Document}.
     */
    public static Document parse(InputStream in) {

        return getInstance().parseDOM(in, null);
    }

    /**
     * Parses an arbitrary {@link InputStream} and returns a cleaned and filtered {@link Document}.
     * During the Filtering Process all HTML-Tags between any of the {@code regexs} will be removed.
     */
    public static Document parseAndFilter(InputStream in) throws IOException {

        // get the content
        String content;
        try {
            StringBuilder out = new StringBuilder();
            byte[] b = new byte[1024];
            for (int n; (n = in.read(b)) != -1;) {
                out.append(new String(b, 0, n, "UTF-8"));
            }

            content = out.toString();
        } finally {
            in.close();
        }

        // parse using the regex
        for (String regex : regexs) {
            Pattern noIndexPattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher noIndexMatcher = noIndexPattern.matcher(content);
            content = noIndexMatcher.replaceAll("");
        }

        // get the document
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
            return getInstance().parseDOM(inputStream, null);
        }
    }
}
