package com.algoTrader.util.mail;

public class EmailFragment {

    private byte[] data;
    private String filename;

    public EmailFragment(String filename, byte[] data) {
        this.filename = filename;
        this.data = data;
    }

    public byte[] getData() {
        return this.data;
    }

    public String getFilename() {
        return this.filename;
    }

    @Override
    public String toString() {
        return this.filename;
    }
}
