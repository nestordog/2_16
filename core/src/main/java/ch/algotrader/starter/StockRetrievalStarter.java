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

import ch.algotrader.ServiceLocator;
import ch.algotrader.service.ReferenceDataService;

/**
 * Starter Class for downloading {@link ch.algotrader.entity.security.Stock Stocks}
 * <p>
 * Usage: {@code ReferenceDataStarter securityFamilyId symbol1 symbol2 ...}
 * <p>
 * E.g. to download all S&P 500 Stocks get the list of symbols from http://www.cboe.com/products/snp500.aspx
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class StockRetrievalStarter {

    public static void main(String[] args) throws ParseException {

        ServiceLocator serviceLocator = ServiceLocator.instance();
        serviceLocator.init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        try {
            serviceLocator.runServices();

            ReferenceDataService service = serviceLocator.getService("iBNativeReferenceDataService", ReferenceDataService.class);
            for (int i = 1; i < args.length; i++) {

                long securityFamilyId = Long.parseLong(args[0]);
                service.retrieveStocks(securityFamilyId, args[i]);
            }
        } finally {
            serviceLocator.shutdown();
        }
    }
}
