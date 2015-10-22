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
package ch.algotrader.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Basic implementation of the {@link Promise} interface. {@code PromiseImpl}
 * can be put into a completed state by invoking any of the following methods:
 * {@link #cancel()}, {@link #failed(Exception)}, or {@link #completed(Object)}.
 *
 * @param <T> the future result type of an asynchronous operation.
 */
public class PromiseImpl<T> implements Promise<T> {

    private final PromiseCallback<T> callback;

    private volatile boolean completed;
    private volatile boolean cancelled;
    private volatile T result;
    private volatile Exception ex;

    public PromiseImpl(final PromiseCallback<T> callback) {
        super();
        this.callback = callback;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public boolean isDone() {
        return this.completed;
    }

    private T getResult() throws ExecutionException {
        if (this.ex != null) {
            throw new ExecutionException(this.ex);
        }
        return this.result;
    }

    @Override
    public synchronized T get() throws InterruptedException, ExecutionException {
        while (!this.completed) {
            wait();
        }
        return getResult();
    }

    @Override
    public synchronized T get(final long timeout, final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        final long msecs = unit.toMillis(timeout);
        final long startTime = (msecs <= 0) ? 0 : System.currentTimeMillis();
        long waitTime = msecs;
        if (this.completed) {
            return getResult();
        } else if (waitTime <= 0) {
            throw new TimeoutException();
        } else {
            for (;;) {
                wait(waitTime);
                if (this.completed) {
                    return getResult();
                } else {
                    waitTime = msecs - (System.currentTimeMillis() - startTime);
                    if (waitTime <= 0) {
                        throw new TimeoutException();
                    }
                }
            }
        }
    }

    public boolean completed(final T result) {
        synchronized(this) {
            if (this.completed) {
                return false;
            }
            this.completed = true;
            this.result = result;
            notifyAll();
        }
        if (this.callback != null) {
            this.callback.completed(result);
        }
        return true;
    }

    public boolean failed(final Exception exception) {
        synchronized(this) {
            if (this.completed) {
                return false;
            }
            this.completed = true;
            this.ex = exception;
            notifyAll();
        }
        if (this.callback != null) {
            this.callback.failed(exception);
        }
        return true;
    }

    @Override
    public boolean cancel() {
        synchronized(this) {
            if (this.completed) {
                return false;
            }
            this.completed = true;
            this.cancelled = true;
            notifyAll();
        }
        if (this.callback != null) {
            this.callback.cancelled();
        }
        return true;
    }

}
