package com.algoTrader.service;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Security;
import com.algoTrader.util.MyLogger;

public class WatchlistServiceImpl extends WatchlistServiceBase {

    private static Logger logger = MyLogger.getLogger(WatchlistServiceImpl.class.getName());

    protected void handlePutOnWatchlist(Security security) throws Exception {

        if (!security.isOnWatchlist()) {
            security.setOnWatchlist(true);
            getSecurityDao().update(security);

            logger.info("put stockOption on watchlist " + security.getSymbol());
        }
    }

    protected void handleRemoveFromWatchlist(Security security) throws Exception {

        if (security.isOnWatchlist()) {
            security.setOnWatchlist(false);
            getSecurityDao().update(security);

            logger.info("removed stockOption from watchlist " + security.getSymbol());
        }
    }

    protected void handlePutOnWatchlist(int securityId) throws Exception {

        Security security = getSecurityDao().load(securityId);
        putOnWatchlist(security);
    }

    protected void handleRemoveFromWatchlist(int securityId) throws Exception {

        Security security = getSecurityDao().load(securityId);
        removeFromWatchlist(security);
    }
}
