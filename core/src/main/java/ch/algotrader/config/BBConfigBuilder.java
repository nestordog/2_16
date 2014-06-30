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
package ch.algotrader.config;

/**
 * Factory for Algotrader BB configuration objects.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public final class BBConfigBuilder {

    private String host;
    private int port;

    BBConfigBuilder() {
    }

    public static BBConfigBuilder create() {
        return new BBConfigBuilder();
    }

    public BBConfigBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public BBConfigBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public BBConfig build() {
        return new BBConfig(host, port);
    }

}
