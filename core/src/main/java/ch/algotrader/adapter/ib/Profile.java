/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.adapter.ib;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class Profile {

    private final String name;
    Map<String, Double> allocations;

    public Profile(String name) {
        super();
        this.name = name;
        this.allocations = new HashMap<String, Double>();
    }

    public void putAllocation(String account, Double value) {

        this.allocations.put(account, value);
    }

    public String getName() {
        return this.name;
    }

    public Map<String, Double> getAllocations() {
        return this.allocations;
    }

}
