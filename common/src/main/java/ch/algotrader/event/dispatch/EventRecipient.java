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
package ch.algotrader.event.dispatch;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum EventRecipient {

    SERVER_ENGINE, STRATEGY_ENGINES, LISTENERS, REMOTE;

    public static Set<EventRecipient> ALL = EnumSet.allOf(EventRecipient.class);

    public static Set<EventRecipient> ALL_LOCAL = EnumSet.of(SERVER_ENGINE, STRATEGY_ENGINES, LISTENERS);

    public static Set<EventRecipient> ALL_LOCAL_STRATEGIES = EnumSet.of(STRATEGY_ENGINES, LISTENERS);

    public static Set<EventRecipient> ALL_LOCAL_LISTENERS = EnumSet.of(LISTENERS);

    public static Set<EventRecipient> ALL_STRATEGIES = EnumSet.of(STRATEGY_ENGINES, LISTENERS, REMOTE);

    public static Set<EventRecipient> ALL_LISTENERS = EnumSet.of(LISTENERS, REMOTE);

    public static Set<EventRecipient> SERVER_LISTENERS = EnumSet.of(SERVER_ENGINE, LISTENERS);

    public static Set<EventRecipient> SERVER_ENGINE_ONLY = EnumSet.of(SERVER_ENGINE);

    public static Set<EventRecipient> REMOTE_ONLY = EnumSet.of(REMOTE);

    public static Set<EventRecipient> of(EventRecipient... recipients) {

        Set<EventRecipient> set = EnumSet.noneOf(EventRecipient.class);
        Collections.addAll(set, recipients);
        return set;
    }

}
