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
package ch.algotrader.entity.security;

import ch.algotrader.entity.marketData.Tick;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class StockImpl extends Stock {

    private static final long serialVersionUID = -6169238869632079681L;

    @Override
    public boolean validateTick(Tick tick) {

        // stocks need to have a BID and ASK
        if (tick.getBid() == null) {
            return false;
        } else if (tick.getVolBid() == 0) {
            return false;
        } else if (tick.getAsk() == null) {
            return false;
        }
        if (tick.getVolAsk() == 0) {
            return false;
        }

        return super.validateTick(tick);
    }
}
