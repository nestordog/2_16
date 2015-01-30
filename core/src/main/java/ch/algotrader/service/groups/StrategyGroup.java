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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.Validate;

import ch.algotrader.service.StrategyService;

/**
 * Strategy group containing service instances and their respective weight.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class StrategyGroup {

    private final Map<Class<? extends StrategyService>, GroupEntry> instances;

    public StrategyGroup(final Map<? extends StrategyService, Double> instances) {

        Validate.notEmpty(instances, "Map of service instances is empty");

        this.instances = new ConcurrentHashMap<>(instances.size());
        for (Map.Entry<? extends StrategyService, Double> entry: instances.entrySet()) {

            StrategyService instance = entry.getKey();
            double weight = entry.getValue();

            Validate.isTrue(weight > 0.0d, "Strategy weight is negative or zero");
            this.instances.put(instance.getClass(), new GroupEntry(instance, weight));
        }
    }

    public StrategyGroup(final GroupEntry... entries) {

        Validate.notEmpty(entries, "Array of entries is empty");

        this.instances = new ConcurrentHashMap<>(entries.length);
        for (GroupEntry entry: entries) {

            StrategyService instance = entry.getServiceInstance();
            double weight = entry.getWeight();

            Validate.isTrue(weight > 0.0d, "Strategy weight is negative or zero");
            this.instances.put(instance.getClass(), entry);
        }
    }

    public List<GroupEntry> getEntries() {

        return new ArrayList<>(this.instances.values());
    }

}
