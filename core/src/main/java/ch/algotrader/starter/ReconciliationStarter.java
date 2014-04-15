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
package ch.algotrader.starter;

import java.io.File;

import ch.algotrader.ServiceLocator;
import ch.algotrader.service.ReconciliationService;

/**
 * Starter Class to infoke the reconciliation process based on a file(s)
 * <p>
 * Usage: {@code ReconciliationStarter serviceName fileName(s)}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ReconciliationStarter {

    public static void main(String[] args) throws Exception {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        ReconciliationService service = ServiceLocator.instance().getService(args[0], ReconciliationService.class);

        for (int i = 1; i < args.length; i++) {

            File file = new File(args[i]);

            service.reconcile(file);
        }

        ServiceLocator.instance().shutdown();
    }
}
