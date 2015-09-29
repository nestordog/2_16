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
package ch.algotrader.adapter.tt;

import java.util.ArrayList;
import java.util.List;

import ch.algotrader.concurrent.PromiseImpl;

/**
 */
public final class TTPendingRequest<T> {

    private final PromiseImpl<List<T>> promise;
    private final List<T> resultList;

    TTPendingRequest(final PromiseImpl<List<T>> promise) {
        this.promise = promise;
        this.resultList = new ArrayList<>();
    }

    public List<T> getResultList() {
        return this.resultList;
    }

    public void add(final T result) {
        this.resultList.add(result);
    }

    public void completed() {
        this.promise.completed(this.resultList);
    }

    public void fail(final Exception ex) {
        this.promise.failed(ex);
    }

    public void cancel() {
        this.promise.cancel();
    }

}
