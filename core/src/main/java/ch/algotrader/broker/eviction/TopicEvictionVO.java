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

package ch.algotrader.broker.eviction;

import java.io.Serializable;

public class TopicEvictionVO implements Serializable {

    private static final long serialVersionUID = -2537308753665883984L;

    private final String destination;

    public TopicEvictionVO(final String destination) {
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return "{" +
                "destination='" + destination + '\'' +
                '}';
    }

}
