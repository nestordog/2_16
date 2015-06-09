/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.visitor;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.BaseEntityI;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.exchange.Holiday;
import ch.algotrader.entity.exchange.TradingHours;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.GenericTick;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.property.Property;
import ch.algotrader.entity.property.PropertyHolder;
import ch.algotrader.entity.security.Bond;
import ch.algotrader.entity.security.BondFamily;
import ch.algotrader.entity.security.BrokerParameters;
import ch.algotrader.entity.security.Combination;
import ch.algotrader.entity.security.Commodity;
import ch.algotrader.entity.security.Component;
import ch.algotrader.entity.security.EasyToBorrow;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Fund;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.GenericFuture;
import ch.algotrader.entity.security.GenericFutureFamily;
import ch.algotrader.entity.security.ImpliedVolatility;
import ch.algotrader.entity.security.Index;
import ch.algotrader.entity.security.IntrestRate;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityReference;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.Measurement;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Allocation;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderPreference;
import ch.algotrader.entity.trade.OrderProperty;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopOrder;


/**
 * Provides a no-OP implementation for every visit method.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultEntityVisitor<R, P> implements EntityVisitor<R, P> {

    @Override
    public R visitAccount(Account entity, P param) {
        return null;
    }

    @Override
    public R visitAllocation(Allocation entity, P param) {
        return null;
    }

    @Override
    public R visitBar(Bar entity, P param) {
        return null;
    }

    @Override
    public R visitBaseEntity(BaseEntityI entity, P param) {
        return null;
    }

    @Override
    public R visitBond(Bond entity, P param) {
        return null;
    }

    @Override
    public R visitBondFamily(BondFamily entity, P param) {
        return null;
    }

    @Override
    public R visitBrokerParameters(BrokerParameters entity, P param) {
        return null;
    }

    @Override
    public R visitCashBalance(CashBalance entity, P param) {
        return null;
    }

    @Override
    public R visitCombination(Combination entity, P param) {
        return null;
    }

    @Override
    public R visitCommodity(Commodity entity, P param) {
        return null;
    }

    @Override
    public R visitComponent(Component entity, P param) {
        return null;
    }

    @Override
    public R visitEasyToBorrow(EasyToBorrow entity, P param) {
        return null;
    }

    @Override
    public R visitExchange(Exchange entity, P param) {
        return null;
    }

    @Override
    public R visitForex(Forex entity, P param) {
        return null;
    }

    @Override
    public R visitFund(Fund entity, P param) {
        return null;
    }

    @Override
    public R visitFuture(Future entity, P param) {
        return null;
    }

    @Override
    public R visitFutureFamily(FutureFamily entity, P param) {
        return null;
    }

    @Override
    public R visitGenericFuture(GenericFuture entity, P param) {
        return null;
    }

    @Override
    public R visitGenericFutureFamily(GenericFutureFamily entity, P param) {
        return null;
    }

    @Override
    public R visitGenericTick(GenericTick entity, P param) {
        return null;
    }

    @Override
    public R visitHoliday(Holiday entity, P param) {
        return null;
    }

    @Override
    public R visitImpliedVolatility(ImpliedVolatility entity, P param) {
        return null;
    }

    @Override
    public R visitIndex(Index entity, P param) {
        return null;
    }

    @Override
    public R visitIntrestRate(IntrestRate entity, P param) {
        return null;
    }

    @Override
    public R visitLimitOrder(LimitOrder entity, P param) {
        return null;
    }

    @Override
    public R visitMarketDataEvent(MarketDataEvent entity, P param) {
        return null;
    }

    @Override
    public R visitMarketOrder(MarketOrder entity, P param) {
        return null;
    }

    @Override
    public R visitMeasurement(Measurement entity, P param) {
        return null;
    }

    @Override
    public R visitOption(Option entity, P param) {
        return null;
    }

    @Override
    public R visitOptionFamily(OptionFamily entity, P param) {
        return null;
    }

    @Override
    public R visitOrder(Order entity, P param) {
        return null;
    }

    @Override
    public R visitOrderPreference(OrderPreference entity, P param) {
        return null;
    }

    @Override
    public R visitOrderProperty(OrderProperty entity, P param) {
        return null;
    }

    @Override
    public R visitOrderStatus(OrderStatus entity, P param) {
        return null;
    }

    @Override
    public R visitPortfolioValue(PortfolioValue entity, P param) {
        return null;
    }

    @Override
    public R visitPosition(Position entity, P param) {
        return null;
    }

    @Override
    public R visitProperty(Property entity, P param) {
        return null;
    }

    @Override
    public R visitPropertyHolder(PropertyHolder entity, P param) {
        return null;
    }

    @Override
    public R visitSecurity(Security entity, P param) {
        return null;
    }

    @Override
    public R visitSecurityFamily(SecurityFamily entity, P param) {
        return null;
    }

    @Override
    public R visitSecurityReference(SecurityReference entity, P param) {
        return null;
    }

    @Override
    public R visitSimpleOrder(SimpleOrder entity, P param) {
        return null;
    }

    @Override
    public R visitStock(Stock entity, P param) {
        return null;
    }

    @Override
    public R visitStopLimitOrder(StopLimitOrder entity, P param) {
        return null;
    }

    @Override
    public R visitStopOrder(StopOrder entity, P param) {
        return null;
    }

    @Override
    public R visitStrategy(Strategy entity, P param) {
        return null;
    }

    @Override
    public R visitSubscription(Subscription entity, P param) {
        return null;
    }

    @Override
    public R visitTick(Tick entity, P param) {
        return null;
    }

    @Override
    public R visitTradingHours(TradingHours entity, P param) {
        return null;
    }

    @Override
    public R visitTransaction(Transaction entity, P param) {
        return null;
    }

}
