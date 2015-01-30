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
package ch.algotrader.vo;

import java.util.Date;

/**
 * Contains a Box Annotation represented as a Box in the Chart
 */
public class BoxAnnotationVO extends AnnotationVO {

    private static final long serialVersionUID = 495844841636346178L;

    /**
     * The startDateTime of this Box Annotation
     */
    private Date startDateTime;

    /**
     * The endDateTime of this Box Annotation
     */
    private Date endDateTime;

    /**
     * The lower value of this Box Annotation
     */
    private double startValue;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setStartValue = false;

    /**
     * The upper value of this Box Annotation
     */
    private double endValue;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setEndValue = false;

    /**
     * Default Constructor
     */
    public BoxAnnotationVO() {

        super();
    }

    /**
     * Constructor with all properties
     * @param startDateTimeIn Date
     * @param endDateTimeIn Date
     * @param startValueIn double
     * @param endValueIn double
     */
    public BoxAnnotationVO(final Date startDateTimeIn, final Date endDateTimeIn, final double startValueIn, final double endValueIn) {

        super();

        this.startDateTime = startDateTimeIn;
        this.endDateTime = endDateTimeIn;
        this.startValue = startValueIn;
        this.setStartValue = true;
        this.endValue = endValueIn;
        this.setEndValue = true;
    }

    /**
     * Copies constructor from other BoxAnnotationVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public BoxAnnotationVO(final BoxAnnotationVO otherBean) {

        super(otherBean);

        this.startDateTime = otherBean.getStartDateTime();
        this.endDateTime = otherBean.getEndDateTime();
        this.startValue = otherBean.getStartValue();
        this.setStartValue = true;
        this.endValue = otherBean.getEndValue();
        this.setEndValue = true;
    }

    /**
     * The startDateTime of this Box Annotation
     * @return startDateTime Date
     */
    public Date getStartDateTime() {

        return this.startDateTime;
    }

    /**
     * The startDateTime of this Box Annotation
     * @param value Date
     */
    public void setStartDateTime(final Date value) {

        this.startDateTime = value;
    }

    /**
     * The endDateTime of this Box Annotation
     * @return endDateTime Date
     */
    public Date getEndDateTime() {

        return this.endDateTime;
    }

    /**
     * The endDateTime of this Box Annotation
     * @param value Date
     */
    public void setEndDateTime(final Date value) {

        this.endDateTime = value;
    }

    /**
     * The lower value of this Box Annotation
     * @return startValue double
     */
    public double getStartValue() {

        return this.startValue;
    }

    /**
     * The lower value of this Box Annotation
     * @param value double
     */
    public void setStartValue(final double value) {

        this.startValue = value;
        this.setStartValue = true;
    }

    /**
     * Return true if the primitive attribute startValue is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetStartValue() {

        return this.setStartValue;
    }

    /**
     * The upper value of this Box Annotation
     * @return endValue double
     */
    public double getEndValue() {

        return this.endValue;
    }

    /**
     * The upper value of this Box Annotation
     * @param value double
     */
    public void setEndValue(final double value) {

        this.endValue = value;
        this.setEndValue = true;
    }

    /**
     * Return true if the primitive attribute endValue is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetEndValue() {

        return this.setEndValue;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("BoxAnnotationVO [startDateTime=");
        builder.append(this.startDateTime);
        builder.append(", endDateTime=");
        builder.append(this.endDateTime);
        builder.append(", startValue=");
        builder.append(this.startValue);
        builder.append(", setStartValue=");
        builder.append(this.setStartValue);
        builder.append(", endValue=");
        builder.append(this.endValue);
        builder.append(", setEndValue=");
        builder.append(this.setEndValue);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("]");

        return builder.toString();
    }

}
