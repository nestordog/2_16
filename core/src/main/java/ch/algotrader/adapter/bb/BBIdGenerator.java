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
package ch.algotrader.adapter.bb;

/**
 * BB Correlation Id Generator.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision: 5937 $ $Date: 2013-05-31 12:00:00 +0200 (Fr, 31 Mai 2013) $
 */
public final class BBIdGenerator {

    private static BBIdGenerator instance;
    private int requestId = 1;

    public static synchronized BBIdGenerator getInstance() {

        if (instance == null) {
            instance = new BBIdGenerator();
        }
        return instance;
    }

    public int getNextRequestId() {
        return this.requestId++;
    }
}
