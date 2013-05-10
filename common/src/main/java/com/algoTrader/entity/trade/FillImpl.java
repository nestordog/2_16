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

import java.text.SimpleDateFormat;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
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

        if (getOrd() != null) {
            buffer.append(",");
            buffer.append(getOrd().getSecurity());
        }

        if (getOrd() != null) {
            buffer.append(",");
            buffer.append(getOrd().getStrategy());
        }

        buffer.append(",price=");
        buffer.append(getPrice());

        if (getOrd() != null) {
            buffer.append(",");
            buffer.append(getOrd().getSecurity().getSecurityFamily().getCurrency());
        }

        buffer.append(",extId=");
        buffer.append(getExtId());

        return buffer.toString();
    }
}
