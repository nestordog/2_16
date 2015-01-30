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
import java.math.BigDecimal;

/**
 * A Generic ValueObject for representation of Money values.
 */
public class ValueVO implements Serializable {

    private static final long serialVersionUID = -8573348217970154035L;

    /**
     * The Money Value
     */
    private BigDecimal val;

    /**
     * Default Constructor
     */
    public ValueVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param valIn BigDecimal
     */
    public ValueVO(final BigDecimal valIn) {

        this.val = valIn;
    }

    /**
     * Copies constructor from other ValueVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public ValueVO(final ValueVO otherBean) {

        this.val = otherBean.getVal();
    }

    /**
     * The Money Value
     * @return val BigDecimal
     */
    public BigDecimal getVal() {

        return this.val;
    }

    /**
     * The Money Value
     * @param value BigDecimal
     */
    public void setVal(final BigDecimal value) {

        this.val = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("ValueVO [val=");
        builder.append(this.val);
        builder.append("]");

        return builder.toString();
    }

}
