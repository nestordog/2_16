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
 * Provides an implementation for every visit method that calls the visit method of the corresponding parent class
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class PolymorphicSecurityVisitor<R, P> implements SecurityVisitor<R, P> {

    @Override
    public R visitSecurity(Security entity, P param) {
        return null;
    }

    @Override
    public R visitBond(Bond entity, P param) {
        return visitSecurity(entity, param);
    }

    @Override
    public R visitCombination(Combination entity, P param) {
        return visitSecurity(entity, param);
    }

    @Override
    public R visitCommodity(Commodity entity, P param) {
        return visitSecurity(entity, param);
    }

    @Override
    public R visitForex(Forex entity, P param) {
        return visitSecurity(entity, param);
    }

    @Override
    public R visitFund(Fund entity, P param) {
        return visitSecurity(entity, param);
    }

    @Override
    public R visitFuture(Future entity, P param) {
        return visitSecurity(entity, param);
    }

    @Override
    public R visitGenericFuture(GenericFuture entity, P param) {
        return visitSecurity(entity, param);
    }

    @Override
    public R visitIndex(Index entity, P param) {
        return visitSecurity(entity, param);
    }

    @Override
    public R visitIntrestRate(IntrestRate entity, P param) {
        return visitSecurity(entity, param);
    }

    @Override
    public R visitOption(Option entity, P param) {
        return visitSecurity(entity, param);
    }

    @Override
    public R visitStock(Stock entity, P param) {
        return visitSecurity(entity, param);
    }

}
