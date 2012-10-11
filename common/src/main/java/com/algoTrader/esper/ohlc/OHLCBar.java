package com.algoTrader.esper.ohlc;

import java.util.Date;

public class OHLCBar {

    private long time;
    private Double open;
    private Double close;
    private Double high;
    private Double low;

    public OHLCBar() {
    }

    public long getTime() {
        return this.time;
    }

    void setTime(long time) {
        this.time = time;
    }

    public Double getOpen() {
        return this.open;
    }

    void setOpen(Double open) {
        this.open = open;
    }

    public Double getClose() {
        return this.close;
    }

    void setClose(Double close) {
        this.close = close;
    }

    public Double getHigh() {
        return this.high;
    }

    void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return this.low;
    }

    void setLow(Double low) {
        this.low = low;
    }

    public double getOpenP() {
        return this.open;
    }

    public double getCloseP() {
        return this.close;
    }

    public double getHighP() {
        return this.high;
    }

    public double getLowP() {
        return this.low;
    }

    @Override
    public String toString() {

        return new Date(this.time) + " open=" + this.open + " high=" + this.high + " low=" + this.low + " close=" + this.close;
    }
}
