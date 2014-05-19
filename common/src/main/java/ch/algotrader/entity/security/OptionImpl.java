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
package ch.algotrader.entity.security;

import java.util.Date;

import org.apache.commons.math.MathException;
import org.apache.log4j.Logger;

import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.option.OptionUtil;
import ch.algotrader.util.DateUtil;
import ch.algotrader.util.MyLogger;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class OptionImpl extends Option {

    private static final long serialVersionUID = -3168298592370987085L;
    private static Logger logger = MyLogger.getLogger(OptionImpl.class.getName());

    @Override
    public double getLeverage() {

        try {
            double underlyingSpot = getUnderlying().getCurrentMarketDataEvent().getCurrentValueDouble();
            double currentValue = getCurrentMarketDataEvent().getCurrentValueDouble();
            double delta = OptionUtil.getDelta(this, currentValue, underlyingSpot);

            return underlyingSpot / currentValue * delta;

        } catch (Exception e) {

            return Double.NaN;
        }
    }

    @Override
    public double getMargin() {

        MarketDataEvent optionMarketDataEvent = getCurrentMarketDataEvent();
        MarketDataEvent underlyingMarketDataEvent = getUnderlying().getCurrentMarketDataEvent();

        double marginPerContract = 0;
        if (optionMarketDataEvent != null && underlyingMarketDataEvent != null && optionMarketDataEvent.getCurrentValueDouble() > 0.0) {

            double optionSettlement = optionMarketDataEvent.getCurrentValueDouble();
            double underlyingSettlement = underlyingMarketDataEvent.getCurrentValueDouble();
            double contractSize = getSecurityFamily().getContractSize();
            try {
                marginPerContract = OptionUtil.getMaintenanceMargin(this, optionSettlement, underlyingSettlement) * contractSize;
            } catch (MathException e) {
                logger.warn("could not calculate margin for " + this, e);
            }
        } else {
            logger.warn("no last tick available or currentValue to low to set margin on " + this);
        }
        return marginPerContract;
    }

    @Override
    public long getTimeToExpiration() {

        return getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime();
    }

    @Override
    public int getDuration() {

        OptionFamily family = (OptionFamily) this.getSecurityFamily();
        Date nextExpDate = DateUtil.getExpirationDate(family.getExpirationType(), DateUtil.getCurrentEPTime());
        return 1 + (int) Math.round(((this.getExpiration().getTime() - nextExpDate.getTime()) / (double) family.getExpirationDistance().value()));
    }

    /**
     * make sure expiration is a java.util.Date and not a java.sql.TimeStamp
     */
    @Override
    public Date getExpiration() {

        Date expiration = super.getExpiration();
        if (expiration != null && expiration instanceof java.sql.Timestamp) {
            return new Date(expiration.getTime());
        } else {
            return expiration;
        }
    }

    @Override
    public boolean validateTick(Tick tick) {

        // options need to have an ASK (but might not have a BID just before expiration)
        if (tick.getVolAsk() == 0) {
            return false;
        } else if (tick.getAsk() == null) {
            return false;
        }

        return super.validateTick(tick);
    }
}
