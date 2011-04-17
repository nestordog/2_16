package com.algoTrader.service;

import java.util.Date;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Future;
import com.algoTrader.entity.FutureFamily;
import com.algoTrader.entity.FutureImpl;
import com.algoTrader.entity.Security;
import com.algoTrader.future.FutureSymbol;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;

public class FutureServiceImpl extends FutureServiceBase {

    private static Logger logger = MyLogger.getLogger(FutureServiceImpl.class.getName());

    protected Future handleCreateDummyFuture(int futureFamilyId, java.util.Date expirationDate) throws java.lang.Exception {

        FutureFamily family = (FutureFamily) getFutureFamilyDao().load(futureFamilyId);
        Security underlaying = family.getUnderlaying();

        // set third Friday of next 3month cycle month
        Date expiration = DateUtil.getNextThirdFriday3MonthCycle(expirationDate);

        // symbol / isin
        String symbol = FutureSymbol.getSymbol(family, expiration);
        String isin = FutureSymbol.getIsin(family, expiration);

        Future future = new FutureImpl();
        future.setIsin(isin);
        future.setSymbol(symbol);
        future.setExpiration(expiration);
        future.setUnderlaying(underlaying);
        future.setSecurityFamily(family);

        getFutureDao().create(future);

        logger.info("created dummy future " + future.getSymbol());

        return future;
    }

}
