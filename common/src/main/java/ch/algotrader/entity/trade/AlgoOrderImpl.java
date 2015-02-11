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
package ch.algotrader.entity.trade;


import org.apache.commons.lang.ClassUtils;

import ch.algotrader.entity.marketData.Tick;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class AlgoOrderImpl extends AlgoOrder {

    private static final long serialVersionUID = 5310975560518020161L;

    @Override
    public boolean isAlgoOrder() {
        return true;
    }

    @Override
    public SimpleOrder modifyOrder(Tick tick) {
        throw new UnsupportedOperationException("modify order not supported by " + ClassUtils.getShortClassName(this.getClass()));
    }

    @Override
    public SimpleOrder nextOrder(long remainingQuantity, Tick tick) {
        throw new UnsupportedOperationException("next order not supported by " + ClassUtils.getShortClassName(this.getClass()));
    }
}
