/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.visitor;

import ch.algotrader.entity.security.Bond;
import ch.algotrader.entity.security.Combination;
import ch.algotrader.entity.security.Commodity;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Fund;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.GenericFuture;
import ch.algotrader.entity.security.Index;
import ch.algotrader.entity.security.IntrestRate;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.Stock;

/**
 * Visitor for security entities. Usually called as follows:
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
 */
public interface SecurityVisitor<R, P> {

    R visitSecurity(Security entity, P param);

    R visitBond(Bond entity, P param);

    R visitCombination(Combination entity, P param);

    R visitCommodity(Commodity entity, P param);

    R visitForex(Forex entity, P param);

    R visitFund(Fund entity, P param);

    R visitFuture(Future entity, P param);

    R visitGenericFuture(GenericFuture entity, P param);

    R visitIndex(Index entity, P param);

    R visitIntrestRate(IntrestRate entity, P param);

    R visitOption(Option entity, P param);

    R visitStock(Stock entity, P param);

}
