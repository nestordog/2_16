package com.algoTrader.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.security.Forex;
import com.algoTrader.entity.trade.MarketOrder;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.Side;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.BalanceVO;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class ForexServiceImpl extends ForexServiceBase {

    private static Currency portfolioBaseCurrency = Currency.fromString(ConfigurationUtil.getBaseConfig().getString("portfolioBaseCurrency"));
    private static int fxEqualizationMinAmount = ConfigurationUtil.getBaseConfig().getInt("fxEqualizationMinAmount");
    private static int fxEqualizationBatchSize = ConfigurationUtil.getBaseConfig().getInt("fxEqualizationBatchSize");

    private static Logger logger = MyLogger.getLogger(ForexServiceImpl.class.getName());

    @Override
    @SuppressWarnings("unchecked")
    protected void handleEqualizeForex() throws Exception {

        Strategy base = getStrategyDao().findByName(StrategyImpl.BASE);

        List<BalanceVO> balances = getStrategyDao().getPortfolioBalances();
        for (BalanceVO balance : balances) {

            if (balance.getCurrency().equals(portfolioBaseCurrency)) {
                continue;
            }

            // netLiqValueBase
            double netLiqValue = balance.getNetLiqValue().doubleValue();
            double netLiqValueBase = balance.getExchangeRate() * netLiqValue;

            // check if amount is larger than minimum
            if (Math.abs(netLiqValueBase) >= fxEqualizationMinAmount) {

                Forex forex = getForexDao().getForex(portfolioBaseCurrency, balance.getCurrency());

                Order order = MarketOrder.Factory.newInstance();
                if (forex.getBaseCurrency().equals(portfolioBaseCurrency)) {

                    // expected case
                    int qty = (int) RoundUtil.roundToNextN(netLiqValueBase, fxEqualizationBatchSize);
                    order.setQuantity(Math.abs(qty));
                    order.setSide(qty > 0 ? Side.BUY : Side.SELL);

                } else {

                    // reverse case
                    int qty = (int) RoundUtil.roundToNextN(netLiqValue, fxEqualizationBatchSize);
                    order.setQuantity(Math.abs(qty));
                    order.setSide(qty > 0 ? Side.SELL : Side.BUY);
                }

                order.setStrategy(base);
                order.setSecurity(forex);

                getOrderService().sendOrder(order);
            }
        }
    }

    public static class EqualizeForexListener implements UpdateListener {

        @Override
        public void update(EventBean[] newEvents, EventBean[] oldEvents) {

            long startTime = System.currentTimeMillis();
            logger.debug("equalizeForex start");

            ServiceLocator.serverInstance().getForexService().equalizeForex();

            logger.debug("equalizeForex end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
        }
    }
}
