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

import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimeUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EasyToBorrowImpl extends EasyToBorrow {

    private static final long serialVersionUID = -5341417499112909950L;

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        DateTimeUtil.formatLocalDate(DateTimeLegacy.toLocalDate(getDate()), buffer);
        buffer.append(",");
        buffer.append(getBroker());
        buffer.append(",");
        buffer.append(getStock());
        buffer.append(",");
        buffer.append(getQuantity());
        return buffer.toString();
    }

}
