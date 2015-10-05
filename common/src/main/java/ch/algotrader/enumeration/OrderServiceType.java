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
 * The Type of an Order Service. The string values contain the fully-qualified name of the
 * corresponding class.
 */
public enum OrderServiceType {

    SIMULATION,

    /**
     * Interactive Brokers Native Interface
     */
    IB_NATIVE,

    /**
     * Interactive Brokers Fix Interface
     */
    IB_FIX,

    /**
     * J.P.Morgan Fix Interface
     */
    JPM_FIX,

    /**
     * DukasCopy Fix Interface
     */
    DC_FIX,

    /**
     * RealTick Fix interface
     */
    RT_FIX,

    /**
     * FXCM Fix interface
     */
    FXCM_FIX,

    /**
     * LMAX Fix interface
     */
    LMAX_FIX,

    /**
     * Currenex Fix interface
     */
    CNX_FIX,

    /**
     * Fortex Fix interface
     */
    FTX_FIX,

    /**
     * Trading Technologies Fix interface
     */
    TT_FIX;

    private static final long serialVersionUID = -1547849286749430155L;

}
