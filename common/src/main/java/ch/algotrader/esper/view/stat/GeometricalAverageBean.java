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
package ch.algotrader.esper.view.stat;

import com.espertech.esper.client.EPException;
import java.io.Serializable;

/**
 * Bean for performing statistical calculations. The bean keeps sums of X squared data points.
 * The bean calculates geometrical average.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class GeometricalAverageBean implements Cloneable, Serializable {

    private static final long serialVersionUID = 7985193760056277184L;

    private double prodX = 1;
    private long dataPoints;

    private void initialize() {
        this.prodX = 1;
        this.dataPoints = 0;
    }

    /**
     * Add a data point for the X data set only.
     * @param x is the X data point to add.
     */
    public final void addPoint(double x) {
        this.dataPoints++;
        this.prodX *= x;
    }

    /**
     * Add a data point.
     * @param x is the X data point to add.
     * @param y is the Y data point to add.
     */
    public final void addPoint(double x, double y) {
        this.dataPoints++;
        this.prodX *= x;
    }

    /**
     * Remove a X data point only.
     * @param x is the X data point to remove.
     */
    public final void removePoint(double x) {
        this.dataPoints--;
        if (this.dataPoints <= 0) {
            initialize();
        } else {
            this.prodX /= x;
        }
    }

    /**
     * Remove a data point.
     * @param x is the X data point to remove.
     * @param y is the Y data point to remove.
     */
    public final void removePoint(double x, double y) {
        this.dataPoints--;
        if (this.dataPoints <= 0) {
            initialize();
        } else {
            this.prodX /= x;
        }
    }

    public final double getGeomAvgX() {
        return Math.pow(this.prodX, (1.0 / this.dataPoints));
    }

    @Override
    public final Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new EPException(e);
        }
    }

    @Override
    public final String toString() {
        return "datapoints=" + this.dataPoints + "  prodX=" + this.prodX;
    }
}
