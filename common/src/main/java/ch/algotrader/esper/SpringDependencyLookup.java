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
package ch.algotrader.esper;

import org.apache.commons.lang.Validate;
import org.springframework.context.ApplicationContext;

import ch.algotrader.config.DependencyLookup;

/**
 * Dependency lookup based.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class SpringDependencyLookup implements DependencyLookup {

    private final ApplicationContext applicationContext;

    public SpringDependencyLookup(final ApplicationContext applicationContext) {
        Validate.notNull(applicationContext, "ApplicationContext is null");
        this.applicationContext = applicationContext;
    }

    public Object getBean(final String name) {

        return this.applicationContext.getBean(name);
    }

    public <T> T getBean(final String name, final Class<T> requiredType) {

        return this.applicationContext.getBean(name, requiredType);
    }

}
