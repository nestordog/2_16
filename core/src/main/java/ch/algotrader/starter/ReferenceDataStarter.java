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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.event.EventListenerRegistry;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.NoServiceResponseException;
import ch.algotrader.service.ReferenceDataService;
import ch.algotrader.vo.SessionEventVO;

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

    private static final Logger LOGGER = LogManager.getLogger(ReferenceDataStarter.class);

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            LOGGER.error("Please specify a list of security family ids");
            return;
        }

        ServiceLocator serviceLocator = ServiceLocator.instance();
        serviceLocator.init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        try {

            CountDownLatch latch = new CountDownLatch(1);

            EventListenerRegistry eventListenerRegistry = serviceLocator.getEventListenerRegistry();
            eventListenerRegistry.register(event -> {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(event.getQualifier() + " -> " + event.getState());
                }
                if (event.getState() == ConnectionState.LOGGED_ON) {
                    latch.countDown();
                }
            }, SessionEventVO.class);

            serviceLocator.runServices();

            if (!latch.await(30, TimeUnit.SECONDS)) {
                LOGGER.error("Timeout waiting for the reference service session to become available");
                return;
            }

            ReferenceDataService service = serviceLocator.getReferenceDataService();
            LookupService lookupService = serviceLocator.getLookupService();
            for (String arg : args) {

                long securityFamilyId = Long.parseLong(arg);

                SecurityFamily securityFamily = lookupService.getSecurityFamily(securityFamilyId);
                if (securityFamily != null) {

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Retrieving security definitions for " + securityFamily);
                    }

                    try {
                        service.retrieve(securityFamilyId);
                    } catch (NoServiceResponseException ex) {
                        LOGGER.warn(ex.getMessage());
                    }

                } else {
                    LOGGER.error("Security family with id {} not found", securityFamilyId);
                }
            }
            LOGGER.info("Security definition retrieval completed");
        } finally {
            serviceLocator.shutdown();
        }
    }
}
