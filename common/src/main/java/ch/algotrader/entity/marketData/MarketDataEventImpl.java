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
package ch.algotrader.entity.marketData;

import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.enumeration.Direction;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class MarketDataEventImpl extends MarketDataEvent {

    private static final long serialVersionUID = 8758212212560594623L;

    private static @Value("${simulation}") boolean simulation;

    @Override
    public double getCurrentValueDouble() {

        return getCurrentValue().doubleValue();
    }

    @Override
    public double getMarketValueDouble(Direction direction) {

        return getMarketValue(direction).doubleValue();
    }
}
