/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.entity.trade;

import org.apache.commons.lang.ClassUtils;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class OrderImpl extends Order {

    private static final long serialVersionUID = -6501807818853981164L;

    @Override
    public String toString() {

        return getDescription() + (!"".equals(getExtDescription()) ? " " + getExtDescription() : "");
    }

    @Override
    public String getDescription() {

        //@formatter:off
        return getSide()
        + " " + getQuantity()
        + " " + ClassUtils.getShortClassName(this.getClass())
        + " " + getSecurity()
        + " " + getStrategy()
        + (getIntId() != null ? " intId: " + getIntId() : "")
        + (getAccount() != null ? " account: " + getAccount() : "");
        //@formatter:on
    }

    @Override
    public void setQuantity(long quantityIn) {

        // always set a positive quantity
        super.setQuantity(Math.abs(quantityIn));
    }

    @Override
    public String getRootIntId() {

        // for FIX Orders remove the Order Version
        if (getIntId().contains(".")) {
            return getIntId().split("\\.")[0];
        } else {
            return getIntId();
        }
    }
}
