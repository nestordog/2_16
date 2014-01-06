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
package ch.algotrader.entity.strategy;

import ch.algotrader.util.ObjectUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class AllocationImpl extends Allocation {

    private static final long serialVersionUID = 1184764642126535447L;

    @Override
    public String toString() {

        return getAccount() + ":" + getValue();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof Allocation) {
            Allocation that = (Allocation) obj;
            return ObjectUtil.equalsNonNull(this.getOrderPreference(), that.getOrderPreference()) &&
                    ObjectUtil.equalsNonNull(this.getAccount(), that.getAccount());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + ObjectUtil.hashCode(getOrderPreference());
        hash = hash * 37 + ObjectUtil.hashCode(getAccount());
        return hash;
    }
}
