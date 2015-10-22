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
package ch.algotrader.esper.callback;

import ch.algotrader.esper.Engine;

/**
 * {@link Engine} aware callback.
 */
public abstract class AbstractEngineCallback implements EngineAwareCallback {

    private volatile Engine engine;

    public void setEngine(final Engine engine) {
        this.engine = engine;
    }

    protected Engine getEngine() {
        return this.engine;
    }

}
