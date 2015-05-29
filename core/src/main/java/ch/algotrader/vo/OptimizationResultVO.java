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
 * A ValueObject representing the results of multiple simulation runs using the {@link
 * org.apache.commons.math.optimization.univariate.BrentOptimizer}
 */
public class OptimizationResultVO implements Serializable {

    private static final long serialVersionUID = 8856207078366863443L;

    /**
     * The name of the Parameter
     */
    private String parameter;

    /**
     * The maximum optimization result
     */
    private double result;

    /**
     * the function value at which the maximum value occured.
     */
    private double functionValue;

    /**
     * the number of iterations it toke to reach the result.
     */
    private int iterations;

    /**
     * Default Constructor
     */
    public OptimizationResultVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param parameterIn String
     * @param resultIn double
     * @param functionValueIn double
     * @param iterationsIn int
     */
    public OptimizationResultVO(final String parameterIn, final double resultIn, final double functionValueIn, final int iterationsIn) {

        this.parameter = parameterIn;
        this.result = resultIn;
        this.functionValue = functionValueIn;
        this.iterations = iterationsIn;
    }

    /**
     * Copies constructor from other OptimizationResultVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public OptimizationResultVO(final OptimizationResultVO otherBean) {

        this.parameter = otherBean.getParameter();
        this.result = otherBean.getResult();
        this.functionValue = otherBean.getFunctionValue();
        this.iterations = otherBean.getIterations();
    }

    public String getParameter() {

        return this.parameter;
    }

    public void setParameter(final String value) {

        this.parameter = value;
    }

    public double getResult() {
        return this.result;
    }

    public void setResult(final double value) {

        this.result = value;
    }

    public double getFunctionValue() {

        return this.functionValue;
    }

    public void setFunctionValue(final double value) {

        this.functionValue = value;
    }

    /**
     * Return the number of iterations it toke to reach the result.
     * @return iterations int
     */
    public int getIterations() {

        return this.iterations;
    }

    /**
     * Return the number of iterations it toke to reach the result.
     * @param value int
     */
    public void setIterations(final int value) {

        this.iterations = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("OptimizationResultVO [parameter=");
        builder.append(this.parameter);
        builder.append(", result=");
        builder.append(this.result);
        builder.append(", functionValue=");
        builder.append(this.functionValue);
        builder.append(", iterations=");
        builder.append(this.iterations);
        builder.append("]");

        return builder.toString();
    }

}
