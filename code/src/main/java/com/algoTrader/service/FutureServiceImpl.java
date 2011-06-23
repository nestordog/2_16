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

    protected Future handleCreateDummyFuture(int futureFamilyId, java.util.Date expirationDate) throws java.lang.Exception {

        FutureFamily family = getFutureFamilyDao().load(futureFamilyId);
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

    protected Future handleCreateDummyFuture3MByTargetDate(int futureFamilyId, java.util.Date targetDate) throws java.lang.Exception {

        // set third Friday of next 3month cycle month
        Date expiration = DateUtil.getNextThirdFriday3MonthCycle(targetDate);

        return createDummyFuture(futureFamilyId, expiration);
    }
}
