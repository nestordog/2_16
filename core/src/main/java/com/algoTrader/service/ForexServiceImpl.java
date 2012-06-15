package com.algoTrader.service;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.security.Forex;
import com.algoTrader.entity.trade.MarketOrder;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.Side;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.BalanceVO;

public class ForexServiceImpl extends ForexServiceBase {

    private static Logger logger = MyLogger.getLogger(ForexServiceImpl.class.getName());

    private @Value("#{T(com.algoTrader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;
    private @Value("${misc.fxEqualizationMinAmount}") int fxEqualizationMinAmount;
    private @Value("${misc.fxEqualizationBatchSize}") int fxEqualizationBatchSize;

    @Override
    protected void handleEqualizeForex() throws Exception {

        Strategy base = getStrategyDao().findByName(StrategyImpl.BASE);

        Collection<BalanceVO> balances = getPortfolioService().getBalances();
        for (BalanceVO balance : balances) {

            if (balance.getCurrency().equals(this.portfolioBaseCurrency)) {
                continue;
            }

            // netLiqValueBase
            double netLiqValue = balance.getNetLiqValue().doubleValue();
            double netLiqValueBase = balance.getExchangeRate() * netLiqValue;

            // check if amount is larger than minimum
            if (Math.abs(netLiqValueBase) >= this.fxEqualizationMinAmount) {

                Forex forex = getForexDao().getForex(this.portfolioBaseCurrency, balance.getCurrency());

                Order order = MarketOrder.Factory.newInstance();
                if (forex.getBaseCurrency().equals(this.portfolioBaseCurrency)) {

                    // expected case
                    int qty = (int) RoundUtil.roundToNextN(netLiqValueBase, this.fxEqualizationBatchSize);
                    order.setQuantity(Math.abs(qty));
                    order.setSide(qty > 0 ? Side.BUY : Side.SELL);

                } else {

                    // reverse case
                    int qty = (int) RoundUtil.roundToNextN(netLiqValue, this.fxEqualizationBatchSize);
                    order.setQuantity(Math.abs(qty));
                    order.setSide(qty > 0 ? Side.SELL : Side.BUY);
                }

                order.setStrategy(base);
                order.setSecurity(forex);

                getOrderService().sendOrder(order);

            } else {

                logger.info("no forex equalization is performed on " + balance.getCurrency() + " because amount "
                        + RoundUtil.getBigDecimal(Math.abs(netLiqValueBase)) + " is less than " + this.fxEqualizationMinAmount);
                continue;
            }
        }
    }
}
