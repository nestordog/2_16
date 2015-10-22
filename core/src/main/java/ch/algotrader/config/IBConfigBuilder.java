/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
package ch.algotrader.config;

/**
 * Factory for Algotrader BB configuration objects.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public final class IBConfigBuilder {

    private String faMethod;
    private String genericTickList;
    private boolean useRTH;

    IBConfigBuilder() {
    }

    public static IBConfigBuilder create() {
        return new IBConfigBuilder();
    }

    public IBConfigBuilder setFaMethod(String faMethod) {
        this.faMethod = faMethod;
        return this;
    }

    public IBConfigBuilder setGenericTickList(String genericTickList) {
        this.genericTickList = genericTickList;
        return this;
    }

    public IBConfigBuilder setUseRTH(boolean useRTH) {
        this.useRTH = useRTH;
        return this;
    }

    public IBConfig build() {
        return new IBConfig(faMethod, genericTickList, useRTH);
    }

}
