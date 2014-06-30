package ch.algotrader.config;
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

/**
 * IB configuration object.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public final class IBConfig {

    private final String faMethod;
    private final String genericTickList;

    public IBConfig(
            @ConfigName("ib.faMethod") String faMethod,
            @ConfigName("ib.genericTickList") String genericTickList) {
        this.faMethod = faMethod;
        this.genericTickList = genericTickList;
    }

    public String getFaMethod() {
        return faMethod;
    }

    public String getGenericTickList() {
        return genericTickList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[");
        sb.append("faMethod='").append(faMethod).append('\'');
        sb.append(", genericTickList='").append(genericTickList).append('\'');
        sb.append(']');
        return sb.toString();
    }

}
