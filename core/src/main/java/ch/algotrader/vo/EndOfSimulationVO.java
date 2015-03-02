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
package ch.algotrader.vo;

import java.io.Serializable;

/**
 * A ValueObject representing the End of a simulation run which is sent into the AlgoTrader Server
 * Esper Engine by the {@link ch.algotrader.simulation.SimulationExecutor}
 */
public class EndOfSimulationVO implements Serializable {

    private static final long serialVersionUID = -4863058803278086014L;

    /**
     * Default Constructor
     */
    public EndOfSimulationVO() {

        // documented empty block - avoid compiler warning
    }

    /**
     * Copies constructor from other EndOfSimulationVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public EndOfSimulationVO(final EndOfSimulationVO otherBean) {

        // documented empty block - avoid compiler warning
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("EndOfSimulationVO []");

        return builder.toString();
    }

}
