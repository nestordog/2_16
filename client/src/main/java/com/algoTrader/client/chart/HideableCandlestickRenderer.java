package com.algoTrader.client.chart;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;

public class HideableCandlestickRenderer extends org.jfree.chart.renderer.xy.CandlestickRenderer {

    private static final long serialVersionUID = 3072314128773926519L;

    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series,
            int item, CrosshairState crosshairState, int pass) {

        if (getSeriesVisible(series) != null && !getSeriesVisible(series)) {
            return;
        }

        super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState, pass);
    }
}
