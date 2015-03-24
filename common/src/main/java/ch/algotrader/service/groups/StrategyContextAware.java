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
package ch.algotrader.service.groups;

import ch.algotrader.esper.Engine;

/**
 * Interface to be implemented by a strategy service that wishes to be notified
 * of the information making up the strategy context. The elements are injected
 * into the service as properties --- see setters defined by this interface.
 *
 * @author <a href="mailto:mterzer@algotrader.ch">Marco Terzer</a>
 *
 * @version $Revision$ $Date$
 */
public interface StrategyContextAware<C> {
    /**
     * Sets the name for this service instance in the strategy context.
     * Corresponds to the strategy name in the strategy definition file and is
     * also used as {@link Engine#getEngineName() engine name}.
     *
     * @param strategyName the name for this service in the strategy context
     */
    void setStrategyName(String strategyName);

    /**
     * Sets the weight for this strategy item in the strategy group context.
     * Usually 1.0 if the strategy group contains only a single item.
     *
     * @param weight the item weight for this service within a strategy group
     */
    void setWeight(double weight);

    /**
     * Sets the engine associated with this strategy service instance. The
     * {@link Engine#getEngineName() engine name} is the same as the strategy
     * name (see {@link #setStrategyName(String)}).
     *
     * @param engine the engine associated with this strategy service instance
     */
    void setEngine(Engine engine);

    /**
     * Sets the config bean for this strategy service instance.
     *
     * @param config the config bean for this strategy service instance
     */
    void setConfig(C config);
}
