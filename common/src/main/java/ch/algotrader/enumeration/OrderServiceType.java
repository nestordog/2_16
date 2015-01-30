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

    SIMULATION("ch.algotrader.service.sim.SimulationOrderService"),

    /**
     * Interactive Brokers Native Interface
     */
    IB_NATIVE("ch.algotrader.service.ib.IBNativeOrderService"),

    /**
     * Interactive Brokers Fix Interface
     */
    IB_FIX("ch.algotrader.service.ib.IBFixOrderService"),

    /**
     * J.P.Morgan Fix Interface
     */
    JPM_FIX("ch.algotrader.service.jpm.JPMFixOrderService"),

    /**
     * DukasCopy Fix Interface
     */
    DC_FIX("ch.algotrader.service.dc.DCFixOrderService"),

    /**
     * RealTick Fix interface
     */
    RT_FIX("ch.algotrader.service.rt.RTFixOrderService"),

    /**
     * FXCM Fix interface
     */
    FXCM_FIX("ch.algotrader.service.fxcm.FXCMFixOrderService"),

    /**
     * LMAX Fix interface
     */
    LMAX_FIX("ch.algotrader.service.lmax.LMAXFixOrderService"),

    /**
     * LMAX Fix interface
     */
    CNX_FIX("ch.algotrader.service.cnx.CNXFixOrderService");

    private static final long serialVersionUID = -1547849286749430155L;

    private final String enumValue;

    /**
     * The constructor with enumeration literal value allowing
     * super classes to access it.
     */
    private OrderServiceType(String value) {

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
