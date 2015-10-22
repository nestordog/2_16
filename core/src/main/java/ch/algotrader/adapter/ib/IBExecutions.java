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
package ch.algotrader.adapter.ib;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ch.algotrader.entity.trade.ExecutionStatusVO;
import ch.algotrader.enumeration.Status;

public final class IBExecutions {

    private final ConcurrentMap<String, IBExecution> executionMap;

    public IBExecutions() {
        this.executionMap = new ConcurrentHashMap<>();
    }

    public IBExecution addNew(final String id) {

        IBExecution newEntry = new IBExecution();
        newEntry.setStatus(Status.OPEN);
        IBExecution existingEntry = this.executionMap.putIfAbsent(id, newEntry);
        return existingEntry != null ? existingEntry : newEntry;
    }

    public IBExecution getOpen(final String id, final ExecutionStatusVO executionStatus) {

        IBExecution execution = this.executionMap.get(id);
        if (execution == null) {
            IBExecution newEntry = new IBExecution();
            newEntry.setStatus(executionStatus.getStatus());
            newEntry.setFilledQuantity(executionStatus.getFilledQuantity());
            newEntry.setRemainingQuantity(executionStatus.getRemainingQuantity());
            IBExecution existingEntry = this.executionMap.putIfAbsent(id, newEntry);
            execution = existingEntry != null ? existingEntry : newEntry;
        }
        return execution;
    }

}
