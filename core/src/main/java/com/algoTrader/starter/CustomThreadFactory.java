/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.starter;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
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
