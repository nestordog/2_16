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
        this.allocations = new HashMap<>();
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
