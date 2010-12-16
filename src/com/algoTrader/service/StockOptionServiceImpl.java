package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Order;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StockOptionFamily;
import com.algoTrader.entity.StockOptionImpl;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.Tick;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.enumeration.OrderStatus;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.stockOption.StockOptionSymbol;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.OrderVO;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

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

    @SuppressWarnings("unchecked")
    protected void handleSetMargins() throws Exception {

        List<Position> positions = getPositionDao().findOpenPositions();

        for (Position position : positions) {
            setMargin(position);
        }
    }

    protected void handleSetMargin(int positionId) throws Exception {

        Position position = getPositionDao().load(positionId);
        setMargin(position);
    }

    protected void handleSetMargin(Position position) throws Exception {

        StockOption stockOption = (StockOption) position.getSecurity();
        Tick stockOptionTick = stockOption.getLastTick();
        Tick underlayingTick = stockOption.getUnderlaying().getLastTick();

        if (stockOptionTick != null && underlayingTick != null && stockOptionTick.getCurrentValueDouble() > 0.0) {

            double marginPerContract = 0;
            try {
                marginPerContract = StockOptionUtil.getMaintenanceMargin(stockOption, stockOptionTick.getSettlement().doubleValue(), underlayingTick.getSettlement().doubleValue())
                        * stockOption.getSecurityFamily().getContractSize();
            } catch (IllegalArgumentException e) {
                logger.warn("could not calculate margin for " + stockOption.getSymbol(), e);
                return;
            }
            long numberOfContracts = Math.abs(position.getQuantity());
            BigDecimal totalMargin = RoundUtil.getBigDecimal(marginPerContract * numberOfContracts);

            position.setMaintenanceMargin(totalMargin);

            getPositionDao().update(position);

            Strategy strategy = position.getStrategy();

            int percent = (int) (strategy.getAvailableFundsDouble() / strategy.getCashBalanceDouble() * 100.0);
            if (strategy.getAvailableFundsDouble() >= 0) {
                logger.info("set margin for " + stockOption.getSymbol() + " to " + RoundUtil.getBigDecimal(marginPerContract) + " total margin: " + strategy.getMaintenanceMargin()
                        + " availableFunds: " + strategy.getAvailableFunds() + " (" + percent + "% of balance)");
            } else {
                logger.warn("set margin for " + stockOption.getSymbol() + " to " + RoundUtil.getBigDecimal(marginPerContract) + " total margin: " + strategy.getMaintenanceMargin()
                        + " availableFunds: " + strategy.getAvailableFunds() + " (" + percent + "% of balance)");
            }
        } else {
            logger.warn("no last tick available or currentValue to low to set margin on " + stockOption.getSymbol());
        }
    }

    public static class SetMarginsListener implements UpdateListener {

        public void update(EventBean[] newEvents, EventBean[] oldEvents) {

            long startTime = System.currentTimeMillis();
            logger.debug("setMargins start");

            ServiceLocator.serverInstance().getStockOptionService().setMargins();

            logger.debug("setMargins end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
        }
    }

    public static class ExpirePositionSubscriber {

        public void update(int positionId, int underlayingId, BigDecimal underlayingSpot) {

            long startTime = System.currentTimeMillis();
            logger.debug("expireStockOptions start");

            ServiceLocator.serverInstance().getStockOptionService().expirePosition(positionId);

            logger.debug("expireStockOptions end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
        }
    }
}
