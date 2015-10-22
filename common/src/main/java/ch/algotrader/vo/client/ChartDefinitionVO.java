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
package ch.algotrader.vo.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import ch.algotrader.enumeration.TimePeriod;

/**
 * Defines a Chart.
 */
public class ChartDefinitionVO implements Serializable {

    private static final long serialVersionUID = -1655742276981557477L;

    /**
     * The {@link TimePeriod} this Chart should be based on
     */
    private TimePeriod timePeriod;

    /**
     * The daily {@code startTime} this Chart should begin. See {@code
     * org.jfree.chart.axis.SegmentedTimeline}
     */
    private Date startTime;

    /**
     * The daily {@code endTime} this Chart should end. See {@code
     * org.jfree.chart.axis.SegmentedTimeline}
     */
    private Date endTime;

    /**
     * Defines a Chart Axis
     */
    private Collection<AxisDefinitionVO> axisDefinitions;

    /**
     * Default Constructor
     */
    public ChartDefinitionVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor taking only required properties
     * @param timePeriodIn TimePeriod The {@link TimePeriod} this Chart should be based on
     */
    public ChartDefinitionVO(final TimePeriod timePeriodIn) {

        this.timePeriod = timePeriodIn;
    }

    /**
     * Constructor with all properties
     * @param timePeriodIn TimePeriod
     * @param startTimeIn Date
     * @param endTimeIn Date
     * @param axisDefinitionsIn Collection<AxisDefinitionVO>
     */
    public ChartDefinitionVO(final TimePeriod timePeriodIn, final Date startTimeIn, final Date endTimeIn, final Collection<AxisDefinitionVO> axisDefinitionsIn) {

        this.timePeriod = timePeriodIn;
        this.startTime = startTimeIn;
        this.endTime = endTimeIn;
        this.axisDefinitions = axisDefinitionsIn;
    }

    /**
     * Copies constructor from other ChartDefinitionVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public ChartDefinitionVO(final ChartDefinitionVO otherBean) {

        this.timePeriod = otherBean.getTimePeriod();
        this.startTime = otherBean.getStartTime();
        this.endTime = otherBean.getEndTime();
        this.axisDefinitions = otherBean.getAxisDefinitions();
    }

    /**
     * The {@link TimePeriod} this Chart should be based on
     * @return timePeriod TimePeriod
     */
    public TimePeriod getTimePeriod() {

        return this.timePeriod;
    }

    /**
     * The {@link TimePeriod} this Chart should be based on
     * @param value TimePeriod
     */
    public void setTimePeriod(final TimePeriod value) {

        this.timePeriod = value;
    }

    /**
     * The daily {@code startTime} this Chart should begin. See {@code
     * org.jfree.chart.axis.SegmentedTimeline}
     * @return startTime Date
     */
    public Date getStartTime() {

        return this.startTime;
    }

    /**
     * The daily {@code startTime} this Chart should begin. See {@code
     * org.jfree.chart.axis.SegmentedTimeline}
     * @param value Date
     */
    public void setStartTime(final Date value) {

        this.startTime = value;
    }

    /**
     * The daily {@code endTime} this Chart should end. See {@code
     * org.jfree.chart.axis.SegmentedTimeline}
     * @return endTime Date
     */
    public Date getEndTime() {

        return this.endTime;
    }

    /**
     * The daily {@code endTime} this Chart should end. See {@code
     * org.jfree.chart.axis.SegmentedTimeline}
     * @param value Date
     */
    public void setEndTime(final Date value) {

        this.endTime = value;
    }

    /**
     * Defines a Chart Axis
     * Get the axisDefinitions Association
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the object.
     * @return this.axisDefinitions Collection<AxisDefinitionVO>
     */
    public Collection<AxisDefinitionVO> getAxisDefinitions() {

        if (this.axisDefinitions == null) {

            this.axisDefinitions = new ArrayList<>();
        }
        return this.axisDefinitions;
    }

    /**
     * Sets the axisDefinitions
     * @param value Collection<AxisDefinitionVO>
     */
    public void setAxisDefinitions(Collection<AxisDefinitionVO> value) {

        this.axisDefinitions = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("ChartDefinitionVO [timePeriod=");
        builder.append(this.timePeriod);
        builder.append(", startTime=");
        builder.append(this.startTime);
        builder.append(", endTime=");
        builder.append(this.endTime);
        builder.append(", axisDefinitions=");
        builder.append(this.axisDefinitions);
        builder.append("]");

        return builder.toString();
    }

}
