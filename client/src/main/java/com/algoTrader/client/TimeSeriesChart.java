package com.algoTrader.client;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.jfree.beans.JTimeSeriesChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.algoTrader.vo.AxisVO;
import com.algoTrader.vo.DiagramVO;
import com.algoTrader.vo.ParameterVO;

public class TimeSeriesChart extends JTimeSeriesChart {

    private static final long serialVersionUID = -1511949341697529944L;

    @SuppressWarnings("unchecked")
    public void updateEvents(DiagramVO diagram, List<Object> events) throws Exception {

        XYPlot plot = (XYPlot) this.chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);

        // initialize the axis
        int axisNumber = 0;
        for (AxisVO axisVO : diagram.getAxis()) {

            //add a TimeSeriesCollection
            plot.setDataset(axisNumber, new TimeSeriesCollection());

            // configure the axis
            NumberAxis axis = new NumberAxis(axisVO.getLabel());

            // set the properteis
            axis.setAutoRange(axisVO.isAutoRange());
            if (axisVO.isAutoRange()) {
                axis.setAutoRangeIncludesZero(axisVO.isAutoRangeIncludesZero());
            } else {
                axis.setLowerBound(axisVO.getLowerBound());
                axis.setUpperBound(axisVO.getUpperBound());
            }

            plot.setRangeAxis(axisNumber, axis);

            // create the renderer
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setShapesVisible(false);
            plot.setRenderer(axisNumber, renderer);

            plot.mapDatasetToRangeAxis(axisNumber, axisNumber);

            axisNumber++;
        }

        // get the timeperiod class
        String timePeriodString = StringUtils.capitalize(diagram.getTimePeriod().getValue().toLowerCase());
        Class<RegularTimePeriod> timePeriodClass = (Class<RegularTimePeriod>) Class.forName("org.jfree.data.time." + timePeriodString);

        // define all TimeSeries
        Collection<ParameterVO> parameters = diagram.getParameters();
        for (ParameterVO parameter : parameters) {

            if (parameter.isSelected()) {

                // create the TimeSeries
                TimeSeries series = new TimeSeries(parameter.getLabel(), timePeriodClass);
                TimeSeriesCollection timeSeriesCollection = (TimeSeriesCollection) plot.getDataset(parameter.getAxis());
                timeSeriesCollection.addSeries(series);

                // set the color
                int seriesCount = timeSeriesCollection.getSeriesCount();
                String colorString = parameter.getColor().getValue();
                Color color = (Color) java.awt.Color.class.getField(colorString).get(null);
                plot.getRenderer(parameter.getAxis()).setSeriesPaint(seriesCount - 1, color);
            }
        }

        // add the DataItems
        for (Object object : events) {

            // get the timeperiod
            Constructor<?> constructor = timePeriodClass.getConstructor(Date.class);
            Object arg = PropertyUtils.getProperty(object, diagram.getDateTimeParam());
            RegularTimePeriod timePeriod = (RegularTimePeriod) constructor.newInstance(arg);

            // add the DataItems
            for (ParameterVO parameter : parameters) {
                if (parameter.isSelected()) {
                    Double value = (Double) PropertyUtils.getProperty(object, parameter.getName());
                    TimeSeriesCollection col = (TimeSeriesCollection) plot.getDataset(parameter.getAxis());
                    col.getSeries(parameter.getLabel()).addOrUpdate(timePeriod, value);
                }
            }
        }

        // configure the Date Axis (if startTime & endTime is set)
        DateAxis axis = new DateAxis();
        if (diagram.getStartTime() != null && diagram.getEndTime() != null) {

            // creat the SegmentedTimeline
            long startTime = diagram.getStartTime().getTime();
            long endTime = diagram.getEndTime().getTime();
            long segmentSize = 60 * 1000; // minute
            int segmentsIncluded = (int) (endTime - startTime) / (60 * 1000);
            int segmentsExcluded = 24 * 60 - segmentsIncluded;
            SegmentedTimeline timeline = new SegmentedTimeline(segmentSize, segmentsIncluded, segmentsExcluded);

            DateAxis oldAxis = (DateAxis) plot.getDomainAxis();
            Date fromDate = oldAxis.getMinimumDate();
            long fromTime = fromDate.getTime();
            long toTime = oldAxis.getMaximumDate().getTime();

            // get year/month/day from fromTime and hour/minute from diagrm.startTime
            Date truncatedDate = DateUtils.truncate(fromDate, Calendar.DAY_OF_MONTH);
            Calendar truncatedCalendar = DateUtils.toCalendar(truncatedDate);
            Calendar startCalendar = DateUtils.toCalendar(diagram.getStartTime());
            truncatedCalendar.set(Calendar.HOUR_OF_DAY, startCalendar.get(Calendar.HOUR_OF_DAY));
            truncatedCalendar.set(Calendar.MINUTE, startCalendar.get(Calendar.MINUTE));

            timeline.setStartTime(truncatedCalendar.getTimeInMillis());
            timeline.setBaseTimeline(SegmentedTimeline.newMondayThroughFridayTimeline());
            timeline.addBaseTimelineExclusions(fromTime, toTime);
            timeline.setAdjustForDaylightSaving(true);

            axis.setTimeline(timeline);
        }

        axis.setDateFormatOverride(new SimpleDateFormat("dd.MM.yy kk:mm"));
        axis.setVerticalTickLabels(true);

        plot.setDomainAxis(axis);
    }
}
