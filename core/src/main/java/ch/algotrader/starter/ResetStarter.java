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

package ch.algotrader.starter;

import java.text.ParseException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import ch.algotrader.ServiceLocator;
import ch.algotrader.enumeration.ResetType;
import ch.algotrader.service.ResetService;

/**
 * Starter Class to reset the database
 * <p>
 * Usage: {@code ResetStarter <resetItems>}
 * <p>
 * resetItems is a list of {@link ch.algotrader.enumeration.ResetType ResetTypes}
 * <p>
 * Example: {@code RestorePortfolioValueStarter TRADES ORDERS SUBSCRIPTIONS MARKET_DATA}
 */
public class ResetStarter {

    public static void main(String[] args) throws ParseException {

        ServiceLocator serviceLocator = ServiceLocator.instance();
        serviceLocator.init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        try {
            ResetService resetService = serviceLocator.getService("resetService", ResetService.class);

            Set<ResetType> resetItems = new HashSet<ResetType>();
            for (String arg : args) {
                resetItems.add(ResetType.valueOf(arg));
            }

            resetService.reset(EnumSet.copyOf(resetItems));
        } finally {
            serviceLocator.shutdown();
        }
    }
}
