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
package ch.algotrader.starter.parallel;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread Factory used in conjunction with {@link MavenLauncher} to start a simulation process in a separate JVM.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
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
