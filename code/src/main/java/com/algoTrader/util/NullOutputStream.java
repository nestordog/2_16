package com.algoTrader.util;

import java.io.IOException;
import java.io.OutputStream;

public class NullOutputStream extends OutputStream {

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        //to /dev/null
    }

    @Override
    public synchronized void write(int b) {
        //to /dev/null
    }

    @Override
    public void write(byte[] b) throws IOException {
        //to /dev/null
    }
}
