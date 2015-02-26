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

import java.text.ParseException;

import ch.algotrader.ServiceLocator;
import ch.algotrader.service.ReferenceDataService;

/**
 * Starter Class for downloading {@link ch.algotrader.entity.security.Future Future} and {@link ch.algotrader.entity.security.Option Option} chains.
 * <p>
 * Usage: {@code ReferenceDataStarter securityFamilyId1 securityFamilyId2}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ReferenceDataStarter {

    public static void main(String[] args) throws ParseException {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);

        ServiceLocator.instance().getLifecycleManager().initServices();

        ReferenceDataService service = ServiceLocator.instance().getService("referenceDataService", ReferenceDataService.class);
        for (String arg : args) {

            int securityFamilyId = Integer.parseInt(arg);
            service.retrieve(securityFamilyId);
        }

        ServiceLocator.instance().shutdown();
    }
}
