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
package ch.algotrader.entity.security;

import java.text.SimpleDateFormat;

import ch.algotrader.util.ObjectUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EasyToBorrowImpl extends EasyToBorrow {

    private static final long serialVersionUID = -5341417499112909950L;
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public String toString() {

        return dayFormat.format(getDate()) + " " + getBroker() + " " + getStock() + " " + getQuantity();
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
