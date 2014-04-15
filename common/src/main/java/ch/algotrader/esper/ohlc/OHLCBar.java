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
package ch.algotrader.esper.ohlc;

import java.util.Date;

/**
 * Pojo used for {@link OHLCView}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
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
