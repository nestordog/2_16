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
package ch.algotrader.entity.strategy;

import ch.algotrader.util.ObjectUtil;


/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultOrderPreferenceImpl extends DefaultOrderPreference {

    private static final long serialVersionUID = -5231151073076967781L;

    @Override
    public String toString() {

        return getStrategy() + ":" + getSecurityFamily() + ":" + getOrderPreference();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof DefaultOrderPreference) {
            DefaultOrderPreference that = (DefaultOrderPreference) obj;
            return ObjectUtil.equalsNonNull(this.getSecurityFamily(), that.getSecurityFamily()) &&
                    ObjectUtil.equalsNonNull(this.getStrategy(), that.getStrategy());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + ObjectUtil.hashCode(getSecurityFamily());
        hash = hash * 37 + ObjectUtil.hashCode(getStrategy());
        return hash;
    }
}
