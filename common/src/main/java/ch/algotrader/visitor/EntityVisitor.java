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
 * Visitor for entity objects. Usually called as follows:
 * <pre>
 * EntityVisitor<Double, Double> myVisitor = ...
 * double myParam = 1.0;
 * Security security = ...
 * double result = security.accept(myVisitor, myParam);
 * </pre>
 *
 * @param <R>   the result type for entity visits with this visitor
 * @param <P>   the param type passed to the accept method (used by visitor in visit methods)
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface EntityVisitor<R, P> {

    R visitAccount(Account entity, P param);

    R visitAllocation(Allocation entity, P param);

    R visitBar(Bar entity, P param);

    R visitBaseEntity(BaseEntityI entity, P param);

    R visitBond(Bond entity, P param);

    R visitBondFamily(BondFamily entity, P param);

    R visitBrokerParameters(BrokerParameters entity, P param);

    R visitCashBalance(CashBalance entity, P param);

    R visitCombination(Combination entity, P param);

    R visitCommodity(Commodity entity, P param);

    R visitComponent(Component entity, P param);

    R visitEasyToBorrow(EasyToBorrow entity, P param);

    R visitExchange(Exchange entity, P param);

    R visitForex(Forex entity, P param);

    R visitFund(Fund entity, P param);

    R visitFuture(Future entity, P param);

    R visitFutureFamily(FutureFamily entity, P param);

    R visitGenericFuture(GenericFuture entity, P param);

    R visitGenericFutureFamily(GenericFutureFamily entity, P param);

    R visitGenericTick(GenericTick entity, P param);

    R visitHoliday(Holiday entity, P param);

    R visitImpliedVolatility(ImpliedVolatility entity, P param);

    R visitIndex(Index entity, P param);

    R visitIntrestRate(IntrestRate entity, P param);

    R visitLimitOrder(LimitOrder entity, P param);

    R visitMarketDataEvent(MarketDataEvent entity, P param);

    R visitMarketOrder(MarketOrder entity, P param);

    R visitMeasurement(Measurement entity, P param);

    R visitOption(Option entity, P param);

    R visitOptionFamily(OptionFamily entity, P param);

    R visitOrder(Order entity, P param);

    R visitOrderPreference(OrderPreference entity, P param);

    R visitOrderProperty(OrderProperty entity, P param);

    R visitOrderStatus(OrderStatus entity, P param);

    R visitPortfolioValue(PortfolioValue entity, P param);

    R visitPosition(Position entity, P param);

    R visitProperty(Property entity, P param);

    R visitPropertyHolder(PropertyHolder entity, P param);

    R visitSecurity(Security entity, P param);

    R visitSecurityFamily(SecurityFamily entity, P param);

    R visitSimpleOrder(SimpleOrder entity, P param);

    R visitStock(Stock entity, P param);

    R visitStopLimitOrder(StopLimitOrder entity, P param);

    R visitStopOrder(StopOrder entity, P param);

    R visitStrategy(Strategy entity, P param);

    R visitSubscription(Subscription entity, P param);

    R visitTick(Tick entity, P param);

    R visitTradingHours(TradingHours entity, P param);

    R visitTransaction(Transaction entity, P param);

}
