package com.algoTrader.client.chart;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
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
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import com.algoTrader.enumeration.Color;
import com.algoTrader.enumeration.DatasetType;
import com.algoTrader.vo.AnnotationVO;
import com.algoTrader.vo.AxisDefinitionVO;
import com.algoTrader.vo.BarDefinitionVO;
import com.algoTrader.vo.BarVO;
import com.algoTrader.vo.ChartDataVO;
import com.algoTrader.vo.ChartDefinitionVO;
import com.algoTrader.vo.DatasetDefinitionVO;
import com.algoTrader.vo.IndicatorDefinitionVO;
import com.algoTrader.vo.IndicatorVO;
import com.algoTrader.vo.IntervalMarkerVO;
import com.algoTrader.vo.MarkerDefinitionVO;
import com.algoTrader.vo.MarkerVO;
import com.algoTrader.vo.SeriesDefinitionVO;
import com.algoTrader.vo.ValueMarkerVO;

public class ChartTab extends ChartPanel {

    private static final long serialVersionUID = -1511949341697529944L;

    private ChartDefinitionVO chartDefinition;

    private Map<Integer, OHLCSeries> bars;
    private Map<String, TimeSeries> indicators;
    private Map<String, Marker> markers;
    private Map<Marker, Boolean> markersSelectionStatus;
    private ChartPlugin chartPlugin;

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
        this.bars = new HashMap<Integer, OHLCSeries>();
        this.indicators = new HashMap<String, TimeSeries>();
        this.markers = new HashMap<String, Marker>();
        this.markersSelectionStatus = new HashMap<Marker, Boolean>();

        // init domain axis
        initDomainAxis(chartDefinition);

        // init range axis
        initRangeAxis(chartDefinition);

        // create a subtitle
        TextTitle title = new TextTitle();
        title.setFont(new Font("SansSerif", 0, 9));
        chart.addSubtitle(title);
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

            if (axisDefinition.getTickUnit() != 0) {
                rangeAxis.setTickUnit(new NumberTickUnit(axisDefinition.getTickUnit()));
            }

            if (axisDefinition.getNumberFormat() != null) {
                rangeAxis.setNumberFormatOverride(new DecimalFormat(axisDefinition.getNumberFormat())); //##0.00% / "##0.000"
            }

            getPlot().setRangeAxis(axisNumber, rangeAxis);

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

        MarkerDefinitionVO markerDefinition = (MarkerDefinitionVO) seriesDefinition;

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

        getPlot().addRangeMarker(marker, markerDefinition.isInterval() ? Layer.BACKGROUND : Layer.FOREGROUND);

        this.markers.put(markerDefinition.getName(), marker);
        this.markersSelectionStatus.put(marker, seriesDefinition.isSelected());

        // add the menu item
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(seriesDefinition.getLabel());
        menuItem.setSelected(seriesDefinition.isSelected());
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetAxis();
                boolean selected = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                ChartTab.this.markersSelectionStatus.put(marker, selected);
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
            boolean selected = this.markersSelectionStatus.get(marker);
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
                intervalMarker.setStartValue(intervalMarkerVO.getStart());
                intervalMarker.setEndValue(intervalMarkerVO.getEnd());
                marker.setLabel(name + ": " + intervalMarkerVO.getStart() + " - " + intervalMarkerVO.getEnd());
                marker.setAlpha(selected ? 0.5f : 0.0f);

            } else {
                throw new RuntimeException(marker.getClass() + " does not match " + markerVO.getClass());
            }
        }

        // update annotations
        for (AnnotationVO annotationVO : chartData.getAnnotations()) {

            XYPointerAnnotation annotation = new XYPointerAnnotation(annotationVO.getText(), annotationVO.getDateTime().getTime(), annotationVO.getValue(), 3.926990816987241D);
            annotation.setToolTipText(annotationVO.getDescription());
            annotation.setTipRadius(0);
            annotation.setBaseRadius(20);
            annotation.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
            annotation.setFont(new Font("SansSerif", 0, 9));

            getPlot().addAnnotation(annotation);
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
        if (this.chartDefinition.getStartTime() != null && this.chartDefinition.getEndTime() != null) {

            // creat the SegmentedTimeline
            long startTime = this.chartDefinition.getStartTime().getTime();
            long endTime = this.chartDefinition.getEndTime().getTime();
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
            awtColor = (java.awt.Color) java.awt.Color.class.getField(color.getValue()).get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return awtColor;
    }

    @SuppressWarnings("unchecked")
    private RegularTimePeriod getRegularTimePeriod(Date date) {

        RegularTimePeriod regularTimePeriod = new Minute(date); // use Minute as default
        try {
            String timePeriodString = StringUtils.capitalize(this.chartDefinition.getTimePeriod().getValue().toLowerCase());
            Class<RegularTimePeriod> timePeriodClass = (Class<RegularTimePeriod>) Class.forName("org.jfree.data.time." + timePeriodString);
            Constructor<RegularTimePeriod> timePeriodConstructor = timePeriodClass.getConstructor(Date.class);

            regularTimePeriod = timePeriodConstructor.newInstance(date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return regularTimePeriod;
    }
}
