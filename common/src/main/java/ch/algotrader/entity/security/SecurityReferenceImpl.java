/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.entity.security;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class SecurityReferenceImpl extends SecurityReference {

    private static final long serialVersionUID = -6307682684589017658L;

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();
        buffer.append(getOwner());
        buffer.append(",");
        buffer.append(getName());
        buffer.append(",");
        buffer.append(getTarget());
        return buffer.toString();
    }
}
