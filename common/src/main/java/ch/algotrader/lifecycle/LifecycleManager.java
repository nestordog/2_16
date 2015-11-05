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
package ch.algotrader.lifecycle;

/**
 * LifecycleManager is intended to enforce transitions through standard {@link ch.algotrader.enumeration.LifecyclePhase}s
 * for server, embedded and strategy processes.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public interface LifecycleManager {

    /**
     * Creates and initializes all platform services.
     */
    void runServices() throws Exception;

    /**
     * Creates and initializes the platform server runtime which includes the server {@link ch.algotrader.esper.Engine}
     * and all platform services.
     */
    void runServer() throws Exception;

    /**
     * Creates and initializes strategy {@link ch.algotrader.esper.Engine}s, strategy specific services
     * and the platform server runtime in one JVM process.
     */
    void runEmbedded() throws Exception;

    /**
     * Creates and initializes strategy {@link ch.algotrader.esper.Engine}s and strategy specific services
     * and opens communication channels to a remote platform server runtime.
     */
    void runStrategy() throws Exception;

}
