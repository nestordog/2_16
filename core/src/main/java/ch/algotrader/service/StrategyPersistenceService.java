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
package ch.algotrader.service;

import ch.algotrader.entity.strategy.Strategy;

/**
 * Strategy persistence service.
 *
 * @author <a href="mailto:mterzer@algotrader.ch">Marco Terzer</a>
 */
public interface StrategyPersistenceService {

    /**
     * Persists the given {@link Strategy} instance.
     */
    void persistStrategy(Strategy strategy);

    /**
     * Returns the strategy with the given {@code name}. If such a strategy does
     * not exist it is created and initialized with the specified
     * {@code allocation}. If a strategy with the given {@code name} already
     * exists then it is returned after updating its allocation if necessary.
     *
     * @param name          the name of the strategy to return
     * @return the existing or newly created strategy entity
     */
    Strategy getOrCreateStrategy(String name);

    /**
     * Deletes the strategy with the given ID.
     *
     * @param strageyId the ID of the strategy to delete
     */
    void deleteStrategy(long strageyId);
}
