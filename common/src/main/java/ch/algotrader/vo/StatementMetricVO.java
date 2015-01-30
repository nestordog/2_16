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

import java.io.Serializable;

/**
 * Represents Metrics information of a particular Esper Statement.
 */
public class StatementMetricVO implements Serializable {

    private static final long serialVersionUID = -3031016448623728100L;

    /**
     * the name of the Esper Engine.
     */
    private String engineURI;

    /**
     * The name of the statement
     */
    private String statementName;

    /**
     * The total CPU time consumed by this statement in nanoseconds.
     */
    private long cpuTime;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setCpuTime = false;

    /**
     * The elapsed wall time consumed by this statement in nanoseconds.
     */
    private long wallTime;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setWallTime = false;

    /**
     * The number of executions of this statement.
     */
    private long numInput;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setNumInput = false;

    /**
     * Default Constructor
     */
    public StatementMetricVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param engineURIIn String
     * @param statementNameIn String
     * @param cpuTimeIn long
     * @param wallTimeIn long
     * @param numInputIn long
     */
    public StatementMetricVO(final String engineURIIn, final String statementNameIn, final long cpuTimeIn, final long wallTimeIn, final long numInputIn) {

        this.engineURI = engineURIIn;
        this.statementName = statementNameIn;
        this.cpuTime = cpuTimeIn;
        this.setCpuTime = true;
        this.wallTime = wallTimeIn;
        this.setWallTime = true;
        this.numInput = numInputIn;
        this.setNumInput = true;
    }

    /**
     * Copies constructor from other StatementMetricVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public StatementMetricVO(final StatementMetricVO otherBean) {

        this.engineURI = otherBean.getEngineURI();
        this.statementName = otherBean.getStatementName();
        this.cpuTime = otherBean.getCpuTime();
        this.setCpuTime = true;
        this.wallTime = otherBean.getWallTime();
        this.setWallTime = true;
        this.numInput = otherBean.getNumInput();
        this.setNumInput = true;
    }

    /**
     * the name of the Esper Engine.
     * @return engineURI String
     */
    public String getEngineURI() {

        return this.engineURI;
    }

    /**
     * the name of the Esper Engine.
     * @param value String
     */
    public void setEngineURI(final String value) {

        this.engineURI = value;
    }

    /**
     * The name of the statement
     * @return statementName String
     */
    public String getStatementName() {

        return this.statementName;
    }

    /**
     * The name of the statement
     * @param value String
     */
    public void setStatementName(final String value) {

        this.statementName = value;
    }

    /**
     * The total CPU time consumed by this statement in nanoseconds.
     * @return cpuTime long
     */
    public long getCpuTime() {

        return this.cpuTime;
    }

    /**
     * The total CPU time consumed by this statement in nanoseconds.
     * @param value long
     */
    public void setCpuTime(final long value) {

        this.cpuTime = value;
        this.setCpuTime = true;
    }

    /**
     * Return true if the primitive attribute cpuTime is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetCpuTime() {

        return this.setCpuTime;
    }

    /**
     * The elapsed wall time consumed by this statement in nanoseconds.
     * @return wallTime long
     */
    public long getWallTime() {

        return this.wallTime;
    }

    /**
     * The elapsed wall time consumed by this statement in nanoseconds.
     * @param value long
     */
    public void setWallTime(final long value) {

        this.wallTime = value;
        this.setWallTime = true;
    }

    /**
     * Return true if the primitive attribute wallTime is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetWallTime() {

        return this.setWallTime;
    }

    /**
     * The number of executions of this statement.
     * @return numInput long
     */
    public long getNumInput() {

        return this.numInput;
    }

    /**
     * The number of executions of this statement.
     * @param value long
     */
    public void setNumInput(final long value) {

        this.numInput = value;
        this.setNumInput = true;
    }

    /**
     * Return true if the primitive attribute numInput is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetNumInput() {

        return this.setNumInput;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("StatementMetricVO [engineURI=");
        builder.append(this.engineURI);
        builder.append(", statementName=");
        builder.append(this.statementName);
        builder.append(", cpuTime=");
        builder.append(this.cpuTime);
        builder.append(", setCpuTime=");
        builder.append(this.setCpuTime);
        builder.append(", wallTime=");
        builder.append(this.wallTime);
        builder.append(", setWallTime=");
        builder.append(this.setWallTime);
        builder.append(", numInput=");
        builder.append(this.numInput);
        builder.append(", setNumInput=");
        builder.append(this.setNumInput);
        builder.append("]");

        return builder.toString();
    }

}
