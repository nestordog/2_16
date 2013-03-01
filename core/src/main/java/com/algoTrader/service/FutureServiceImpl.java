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
package com.algoTrader.service;

import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;

import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.FutureFamily;
import com.algoTrader.entity.security.FutureImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.future.FutureSymbol;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FutureServiceImpl extends FutureServiceBase {

    private static Logger logger = MyLogger.getLogger(FutureServiceImpl.class.getName());

    @Override
    protected void handleCreateDummyFutures(int futureFamilyId) {

        FutureFamily family = getFutureFamilyDao().get(futureFamilyId);
        Security underlying = family.getUnderlying();

        Collection<Future> futures = getFutureDao().findByMinExpiration(family.getId(), DateUtil.getCurrentEPTime());

        // create the missing part of the futures chain
        for (int i = futures.size() + 1; i <= family.getLength(); i++) {

            int duration = i * family.getExpirationMonths();

            Date expirationDate = DateUtil.getExpirationDateNMonths(family.getExpirationType(), DateUtil.getCurrentEPTime(), duration);

            String symbol = FutureSymbol.getSymbol(family, expirationDate);
            String isin = FutureSymbol.getIsin(family, expirationDate);
            String ric = FutureSymbol.getRic(family, expirationDate);

            Future future = new FutureImpl();
            future.setSymbol(symbol);
            future.setIsin(isin);
            future.setRic(ric);
            future.setExpiration(expirationDate);
            future.setUnderlying(underlying);
            future.setSecurityFamily(family);

            getFutureDao().create(future);

            logger.info("created future " + future);
        }
    }
}
