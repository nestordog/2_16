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
     * The elapsed wall time consumed by this statement in nanoseconds.
     */
    private long wallTime;

    /**
     * The number of executions of this statement.
     */
    private long numInput;

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
        this.wallTime = wallTimeIn;
        this.numInput = numInputIn;
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
        this.wallTime = otherBean.getWallTime();
        this.numInput = otherBean.getNumInput();
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
        builder.append(", wallTime=");
        builder.append(this.wallTime);
        builder.append(", numInput=");
        builder.append(this.numInput);
        builder.append("]");

        return builder.toString();
    }

}
