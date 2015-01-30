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
package ch.algotrader.enumeration;

/**
 * The Type of DataFeed. The string values contain the fully-qualified name of the corresponding
 * market data service class.
 */
public enum FeedType {

    /**
     * Interactive Brokers
     */
    IB("ch.algotrader.service.ib.IBNativeMarketDataService"),

    /**
     * BloomBerg
     */
    BB("ch.algotrader.service.bb.BBMarketDataService"),

    /**
     * DukasCopy
     */
    DC("ch.algotrader.service.dc.DCFixMarketDataService"),

    /**
     * FXCM
     */
    FXCM("ch.algotrader.service.fxcm.FXCMFixMarketDataService"),

    /**
     * LMAX
     */
    LMAX("ch.algotrader.service.lmax.LMAXFixMarketDataService"),

    /**
     * CNX
     */
    CNX("ch.algotrader.service.cnx.CNXFixMarketDataService"),

    /**
     * Simulation
     */
    SIM("ch.algotrader.service.SimMarketDataService");

    private static final long serialVersionUID = 7802917315855678552L;

    private final String enumValue;

    /**
     * The constructor with enumeration literal value allowing
     * super classes to access it.
     */
    private FeedType(String value) {

        this.enumValue = value;
    }

    /**
     * Gets the underlying value of this type safe enumeration.
     * This method is necessary to comply with DaoBase implementation.
     * @return The name of this literal.
     */
    public String getValue() {

        return this.enumValue;
    }

}
