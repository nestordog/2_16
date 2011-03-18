package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Order;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StockOptionFamily;
import com.algoTrader.entity.StockOptionImpl;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.enumeration.OrderStatus;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.stockOption.StockOptionSymbol;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.OrderVO;

public class StockOptionServiceImpl extends StockOptionServiceBase {

    private static Logger logger = MyLogger.getLogger(StockOptionServiceImpl.class.getName());

    protected StockOption handleCreateDummyStockOption(int stockOptionFamilyId, Date expirationDate, BigDecimal underlayingSpot, OptionType type) throws Exception {

        StockOptionFamily family = (StockOptionFamily) getStockOptionFamilyDao().load(stockOptionFamilyId);
        Security underlaying = family.getUnderlaying();

        // set third Friday of the month
        Date expiration = DateUtil.getNextThirdFriday(expirationDate);

        BigDecimal strike = RoundUtil.roundToNextN(underlayingSpot, family.getStrikeDistance(), type);

        // symbol / isin
        String symbol = StockOptionSymbol.getSymbol(family, expiration, type, strike);
        String isin = StockOptionSymbol.getIsin(family, expiration, type, strike);

        StockOption stockOption = new StockOptionImpl();
        stockOption.setIsin(isin);
        stockOption.setSymbol(symbol);
        stockOption.setStrike(strike);
        stockOption.setExpiration(expiration);
        stockOption.setType(type);
        stockOption.setUnderlaying(underlaying);
        stockOption.setSecurityFamily(family);

        getStockOptionDao().create(stockOption);

        logger.info("created dummy option " + stockOption.getSymbol());

        return stockOption;
    }

    protected void handleExpirePosition(int positionId) throws Exception {

        Position position = getPositionDao().load(positionId);

        if (position.getExitValue() == null || position.getExitValue().doubleValue() == 0d) {
            logger.warn(position.getSecurity().getSymbol() + " expired but did not have a exit value specified");
        }

        StockOption stockOption = (StockOption)position.getSecurity();

        long numberOfContracts = Math.abs(position.getQuantity());

        OrderVO order = new OrderVO();
        order.setStrategyName(position.getStrategy().getName());
        order.setSecurityId(stockOption.getId());
        order.setRequestedQuantity(numberOfContracts);
        order.setTransactionType(TransactionType.EXPIRATION);

        Order executedOrder = getDispatcherService().getTransactionService().executeTransaction(position.getStrategy().getName(), order);

        // only remove the stockOption from the watchlist, if the transaction did execute fully.
        // otherwise the next tick will execute the reminder of the order
        if (OrderStatus.EXECUTED.equals(executedOrder.getStatus()) || OrderStatus.AUTOMATIC.equals(executedOrder.getStatus())) {

            getDispatcherService().getTickService().removeFromWatchlist(position.getStrategy(), stockOption);
        }
    }

    public static class ExpirePositionSubscriber {

        public void update(int positionId) {

            long startTime = System.currentTimeMillis();
            logger.debug("expireStockOptions start");

            ServiceLocator.serverInstance().getStockOptionService().expirePosition(positionId);

            logger.debug("expireStockOptions end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
        }
    }
}
