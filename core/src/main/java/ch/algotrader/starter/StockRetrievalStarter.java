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
package ch.algotrader.starter;

import java.text.ParseException;

import ch.algotrader.ServiceLocator;
import ch.algotrader.service.ib.IBNativeSecurityRetrieverService;

/**
 * Starter Class for downloading {@link ch.algotrader.entity.security.Stock Stocks}
 * <p>
 * Usage: {@code SecurityRetrievalStarter securityFamilyId symbol1 symbol2 ...}
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class StockRetrievalStarter {

    public static void main(String[] args) throws ParseException {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        IBNativeSecurityRetrieverService service = ServiceLocator.instance().getService("iBNativeSecurityRetrieverService", IBNativeSecurityRetrieverService.class);

        service.init();

        for (int i = 1; i < args.length; i++) {

            int securityFamilyId = Integer.parseInt(args[0]);
            service.retrieveStocks(securityFamilyId, args[i]);
        }

        ServiceLocator.instance().shutdown();
    }
}
