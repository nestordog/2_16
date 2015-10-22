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

import ch.algotrader.ServiceLocator;

/**
 * Starter Class that initializes only the Spring Environment.
 * It does not initialize EsperEngines or execute any actions.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class NoActionStarter {

    public static void main(String[] args) throws Exception {

        ServiceLocator serviceLocator = ServiceLocator.instance();
        serviceLocator.init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        try {
            serviceLocator.getContext();
        } finally {
            serviceLocator.shutdown();
        }
    }
}
