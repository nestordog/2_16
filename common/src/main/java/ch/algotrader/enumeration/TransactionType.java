/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.enumeration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Type of a Transaction
 */
public enum TransactionType {

    BUY("B"),

    SELL("S"),

    /**
     * Only valid for {@link ch.algotrader.entity.security.Future Futures} and {@link
     * ch.algotrader.entity.security.Option Options}
     */
    EXPIRATION("E"),

    /**
     * A Position Transfer between two Strategies.
     */
    TRANSFER("T"),

    /**
     * An inbound Cash Transfer.
     */
    CREDIT("C"),

    /**
     * An outbound Cash Transfer.
     */
    DEBIT("D"),

    /**
     * Intrest, the client had to pay.
     */
    INTREST_PAID("IP"),

    /**
     * Intrest, the client has received
     */
    INTREST_RECEIVED("IR"),

    /**
     * Dividend paid for a  {@link ch.algotrader.entity.security.Stock} or another Security
     */
    DIVIDEND("DI"),

    /**
     * Fees that had to be payed
     */
    FEES("F"),

    /**
     * A Refund that was made in the name of the client.
     */
    REFUND("RF"),

    /**
     * CashTransfer between Strategies.
     * <p>
     * <i>Note: The total of all {@code REBALANCES} of a particular {@code dateTime} has to be 0.</i>
     */
    REBALANCE("RB");

    private static final long serialVersionUID = 2931135696800257515L;

    private final String enumValue;

    /**
     * The constructor with enumeration literal value allowing
     * super classes to access it.
     */
    private TransactionType(String value) {

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

    /**
     * Returns an element of the enumeration by its value.
     *
     * @param value the value.
     * @return enumeration element
     */
    public static TransactionType fromValue(final String value) {

        if (value == null) {
            return null;
        }
        TransactionType instance = MAP_BY_VALUE.get(value);
        if (instance == null) {
            throw new IllegalArgumentException("Invalid value '" + value + "'");
        }
        return instance;
    }

    private static final Map<String, TransactionType> MAP_BY_VALUE;
    static {
        HashMap<String, TransactionType> map = new HashMap<>();
        for (TransactionType instance: TransactionType.values()) {

            map.put(instance.getValue(), instance);
        }
        MAP_BY_VALUE = new ConcurrentHashMap<>(map);
    }

}
