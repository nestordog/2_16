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
    IB,

    /**
     * BloomBerg
     */
    BB,

    /**
     * DukasCopy
     */
    DC,

    /**
     * FXCM
     */
    FXCM,

    /**
     * LMAX
     */
    LMAX,

    /**
     * CNX
     */
    CNX,

    /**
     * Fortex
     */
    FTX,

    /**
     * Trading Technologies
     */
    TT,

    /**
     * Simulation
     */
    SIM;

    private static final long serialVersionUID = 7802917315855678552L;

}
