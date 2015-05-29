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
package ch.algotrader.adapter.bb;

import java.util.concurrent.atomic.AtomicLong;

/**
 * BB Correlation Id Generator.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision: 5937 $ $Date: 2013-05-31 12:00:00 +0200 (Fr, 31 Mai 2013) $
 */
public final class BBIdGenerator {

    private static BBIdGenerator instance;
    private final AtomicLong requestId = new AtomicLong(0);

    public static synchronized BBIdGenerator getInstance() {

        if (instance == null) {
            instance = new BBIdGenerator();
        }
        return instance;
    }

    public String getNextRequestId() {
        return Long.toString(this.requestId.incrementAndGet());
    }
}
