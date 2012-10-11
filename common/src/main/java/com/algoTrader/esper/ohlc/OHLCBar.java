package com.algoTrader.esper.ohlc;

import java.util.Date;

public class OHLCBar {

    private long time;
    private Double open;
    private Double close;
    private Double high;
    private Double low;

    public OHLCBar(long time, Double open, Double high, Double low, Double close) {

        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public long getTime() {
        return this.time;
    }

    public Double getOpen() {
        return this.open;
    }

    public Double getClose() {
        return this.close;
    }

    public Double getHigh() {
        return this.high;
    }

    public Double getLow() {
        return this.low;
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
