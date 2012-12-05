package com.algoTrader.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Subscription;
import com.algoTrader.entity.security.Forex;
import com.algoTrader.entity.security.ForexFuture;
import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.FutureFamily;
import com.algoTrader.entity.trade.MarketOrder;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.entity.trade.OrderStatus;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.Side;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.esper.TradeCallback;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.BalanceVO;

public class ForexServiceImpl extends ForexServiceBase {

    private static Logger logger = MyLogger.getLogger(ForexServiceImpl.class.getName());

    private @Value("#{T(com.algoTrader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;
    private @Value("${misc.fxFutureEqualizationEnabled}") boolean fxFutureEqualizationEnabled;
    private @Value("${misc.fxFutureEqualizationMinTimeToExpiration}") int fxFutureEqualizationMinTimeToExpiration;
    private @Value("${misc.fxEqualizationMinAmount}") int fxEqualizationMinAmount;
    private @Value("${misc.fxEqualizationBatchSize}") int fxEqualizationBatchSize;

    @Override
    protected void handleEqualizeForex() throws Exception {

        Strategy base = getStrategyDao().findByName(StrategyImpl.BASE);

        // potentially close a ForexFuture position if it is below the MinTimeToExpiration
        if (this.fxFutureEqualizationEnabled) {

            // get the closing orders
            final List<Order> orders = new ArrayList<Order>();
            for (Position position : getLookupService().getOpenPositionsByStrategyAndType(StrategyImpl.BASE, ForexFuture.class)) {

                // check if expiration is below minimum
                ForexFuture forexFuture = (ForexFuture) position.getSecurityInitialized();
                if (forexFuture.getTimeToExpiration() < this.fxFutureEqualizationMinTimeToExpiration) {

                    Order order = MarketOrder.Factory.newInstance();
                    order.setStrategy(base);
                    order.setSecurity(forexFuture);
                    order.setQuantity(Math.abs(position.getQuantity()));
                    order.setSide(position.getQuantity() > 0 ? Side.SELL : Side.BUY);

                    orders.add(order);
                }
            }

            // setup an TradeCallback so that new hedge positions are only setup when existing positions are closed
            if (orders.size() > 0) {

                EsperManager.addTradeCallback(base.getName(), orders, new TradeCallback(true) {
                    @Override
                    public void onTradeCompleted(List<OrderStatus> orderStati) {

                        // build new hedge positions by invoking equalizeForex again
                        // use ServiceLocator because TradeCallback is executed in a new thread
                        ServiceLocator.instance().getService("forexService", ForexService.class).equalizeForex();
                    }
                });

                // send the orders
                for (Order order : orders) {
                    getOrderService().sendOrder(order);
                }

                return; // do not go any furter because closing trades will have to finish first
            }
        }

        // process all non-base currency balances
        Collection<BalanceVO> balances = getPortfolioService().getBalances();
        for (BalanceVO balance : balances) {

            if (balance.getCurrency().equals(this.portfolioBaseCurrency)) {
                continue;
            }

            // get the netLiqValueBase
            double netLiqValue = balance.getNetLiqValue().doubleValue();
            double netLiqValueBase = balance.getExchangeRate() * netLiqValue;

            // check if amount is larger than minimum
            if (Math.abs(netLiqValueBase) >= this.fxEqualizationMinAmount) {

                // get the forex
                Forex forex = getForexDao().getForex(this.portfolioBaseCurrency, balance.getCurrency());

                double tradeValue = forex.getBaseCurrency().equals(this.portfolioBaseCurrency) ? netLiqValueBase : netLiqValue;

                // create the order
                Order order = MarketOrder.Factory.newInstance();
                order.setStrategy(base);

                // if a hedging family is defined for this Forex use it instead of the Forex directly
                int qty;
                if (this.fxFutureEqualizationEnabled) {

                    Subscription forexSubscription = getSubscriptionDao().findByStrategyAndSecurity(StrategyImpl.BASE, forex.getId());
                    if (!forexSubscription.hasProperty("hedgingFamily")) {
                        throw new IllegalStateException("no hedgingFamily defined for forex");
                    }

                    FutureFamily futureFamily = getFutureFamilyDao().load(forexSubscription.getIntProperty("hedgingFamily"));

                    Date targetDate = DateUtils.addMilliseconds(DateUtil.getCurrentEPTime(), this.fxFutureEqualizationMinTimeToExpiration);
                    Future future = getLookupService().getFutureByMinExpiration(futureFamily.getId(), targetDate);

                    // make sure the future is subscriped
                    getMarketDataService().subscribe(base.getName(), future.getId());

                    order.setSecurity(future);

                    // round to the number of contracts
                    qty = (int) MathUtils.round(tradeValue / futureFamily.getContractSize(), 0);

                } else {

                    order.setSecurity(forex);

                    // round to batchSize
                    qty = (int) RoundUtil.roundToNextN(tradeValue, this.fxEqualizationBatchSize);
                }

                if (forex.getBaseCurrency().equals(this.portfolioBaseCurrency)) {

                    // expected case
                    order.setQuantity(Math.abs(qty));
                    order.setSide(qty > 0 ? Side.BUY : Side.SELL);

                } else {

                    // reverse case
                    order.setQuantity(Math.abs(qty));
                    order.setSide(qty > 0 ? Side.SELL : Side.BUY);
                }

                getOrderService().sendOrder(order);

            } else {

                logger.info("no forex equalization is performed on " + balance.getCurrency() + " because amount "
                        + RoundUtil.getBigDecimal(Math.abs(netLiqValueBase)) + " is less than " + this.fxEqualizationMinAmount);
                continue;
            }
        }
    }
}
