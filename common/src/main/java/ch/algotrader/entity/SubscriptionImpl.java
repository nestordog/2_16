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
package ch.algotrader.entity;

import java.util.Objects;


/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SubscriptionImpl extends Subscription {

    private static final long serialVersionUID = -5408055861947044393L;

    @Override
    public String toString() {

        return getStrategy() + "," + getSecurity() + "," + getFeedType();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof Subscription) {
            Subscription that = (Subscription) obj;
            return Objects.equals(this.getSecurity(), that.getSecurity()) &&
                    Objects.equals(this.getStrategy(), that.getStrategy()) &&
                    Objects.equals(this.getFeedType(), that.getFeedType());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + Objects.hashCode(getSecurity());
        hash = hash * 37 + Objects.hashCode(getStrategy());
        hash = hash * 37 + Objects.hashCode(getFeedType());
        return hash;
    }
}
