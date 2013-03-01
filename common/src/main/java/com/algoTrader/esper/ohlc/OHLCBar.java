/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.esper.ohlc;

import java.util.Date;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
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
