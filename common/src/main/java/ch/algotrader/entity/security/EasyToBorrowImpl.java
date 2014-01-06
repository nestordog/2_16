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
package ch.algotrader.entity.security;

import ch.algotrader.util.ObjectUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EasyToBorrowImpl extends EasyToBorrow {

    private static final long serialVersionUID = -5341417499112909950L;

    @Override
    public String toString() {

        return getDate() + " " + getBroker() + " " + getStock() + " " + getQuantity();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof EasyToBorrow) {
            EasyToBorrow that = (EasyToBorrow) obj;
            return ObjectUtil.equalsNonNull(this.getStock(), that.getStock()) &&
                    ObjectUtil.equalsNonNull(this.getDate(), that.getDate()) &&
                    ObjectUtil.equalsNonNull(this.getBroker(), that.getBroker());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + ObjectUtil.hashCode(getStock());
        hash = hash * 37 + ObjectUtil.hashCode(getDate());
        hash = hash * 37 + ObjectUtil.hashCode(getBroker());
        return hash;
    }
}
