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
package ch.algotrader.enumeration;

/**
 * Life-cycle phases; they occur in definition order. Depending on the
 * {@link OperationMode} not all phases may be relevant.
 */
public enum LifecyclePhase {
    /**
     * Called <i>after</i> deploying all modules of the Server Engine but
     * <i>before</i> deploying the
     * {@link ch.algotrader.esper.Engine#deployInitModules() init modules} of
     * the Strategy Engines.
     */
    INIT,
    /**
     * Called <i>after</i> deploying the
     * {@link ch.algotrader.esper.Engine#deployInitModules() init modules} of
     * Strategy Engines but <i>before</i> deploying their
     * {@link ch.algotrader.esper.Engine#deployRunModules() run modules} and
     * <i>before</i> feeding any market data events.
     */
    PREFEED,
    /**
     * Called <i>after</i> deploying the
     * {@link ch.algotrader.esper.Engine#deployRunModules() run modules} of all
     * Engines. Market data events may or may not be feeding at this stage.
     */
    START,
    /**
     * In {@link OperationMode#SIMULATION SIMULATION} mode this event occurs
     * <i>after</i> finishing the simulation and <i>before</i> sending an
     * {@code EndOfSimulationVO} event and <i>before</i> publishing simulation
     * results.
     * <p>
     * In {@link OperationMode#REAL_TIME REAL_TIME} operation mode an EXIT
     * lifecycle event occurs in the Runtime
     * {@link Runtime#addShutdownHook(Thread) shutdown hook} when the virtual
     * machine begins its shutdown.
     */
    EXIT;
}
