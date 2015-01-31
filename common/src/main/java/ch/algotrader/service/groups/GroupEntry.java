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

import org.apache.commons.lang.Validate;

import ch.algotrader.service.StrategyService;

/**
 * Strategy group entry containing a service instance and its weight within the group.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class GroupEntry {

    private final StrategyService serviceInstance;
    private final double weight;

    public GroupEntry(final StrategyService serviceInstance, double weight) {

        Validate.notNull(serviceInstance, "ServiceInstance is null");

        this.serviceInstance = serviceInstance;
        this.weight = weight;
    }

    public StrategyService getServiceInstance() {
        return serviceInstance;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[");
        sb.append(serviceInstance.getClass().getCanonicalName());
        sb.append(": ").append(weight);
        sb.append("]");
        return sb.toString();
    }

}
