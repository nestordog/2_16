package com.algoTrader.starter;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory implements ThreadFactory {

    static final AtomicInteger poolNumber = new AtomicInteger(0);

    public static class CustomThread extends Thread {

        private int number;

        public CustomThread(Runnable r, int number) {
            super(r);
            this.number = number;
        }

        public int getNumber() {
            return this.number;
        }
    }

    @Override
    public Thread newThread(Runnable r) {
        return new CustomThread(r, poolNumber.getAndIncrement());
    }
}
