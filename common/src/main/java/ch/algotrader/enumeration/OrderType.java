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
package ch.algotrader.enumeration;

/**
 * The Type of an Order (e.g. Market, Limit, etc.). The string values contain the fully-qualified
 * name of the corresponding class.
 */
public enum OrderType {

    MARKET("ch.algotrader.entity.trade.MarketOrderImpl"),

    LIMIT("ch.algotrader.entity.trade.LimitOrderImpl"),

    STOP("ch.algotrader.entity.trade.StopOrderImpl"),

    STOP_LIMIT("ch.algotrader.entity.trade.StopLimitOrderImpl"),

    SLICING("ch.algotrader.entity.trade.SlicingOrder"),

    TICKWISE_INCREMENTAL("ch.algotrader.entity.trade.TickwiseIncrementalOrder"),

    VARIABLE_INCREMENTAL("ch.algotrader.entity.trade.VariableIncrementalOrder"),

    DISTRIBUTIONAL("ch.algotrader.entity.trade.DistributingOrder");

    private static final long serialVersionUID = 8422386519639840923L;

    private final String enumValue;

    /**
     * The constructor with enumeration literal value allowing
     * super classes to access it.
     */
    private OrderType(String value) {

        this.enumValue = value;
    }

    /**
     * Gets the underlying value of this type safe enumeration.
     * This method is necessary to comply with DaoBase implementation.
     * @return The name of this literal.
     */
    public String getValue() {

        return this.enumValue;
    }

}
