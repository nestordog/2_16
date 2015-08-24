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
 * Starter Class for downloading {@link ch.algotrader.entity.security.Stock Stocks}
 * <p>
 * Usage: {@code ReferenceDataStarter securityFamilyId symbol1 symbol2 ...}
 * <p>
 * E.g. to download all S&P 500 Stocks get the list of symbols from http://www.cboe.com/products/snp500.aspx
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class StockRetrievalStarter {

    public static void main(String[] args) throws ParseException {

        ServiceLocator.instance().runServices();

        ReferenceDataService service = ServiceLocator.instance().getService("iBNativeReferenceDataService", ReferenceDataService.class);
        for (int i = 1; i < args.length; i++) {

            long securityFamilyId = Long.parseLong(args[0]);
            service.retrieveStocks(securityFamilyId, args[i]);
        }

        ServiceLocator.instance().shutdown();
    }
}
