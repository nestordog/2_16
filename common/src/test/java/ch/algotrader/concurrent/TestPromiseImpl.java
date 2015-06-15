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
package ch.algotrader.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

public class TestPromiseImpl {

    @Test
    public void testCompleted() throws Exception {
        final BasicPromiseCallback<Object> callback = new BasicPromiseCallback<>();
        final PromiseImpl<Object> future = new PromiseImpl<>(callback);

        Assert.assertFalse(future.isDone());

        final Object result = new Object();
        final Exception boom = new Exception();
        future.completed(result);
        future.failed(boom);
        Assert.assertTrue(callback.isCompleted());
        Assert.assertSame(result, callback.getResult());
        Assert.assertFalse(callback.isFailed());
        Assert.assertNull(callback.getException());
        Assert.assertFalse(callback.isCancelled());

        Assert.assertSame(result, future.get());
        Assert.assertTrue(future.isDone());
        Assert.assertFalse(future.isCancelled());

    }

    @Test
    public void testCompletedWithTimeout() throws Exception {
        final BasicPromiseCallback<Object> callback = new BasicPromiseCallback<>();
        final PromiseImpl<Object> future = new PromiseImpl<>(callback);

        Assert.assertFalse(future.isDone());

        final Object result = new Object();
        final Exception boom = new Exception();
        future.completed(result);
        future.failed(boom);
        Assert.assertTrue(callback.isCompleted());
        Assert.assertSame(result, callback.getResult());
        Assert.assertFalse(callback.isFailed());
        Assert.assertNull(callback.getException());
        Assert.assertFalse(callback.isCancelled());

        Assert.assertSame(result, future.get(1, TimeUnit.MILLISECONDS));
        Assert.assertTrue(future.isDone());
        Assert.assertFalse(future.isCancelled());
    }

    @Test
    public void testFailed() throws Exception {
        final BasicPromiseCallback<Object> callback = new BasicPromiseCallback<>();
        final PromiseImpl<Object> future = new PromiseImpl<>(callback);
        final Object result = new Object();
        final Exception boom = new Exception();
        future.failed(boom);
        future.completed(result);
        Assert.assertFalse(callback.isCompleted());
        Assert.assertNull(callback.getResult());
        Assert.assertTrue(callback.isFailed());
        Assert.assertSame(boom, callback.getException());
        Assert.assertFalse(callback.isCancelled());

        try {
            future.get();
        } catch (final ExecutionException ex) {
            Assert.assertSame(boom, ex.getCause());
        }
        Assert.assertTrue(future.isDone());
        Assert.assertFalse(future.isCancelled());
    }

    @Test
    public void testCancelled() throws Exception {
        final BasicPromiseCallback<Object> callback = new BasicPromiseCallback<>();
        final PromiseImpl<Object> future = new PromiseImpl<>(callback);
        final Object result = new Object();
        final Exception boom = new Exception();
        future.cancel();
        future.failed(boom);
        future.completed(result);
        Assert.assertFalse(callback.isCompleted());
        Assert.assertNull(callback.getResult());
        Assert.assertFalse(callback.isFailed());
        Assert.assertNull(callback.getException());
        Assert.assertTrue(callback.isCancelled());

        Assert.assertNull(future.get());
        Assert.assertTrue(future.isDone());
        Assert.assertTrue(future.isCancelled());
    }

    @Test
    public void testAsyncCompleted() throws Exception {
        final PromiseImpl<Object> future = new PromiseImpl<>(null);
        final Object result = new Object();

        final Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    future.completed(result);
                } catch (final InterruptedException boom) {
                }
            }

        };
        t.setDaemon(true);
        t.start();
        Assert.assertSame(result, future.get(60, TimeUnit.SECONDS));
        Assert.assertTrue(future.isDone());
        Assert.assertFalse(future.isCancelled());
    }

    @Test
    public void testAsyncFailed() throws Exception {
        final PromiseImpl<Object> future = new PromiseImpl<>(null);
        final Exception boom = new Exception();

        final Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    future.failed(boom);
                } catch (final InterruptedException ex) {
                }
            }

        };
        t.setDaemon(true);
        t.start();
        try {
            future.get(60, TimeUnit.SECONDS);
        } catch (final ExecutionException ex) {
            Assert.assertSame(boom, ex.getCause());
        }
        Assert.assertTrue(future.isDone());
        Assert.assertFalse(future.isCancelled());
    }

    @Test
    public void testAsyncCancelled() throws Exception {
        final PromiseImpl<Object> future = new PromiseImpl<>(null);

        final Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    future.cancel();
                } catch (final InterruptedException ex) {
                }
            }

        };
        t.setDaemon(true);
        t.start();
        Assert.assertNull(future.get(60, TimeUnit.SECONDS));
        Assert.assertTrue(future.isDone());
        Assert.assertTrue(future.isCancelled());
    }

    @Test(expected=TimeoutException.class)
    public void testAsyncTimeout() throws Exception {
        final PromiseImpl<Object> future = new PromiseImpl<>(null);
        final Object result = new Object();

        final Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                    future.completed(result);
                } catch (final InterruptedException ex) {
                }
            }

        };
        t.setDaemon(true);
        t.start();
        future.get(1, TimeUnit.MILLISECONDS);
    }

    @Test(expected=TimeoutException.class)
    public void testAsyncNegativeTimeout() throws Exception {
        final PromiseImpl<Object> future = new PromiseImpl<>(null);
        future.get(-1, TimeUnit.MILLISECONDS);
    }

    static class BasicPromiseCallback<T> implements PromiseCallback<T> {

        private T result;
        private Exception ex;
        private boolean completed;
        private boolean failed;
        private boolean cancelled;

        @Override
        public void completed(final T result) {
            this.result = result;
            this.completed = true;
        }

        public T getResult() {
            return this.result;
        }

        public Exception getException() {
            return this.ex;
        }

        @Override
        public void failed(final Exception ex) {
            this.ex = ex;
            this.failed = true;
        }

        @Override
        public void cancelled() {
            this.cancelled = true;
        }

        public boolean isCompleted() {
            return this.completed;
        }

        public boolean isFailed() {
            return this.failed;
        }

        public boolean isCancelled() {
            return this.cancelled;
        }

    }

}
