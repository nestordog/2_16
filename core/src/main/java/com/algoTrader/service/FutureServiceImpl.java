package com.algoTrader.service;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.FutureFamily;
import com.algoTrader.entity.security.FutureImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.future.FutureSymbol;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;

public class FutureServiceImpl extends FutureServiceBase {

    private static Logger logger = MyLogger.getLogger(FutureServiceImpl.class.getName());

    @Override
    protected void handleCreateDummyFutures(int futureFamilyId) {

        FutureFamily family = getFutureFamilyDao().load(futureFamilyId);
        Security underlaying = family.getUnderlaying();

        List<Future> futures = getFutureDao().findAllFutures(underlaying.getId(), DateUtil.getCurrentEPTime());

        // create the missing part of the futures chain
        for (int i = futures.size() + 1; i <= family.getLength(); i++) {

            int duration = i * family.getExpirationMonths();

            Date expirationDate = DateUtil.getExpirationDateNMonths(family.getExpirationType(), DateUtil.getCurrentEPTime(), duration);

            String symbol = FutureSymbol.getSymbol(family, expirationDate);
            String isin = FutureSymbol.getIsin(family, expirationDate);

            Future future = new FutureImpl();
            future.setIsin(isin);
            future.setSymbol(symbol);
            future.setExpiration(expirationDate);
            future.setUnderlaying(underlaying);
            future.setSecurityFamily(family);

            getFutureDao().create(future);

            logger.info("created future " + future.getSymbol());
        }
    }
}
