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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.Index;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.Stock;

/**
 * An EntityVistor used to validate ticks on a per-entity-basis
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TickValidationVisitor extends PolymorphicEntityVisitor<Boolean, Tick> {

    public static final TickValidationVisitor INSTANCE = new TickValidationVisitor();

    private static final Logger LOGGER = LogManager.getLogger(TickValidationVisitor.class);

    private TickValidationVisitor() {
    }

    public TickValidationVisitor instance() {
        return INSTANCE;
    }

    @Override
    public Boolean visitSecurity(Security entity, Tick tick) {

        // BId / ASK cannot be negative
        if (tick.getBid() != null && tick.getBid().doubleValue() < 0) {
            return false;
        } else if (tick.getAsk() != null && tick.getAsk().doubleValue() < 0) {
            return false;
        }

        // spread cannot be crossed
        CommonConfig commonConfig = ConfigLocator.instance().getCommonConfig();
        if (commonConfig.isValidateCrossedSpread() && tick.getBid() != null && tick.getAsk() != null && tick.getBidAskSpreadDouble() < 0) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("crossed spread: bid {} ask {} for {}", tick.getBid(), tick.getAsk(), this);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Boolean visitOption(Option entity, Tick tick) {

        // options need to have an ASK (but might not have a BID just before expiration)
        if (tick.getVolAsk() == 0) {
            return false;
        } else if (tick.getAsk() == null) {
            return false;
        }

        return super.visitOption(entity, tick);
    }

    @Override
    public Boolean visitFuture(Future entity, Tick tick) {

        // futures need to have a BID and ASK
        if (tick.getBid() == null) {
            return false;
        } else if (tick.getVolBid() == 0) {
            return false;
        } else if (tick.getAsk() == null) {
            return false;
        }
        if (tick.getVolAsk() == 0) {
            return false;
        }

        return super.visitFuture(entity, tick);
    }

    @Override
    public Boolean visitStock(Stock entity, Tick tick) {

        // stocks need to have a BID and ASK
        if (tick.getBid() == null) {
            return false;
        } else if (tick.getVolBid() == 0) {
            return false;
        } else if (tick.getAsk() == null) {
            return false;
        }
        if (tick.getVolAsk() == 0) {
            return false;
        }

        return super.visitStock(entity, tick);
    }

    @Override
    public Boolean visitIndex(Index entity, Tick tick) {

        if (tick.getLast() == null) {
            return false;
        } else if (tick.getLastDateTime() == null) {
            return false;
        } else {
            return true;
        }
    }

}
