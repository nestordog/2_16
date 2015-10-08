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
package ch.algotrader.service;

import ch.algotrader.service.groups.StrategyContextAware;

/**
 * Base strategy that implements all event listener interfaces. Events are
 * propagated to the listener methods. Alternatively strategies can implement
 * listener interfaces selectively without needing to extend this class.
 * <p>
 * The framework is made aware of event listener via Spring. As a consequence,
 * all implementors of these interfaces should be managed by the Spring
 * container.
 * <p>
 * In addition the service receives a reference to the strategy config
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ConfigAwareStrategyService<C> extends StrategyService implements StrategyContextAware<C> {

    private C config;

    @Override
    public void setConfig(final C config) {
        this.config = config;
    }

    public C getConfig() {
        return this.config;
    }

}
