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
 * Types of Broker
 */
public enum Broker {

    //InteractiveBrokers
    IB,

    // J.P.Morgan
    JPM,

    // DukasCopy
    DC,

    //Royal Bank of Scotland
    RBS,

    //RealTick
    RT,

    // FXCM
    FXCM,

    // LMAX
    LMAX,

    // CNX
    CNX,

    // Bloomberg
    BBG;

    private static final long serialVersionUID = -6191895924586464902L;

}
