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
package ch.algorader.entity.trade;

import com.algoTrader.entity.trade.OrderValidationException;
import com.algoTrader.entity.trade.StopOrder;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class StopOrderImpl extends StopOrder {

    private static final long serialVersionUID = -9213820219309533525L;

    @Override
    public String getExtDescription() {
        return "stop: " + getStop();
    }

    @Override
    public void validate() throws OrderValidationException {

        if (getStop() == null) {
            throw new OrderValidationException("no stop defined for " + this);
        }
    }
}
