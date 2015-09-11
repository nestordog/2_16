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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ch.algotrader.enumeration.Status;

public final class IBExecutions {

    private final ConcurrentMap<String, IBExecution> executionMap;

    public IBExecutions() {
        this.executionMap = new ConcurrentHashMap<>();
    }

    public IBExecution add(final String id) {

        IBExecution newEntry = new IBExecution();
        newEntry.setStatus(Status.OPEN);
        IBExecution existingEntry = this.executionMap.putIfAbsent(id, newEntry);
        return existingEntry != null ? existingEntry : newEntry;
    }

    public IBExecution get(final String id) {

        IBExecution execution = this.executionMap.get(id);
        if (execution == null) {
            throw new IllegalStateException("Unexpected execution id: " + id);
        }
        return execution;
    }

    public IBExecution remove(final String id) {

        return this.executionMap.remove(id);
    }

}
