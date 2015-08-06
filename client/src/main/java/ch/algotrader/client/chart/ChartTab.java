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
package ch.algotrader.client.chart;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import org.apache.commons.lang.time.DateUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.AbstractXYAnnotation;
import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import ch.algotrader.enumeration.Color;
import ch.algotrader.enumeration.DatasetType;
import ch.algotrader.enumeration.TimePeriod;
import ch.algotrader.vo.client.AnnotationVO;
import ch.algotrader.vo.client.AxisDefinitionVO;
import ch.algotrader.vo.client.BarDefinitionVO;
import ch.algotrader.vo.client.BarVO;
import ch.algotrader.vo.client.BoxAnnotationVO;
import ch.algotrader.vo.client.ChartDataVO;
import ch.algotrader.vo.client.ChartDefinitionVO;
import ch.algotrader.vo.client.DatasetDefinitionVO;
import ch.algotrader.vo.client.IndicatorDefinitionVO;
import ch.algotrader.vo.client.IndicatorVO;
import ch.algotrader.vo.client.IntervalMarkerVO;
import ch.algotrader.vo.client.MarkerDefinitionVO;
import ch.algotrader.vo.client.MarkerVO;
import ch.algotrader.vo.client.PointerAnnotationVO;
import ch.algotrader.vo.client.SeriesDefinitionVO;
import ch.algotrader.vo.client.ValueMarkerVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ChartTab extends ChartPanel {

    private static final long serialVersionUID = -1511949341697529944L;
    private static final DateFormat formatter = new SimpleDateFormat("EEE dd.MM.yyyy HH:mm:ss");


    private ChartDefinitionVO chartDefinition;

    private Map<Long, OHLCSeries> bars;
    private Map<String, TimeSeries> indicators;
    private Map<String, Marker> markers;
    private Map<String, Boolean> markersSelectionStatus;
    private final ChartPlugin chartPlugin;

    public ChartTab(ChartPlugin chartPlugin) {

        super(new JFreeChart(new XYPlot()), true, true, true, true, true);
        this.chartPlugin = chartPlugin;

        initPopupMenu();
    }

    private void initPopupMenu() {

        this.getPopupMenu().addSeparator();

        JMenuItem resetMenuItem = new JMenuItem("Reset Chart");
        resetMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChartTab.this.chartPlugin.setInitialized(false);
                ChartTab.this.chartPlugin.newSwingWorker().execute();
            }
        });
        this.getPopupMenu().add(resetMenuItem);

        JMenuItem updateMenuItem = new JMenuItem("Update Chart Data");
        updateMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChartTab.this.chartPlugin.newSwingWorker().execute();
            }
        });
        this.getPopupMenu().add(updateMenuItem);

        this.getPopupMenu().addSeparator();
    }

    public long getMaxDate() {

        Range range = getPlot().getDataRange(getPlot().getDomainAxis());
        if (range != null) {
            return (long) range.getUpperBound();
        } else {
            return 0;
        }
    }

    public void init(ChartDefinitionVO chartDefinition) {

        // remove all components first
        this.removeAll();

        resetPopupMenu();

        this.chartDefinition = chartDefinition;

        // create the plot
        XYPlot plot = new XYPlot();

        // add gridlines
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        // create the JFreeChart
        JFreeChart chart = new JFreeChart(plot);
        this.setChart(chart);

        // init the maps
        this.bars = new HashMap<>();
        this.indicators = new HashMap<>();
        this.markers = new HashMap<>();
        this.markersSelectionStatus = new HashMap<>();

        // init domain axis
        initDomainAxis(chartDefinition);

        // init range axis
        initRangeAxis(chartDefinition);

        // create a subtitle
        TextTitle title = new TextTitle();
        title.setFont(new Font("SansSerif", 0, 9));
        chart.addSubtitle(title);

        // crosshair
        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairLockedOnData(true);
        plot.setRangeCrosshairVisible(true);
        plot.setDomainCrosshairLockedOnData(true);
    }

    private void resetPopupMenu() {

        // remove series checkboxes
        for (Component component : this.getPopupMenu().getComponents()) {
            if (component instanceof JCheckBoxMenuItem) {
                this.getPopupMenu().remove(component);
            }
        }
    }

    private XYPlot getPlot() {
        return (XYPlot) this.getChart().getPlot();
    }

    private void initDomainAxis(ChartDefinitionVO chartDefinition) {

        DateAxis domainAxis = new DateAxis();

        domainAxis.setVerticalTickLabels(true);

        getPlot().setDomainAxis(domainAxis);
    }

    private void initRangeAxis(ChartDefinitionVO chartDefinition) {

        int axisNumber = 0;
        int datasetNumber = 0;
        for (AxisDefinitionVO axisDefinition : chartDefinition.getAxisDefinitions()) {

            // configure the axis
            NumberAxis rangeAxis = new NumberAxis(axisDefinition.getLabel());

            // set the properteis
            rangeAxis.setAutoRange(axisDefinition.isAutoRange());
            if (axisDefinition.isAutoRange()) {
                rangeAxis.setAutoRangeIncludesZero(axisDefinition.isAutoRangeIncludesZero());
            } else {
                rangeAxis.setLowerBound(axisDefinition.getLowerBound());
                rangeAxis.setUpperBound(axisDefinition.getUpperBound());
            }

            if (axisDefinition.getNumberFormat() != null) {
                rangeAxis.setNumberFormatOverride(new DecimalFormat(axisDefinition.getNumberFormat())); //##0.00% / "##0.000"
            }

            getPlot().setRangeAxis(axisNumber, rangeAxis);
            getPlot().setRangeAxisLocation(axisNumber, AxisLocation.BOTTOM_OR_RIGHT);

            // initialize datasets
            for (DatasetDefinitionVO datasetDefinition : axisDefinition.getDatasetDefinitions()) {

                XYDataset dataset;
                if (DatasetType.TIME.equals(datasetDefinition.getType())) {

                    // create the time series collection
                    dataset = new TimeSeriesCollection();
                    getPlot().setDataset(datasetNumber, dataset);

                    // create the renderer
                    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
                    renderer.setBaseShapesVisible(false);

                    getPlot().setRenderer(datasetNumber, renderer);

                } else if (DatasetType.OHLC.equals(datasetDefinition.getType())) {

                    // create the ohlc series collection
                    dataset = new OHLCSeriesCollection();
                    getPlot().setDataset(datasetNumber, dataset);

                    // create the renderer
                    HideableCandlestickRenderer renderer = new HideableCandlestickRenderer();
                    renderer.setBaseToolTipGenerator(new HighLowItemLabelGenerator(new SimpleDateFormat("dd.MM.yyyy kk:mm:ss"), NumberFormat.getInstance()));
                    getPlot().setRenderer(datasetNumber, renderer);

                } else {
                    throw new IllegalArgumentException("illegal dataset type " + datasetDefinition.getType());
                }

                getPlot().mapDatasetToRangeAxis(datasetNumber, axisNumber);

                // initialize series
                initSeries(datasetNumber, datasetDefinition, dataset);

                datasetNumber++;
            }

            axisNumber++;
        }
    }

    private void initSeries(int datasetNumber, DatasetDefinitionVO datasetDefinition, XYDataset dataset) {

        for (SeriesDefinitionVO seriesDefinition : datasetDefinition.getSeriesDefinitions()) {

            if (seriesDefinition instanceof IndicatorDefinitionVO) {

                initTimeSeries(datasetNumber, dataset, seriesDefinition);

            } else if (seriesDefinition instanceof BarDefinitionVO) {

                initOHLCSeries(datasetNumber, dataset, seriesDefinition);

            } else if (seriesDefinition instanceof MarkerDefinitionVO) {

                initMarker(seriesDefinition);
            } else {
                throw new IllegalArgumentException("unknown series definition" + seriesDefinition.getClass());
            }
        }
    }

    private void initOHLCSeries(int datasetNumber, XYDataset dataset, SeriesDefinitionVO seriesDefinition) {

        BarDefinitionVO barDefinition = (BarDefinitionVO) seriesDefinition;
        OHLCSeriesCollection ohlcSeriesCollection = (OHLCSeriesCollection) dataset;
        ohlcSeriesCollection.setXPosition(TimePeriodAnchor.START);

        // create the TimeSeries
        OHLCSeries series = new OHLCSeries(barDefinition.getLabel());
        ohlcSeriesCollection.addSeries(series);
        this.bars.put(barDefinition.getSecurityId(), series);

        // get the seriesNumber & color
        final int seriesNumber = ohlcSeriesCollection.getSeriesCount() - 1;

        // configure the renderer
        final CandlestickRenderer renderer = (CandlestickRenderer) getPlot().getRenderer(datasetNumber);
        renderer.setSeriesPaint(seriesNumber, getColor(barDefinition.getColor()));
        renderer.setSeriesVisible(seriesNumber, seriesDefinition.isSelected());
        renderer.setAutoWidthMethod(HideableCandlestickRenderer.WIDTHMETHOD_SMALLEST);

        // add the menu item
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(seriesDefinition.getLabel());
        menuItem.setSelected(seriesDefinition.isSelected());
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetAxis();
                renderer.setSeriesVisible(seriesNumber, ((JCheckBoxMenuItem) e.getSource()).isSelected(), true);
                initAxis();
            }
        });
        this.getPopupMenu().add(menuItem);
    }

    private void initTimeSeries(int datasetNumber, XYDataset dataset, SeriesDefinitionVO seriesDefinition) {

        IndicatorDefinitionVO indicatorDefinition = (IndicatorDefinitionVO) seriesDefinition;
        TimeSeriesCollection timeSeriesCollection = (TimeSeriesCollection) dataset;

        // create the TimeSeries
        TimeSeries series = new TimeSeries(indicatorDefinition.getLabel());
        timeSeriesCollection.addSeries(series);
        this.indicators.put(indicatorDefinition.getName(), series);

        // get the seriesNumber & color
        final int seriesNumber = timeSeriesCollection.getSeriesCount() - 1;

        // configure the renderer
        final XYItemRenderer renderer = getPlot().getRenderer(datasetNumber);
        renderer.setSeriesPaint(seriesNumber, getColor(indicatorDefinition.getColor()));
        renderer.setSeriesVisible(seriesNumber, seriesDefinition.isSelected());
        renderer.setBaseToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());

        if (seriesDefinition.isDashed()) {
            renderer.setSeriesStroke(seriesNumber, new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 5.0f }, 0.0f));
        } else {
            renderer.setSeriesStroke(seriesNumber, new BasicStroke(0.5f));
        }

        // add the menu item
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(seriesDefinition.getLabel());
        menuItem.setSelected(seriesDefinition.isSelected());
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetAxis();
                renderer.setSeriesVisible(seriesNumber, ((JCheckBoxMenuItem) e.getSource()).isSelected());
                initAxis();
            }
        });
        this.getPopupMenu().add(menuItem);
    }

    private void initMarker(SeriesDefinitionVO seriesDefinition) {

        final MarkerDefinitionVO markerDefinition = (MarkerDefinitionVO) seriesDefinition;

        final Marker marker;
        if (markerDefinition.isInterval()) {
            marker = new IntervalMarker(0, 0);
            marker.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT); // position of the label
            marker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT); // position of the text within the label
        } else {
            marker = new ValueMarker(0);
            marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
            marker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
        }

        marker.setPaint(getColor(markerDefinition.getColor()));
        marker.setLabel(markerDefinition.getLabel());
        marker.setLabelFont(new Font("SansSerif", 0, 9));

        if (seriesDefinition.isDashed()) {
            marker.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 5.0f }, 0.0f));
        } else {
            marker.setStroke(new BasicStroke(1.0f));
        }

        getPlot().addRangeMarker(marker, markerDefinition.isInterval() ? Layer.BACKGROUND : Layer.FOREGROUND);

        this.markers.put(markerDefinition.getName(), marker);
        this.markersSelectionStatus.put(markerDefinition.getName(), seriesDefinition.isSelected());

        // add the menu item
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(seriesDefinition.getLabel());
        menuItem.setSelected(seriesDefinition.isSelected());
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetAxis();
                boolean selected = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                ChartTab.this.markersSelectionStatus.put(markerDefinition.getName(), selected);
                if (selected) {
                    if (marker instanceof ValueMarker) {
                        marker.setAlpha(1.0f);
                    } else {
                        marker.setAlpha(0.5f);
                    }
                } else {
                    marker.setAlpha(0);
                }
                initAxis();
            }
        });
        this.getPopupMenu().add(menuItem);
    }

    @SuppressWarnings("unchecked")
    public void updateData(ChartDataVO chartData) {

        resetAxis();

        // add/update indicators
        for (IndicatorVO indicator : chartData.getIndicators()) {

            RegularTimePeriod timePeriod = getRegularTimePeriod(indicator.getDateTime());
            TimeSeries series = this.indicators.get(indicator.getName());

            if (series != null) {
                series.addOrUpdate(timePeriod, indicator.getValue());
            }
        }

        // add/update bars
        for (BarVO bar : chartData.getBars()) {

            OHLCSeries series = this.bars.get(bar.getSecurityId());

            if (series != null) {

                // remove a value if it already exists
                RegularTimePeriod timePeriod = getRegularTimePeriod(bar.getDateTime());
                if (series.indexOf(timePeriod) >= 0) {
                    series.remove(timePeriod);
                }
                series.add(timePeriod, bar.getOpen().doubleValue(), bar.getHigh().doubleValue(), bar.getLow().doubleValue(), bar.getClose().doubleValue());
            }
        }

        // make all invisible since they might not currently have a value
        for (Marker marker : this.markers.values()) {
            marker.setAlpha(0);
        }

        // update markers
        for (MarkerVO markerVO : chartData.getMarkers()) {

            Marker marker = this.markers.get(markerVO.getName());
            Boolean selected = this.markersSelectionStatus.get(markerVO.getName());
            String name = marker.getLabel().split(":")[0];
            if (marker instanceof ValueMarker && markerVO instanceof ValueMarkerVO) {

                ValueMarker valueMarker = (ValueMarker) marker;
                ValueMarkerVO valueMarkerVO = (ValueMarkerVO) markerVO;
                valueMarker.setValue(valueMarkerVO.getValue());
                marker.setLabel(name + ": " + valueMarkerVO.getValue());
                marker.setAlpha(selected ? 1.0f : 0.0f);

            } else if (marker instanceof IntervalMarker && markerVO instanceof IntervalMarkerVO) {

                IntervalMarker intervalMarker = (IntervalMarker) marker;
                IntervalMarkerVO intervalMarkerVO = (IntervalMarkerVO) markerVO;
                intervalMarker.setStartValue(intervalMarkerVO.getStartValue());
                intervalMarker.setEndValue(intervalMarkerVO.getEndValue());
                marker.setLabel(name + ": " + intervalMarkerVO.getStartValue() + " - " + intervalMarkerVO.getEndValue());
                marker.setAlpha(selected ? 0.5f : 0.0f);

            } else {
                throw new RuntimeException(marker.getClass() + " does not match " + markerVO.getClass());
            }
        }

        // update annotations
        for (AnnotationVO annotationVO : chartData.getAnnotations()) {

            AbstractXYAnnotation annotation;
            if (annotationVO instanceof PointerAnnotationVO) {

                PointerAnnotationVO pointerAnnotationVO = (PointerAnnotationVO)annotationVO;
                XYPointerAnnotation pointerAnnotation = new XYPointerAnnotation(
                        pointerAnnotationVO.getText(),
                        pointerAnnotationVO.getDateTime().getTime(),
                        pointerAnnotationVO.getValue(),
                        3.926990816987241D);
                pointerAnnotation.setTipRadius(0);
                pointerAnnotation.setBaseRadius(20);
                pointerAnnotation.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
                pointerAnnotation.setFont(new Font("SansSerif", 0, 9));
                pointerAnnotation.setToolTipText(
                        "<html>" +
                        formatter.format(pointerAnnotationVO.getDateTime()) +
                        "<br>" +
                        pointerAnnotationVO.getValue() +
                        "</html>");

                annotation = pointerAnnotation;

            } else if (annotationVO instanceof BoxAnnotationVO) {

                BoxAnnotationVO boxAnnotationVO = (BoxAnnotationVO)annotationVO;
                XYBoxAnnotation boxAnnotation = new XYBoxAnnotation(
                        boxAnnotationVO.getStartDateTime().getTime(),
                        boxAnnotationVO.getStartValue(),
                        boxAnnotationVO.getEndDateTime().getTime(),
                        boxAnnotationVO.getEndValue(),
                        null, null, new java.awt.Color(0, 0, 0, 60)
                        );
                boxAnnotation.setToolTipText(
                        "<html>" +
                        formatter.format(boxAnnotationVO.getStartDateTime()) + " - " +
                        formatter.format(boxAnnotationVO.getEndDateTime()) +
                        "<br>" +
                        boxAnnotationVO.getStartValue() + " - " +
                        boxAnnotationVO.getEndValue() +
                        "</html>");


                annotation = boxAnnotation;
            } else {
                throw new RuntimeException("unkown annotation type" + annotationVO.getClass());
            }

            if (!getPlot().getAnnotations().contains(annotation)) {
                getPlot().addAnnotation(annotation);
            }
        }

        // update description
        for (Title title : (List<Title>) this.getChart().getSubtitles()) {
            if (title instanceof TextTitle) {
                TextTitle textTitle = ((TextTitle) title);
                if (chartData.getDescription() != null && !("".equals(chartData.getDescription()))) {
                    textTitle.setText(chartData.getDescription());
                    textTitle.setVisible(true);
                } else {
                    textTitle.setVisible(false);
                }
            }
        }

        initAxis();
    }

    private void resetAxis() {

        // set a default timeline in order to compute the maximum date correctly
        DateAxis domainAxis = (DateAxis) getPlot().getDomainAxis();
        domainAxis.setTimeline(new DefaultTimeline());

        // reset value axis
        ValueAxis rangeAxis = getPlot().getRangeAxis();
        rangeAxis.setAutoRange(true);
    }

    private void initAxis() {

        DateAxis domainAxis = (DateAxis) getPlot().getDomainAxis();

        // configure the Date Axis (if startTime & endTime is set)
        if (this.chartDefinition.getStartTime() != null && this.chartDefinition.getEndTime() != null && !this.chartDefinition.getStartTime().equals(this.chartDefinition.getEndTime())) {

            // creat the SegmentedTimeline
            long startTime = this.chartDefinition.getStartTime().getTime();
            long endTime = this.chartDefinition.getEndTime().getTime();
            if (endTime == -3600000) {
                // adjust 00:00
                endTime += 86400000;
            }
            long segmentSize = 60 * 1000; // minute
            int segmentsIncluded = (int) (endTime - startTime) / (60 * 1000);
            int segmentsExcluded = 24 * 60 - segmentsIncluded;
            SegmentedTimeline timeline = new SegmentedTimeline(segmentSize, segmentsIncluded, segmentsExcluded);

            Date fromDate = domainAxis.getMinimumDate();
            Date toDate = domainAxis.getMaximumDate();
            long fromTime = fromDate.getTime();
            long toTime = toDate.getTime();

            // get year/month/day from fromTime and hour/minute from diagrm.startTime
            Date truncatedDate = DateUtils.truncate(fromDate, Calendar.DAY_OF_MONTH);
            Calendar truncatedCalendar = DateUtils.toCalendar(truncatedDate);
            Calendar startCalendar = DateUtils.toCalendar(this.chartDefinition.getStartTime());
            truncatedCalendar.set(Calendar.HOUR_OF_DAY, startCalendar.get(Calendar.HOUR_OF_DAY));
            truncatedCalendar.set(Calendar.MINUTE, startCalendar.get(Calendar.MINUTE));

            timeline.setStartTime(truncatedCalendar.getTimeInMillis());
            timeline.setBaseTimeline(SegmentedTimeline.newMondayThroughFridayTimeline());
            timeline.addBaseTimelineExclusions(fromTime, toTime);
            timeline.setAdjustForDaylightSaving(true);

            domainAxis.setTimeline(timeline);
        }

        // make sure the markers are within the rangeAxis
        ValueAxis rangeAxis = getPlot().getRangeAxis();
        for (Marker marker : this.markers.values()) {

            if (marker instanceof ValueMarker) {

                ValueMarker valueMarker = (ValueMarker) marker;
                if (marker.getAlpha() > 0 && valueMarker.getValue() != 0.0) {

                    if (valueMarker.getValue() < rangeAxis.getLowerBound()) {
                        rangeAxis.setLowerBound(valueMarker.getValue());
                        marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
                        marker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
                    }

                    if (valueMarker.getValue() > rangeAxis.getUpperBound()) {
                        rangeAxis.setUpperBound(valueMarker.getValue());
                        marker.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
                        marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
                    }
                }
            } else {

                IntervalMarker intervalMarker = (IntervalMarker) marker;
                if (marker.getAlpha() > 0 && intervalMarker.getStartValue() != 0.0) {

                    if (intervalMarker.getStartValue() < rangeAxis.getLowerBound()) {
                        rangeAxis.setLowerBound(intervalMarker.getStartValue());
                        marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
                        marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
                    }

                    if (intervalMarker.getEndValue() > rangeAxis.getUpperBound()) {
                        rangeAxis.setUpperBound(intervalMarker.getEndValue());
                        marker.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
                        marker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
                    }
                }
            }
        }
    }

    private java.awt.Color getColor(Color color) {

        java.awt.Color awtColor = java.awt.Color.BLACK; // use black as default
        try {
            awtColor = (java.awt.Color) java.awt.Color.class.getField(color.name()).get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return awtColor;
    }

    private RegularTimePeriod getRegularTimePeriod(Date date) {

        if (TimePeriod.MSEC.equals(this.chartDefinition.getTimePeriod())) {
            return new Millisecond(date);
        } else if (TimePeriod.SEC.equals(this.chartDefinition.getTimePeriod())) {
            return new Second(date);
        } else if (TimePeriod.MIN.equals(this.chartDefinition.getTimePeriod())) {
            return new Minute(date);
        } else if (TimePeriod.HOUR.equals(this.chartDefinition.getTimePeriod())) {
            return new Hour(date);
        } else if (TimePeriod.DAY.equals(this.chartDefinition.getTimePeriod())) {
            return new Day(date);
        } else if (TimePeriod.WEEK.equals(this.chartDefinition.getTimePeriod())) {
            return new Week(date);
        } else if (TimePeriod.MONTH.equals(this.chartDefinition.getTimePeriod())) {
            return new Month(date);
        } else if (TimePeriod.YEAR.equals(this.chartDefinition.getTimePeriod())) {
            return new Year(date);
        } else {
            throw new IllegalArgumentException("unkown TimePeriod: " + this.chartDefinition.getTimePeriod());
        }
    }
}
