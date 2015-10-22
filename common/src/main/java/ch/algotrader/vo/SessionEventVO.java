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
package ch.algotrader.vo;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.Validate;

import ch.algotrader.enumeration.ConnectionState;

/**
 * External service session event.
 */
public class SessionEventVO implements Serializable {

    private static final long serialVersionUID = 5279075101650300740L;

    private final ConnectionState state;
    private final String qualifier;
    private final Date timestamp;

    public SessionEventVO(final ConnectionState state, final String qualifier, final Date timestamp) {
        Validate.notNull(state, "State is null");
        Validate.notNull(qualifier, "Qualifier is null");
        Validate.notNull(timestamp, "Timestamp is null");
        this.state = state;
        this.qualifier = qualifier;
        this.timestamp = timestamp;
    }

    public SessionEventVO(final ConnectionState state, final String qualifier) {
        this(state, qualifier, new Date());
    }

    public ConnectionState getState() {
        return state;
    }

    public String getQualifier() {
        return qualifier;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[");
        sb.append(state).append("; ").append(qualifier).append("; ").append(timestamp);
        sb.append(']');
        return sb.toString();
    }

}