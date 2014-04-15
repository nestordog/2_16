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
package ch.algotrader.entity.trade;

import java.text.SimpleDateFormat;

import ch.algotrader.entity.trade.Fill;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FillImpl extends Fill {

    private static final long serialVersionUID = 1619681349145226990L;
    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer();

        buffer.append(format.format(getExtDateTime()));
        buffer.append(",");
        buffer.append(getSide());
        buffer.append(",");
        buffer.append(getQuantity());

        if (getOrder() != null) {
            buffer.append(",");
            buffer.append(getOrder().getSecurity());
        }

        if (getOrder() != null) {
            buffer.append(",");
            buffer.append(getOrder().getStrategy());
        }

        buffer.append(",price=");
        buffer.append(getPrice());

        if (getOrder() != null) {
            buffer.append(",");
            buffer.append(getOrder().getSecurity().getSecurityFamily().getCurrency());
        }

        buffer.append(",extId=");
        buffer.append(getExtId());

        return buffer.toString();
    }
}
