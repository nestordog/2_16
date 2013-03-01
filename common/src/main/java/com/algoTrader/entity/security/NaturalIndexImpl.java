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
package com.algoTrader.entity.security;

import com.algoTrader.entity.marketData.Tick;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class NaturalIndexImpl extends NaturalIndex {

    private static final long serialVersionUID = 4087343403938105040L;

    @Override
    public boolean validateTick(Tick tick) {

        if (tick.getLast() == null) {
            return false;
        } else if (tick.getLastDateTime() == null) {
            return false;
        }

        return super.validateTick(tick);
    }
}
