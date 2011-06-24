package com.algoTrader.service;

import java.util.Date;

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

    protected Future handleCreateDummyFuture(int futureFamilyId, Date targetExpirationDate) throws java.lang.Exception {

        FutureFamily family = getFutureFamilyDao().load(futureFamilyId);

        Date expirationDate = DateUtil.getExpirationDate(family.getExpirationType(), targetExpirationDate);

        return createDummyFuture(family, expirationDate);
    }

    protected Future handleCreateDummyFutureNMonths(int futureFamilyId, Date targetExpirationDate, int duration) throws java.lang.Exception {

        FutureFamily family = getFutureFamilyDao().load(futureFamilyId);

        Date expirationDate = DateUtil.getExpirationDateNMonths(family.getExpirationType(), targetExpirationDate, duration);

        return createDummyFuture(family, expirationDate);
    }

    private Future createDummyFuture(FutureFamily family, Date expirationDate) {

        Security underlaying = family.getUnderlaying();

        String symbol = FutureSymbol.getSymbol(family, expirationDate);
        String isin = FutureSymbol.getIsin(family, expirationDate);

        Future future = new FutureImpl();
        future.setIsin(isin);
        future.setSymbol(symbol);
        future.setExpiration(expirationDate);
        future.setUnderlaying(underlaying);
        future.setSecurityFamily(family);

        getFutureDao().create(future);

        logger.info("created dummy future " + future.getSymbol());

        return future;
    }
}
