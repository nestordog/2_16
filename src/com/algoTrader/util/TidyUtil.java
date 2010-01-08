package com.algoTrader.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.w3c.tidy.Tidy;


public class TidyUtil {

    private static class NullOutputStream extends OutputStream {

        public synchronized void write(byte[] b, int off, int len) {}

        public synchronized void write(int b) {}

        public void write(byte[] b) throws IOException {}
    }

    private static Tidy _tidy;

    /**
     * returns a singleton instance of the HTMLParser
     */
    public static Tidy getInstance() {

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
}
