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

import java.util.Collection;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.collection.AbstractPersistentCollection;
import org.hibernate.proxy.HibernateProxy;

import ch.algotrader.cache.CacheManager;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class SecurityImpl extends Security {

    private static final long serialVersionUID = -6631052475125813394L;

    private static Logger logger = Logger.getLogger(SecurityImpl.class.getName());


    @Override
    public boolean isSubscribed() {

        return Hibernate.isInitialized(getSubscriptions()) && (getSubscriptions().size() != 0);
    }

    @Override
    public double getLeverage(double currentValue, double underlyingCurrentValue) {
        return 0;
    }

    /**
     * generic default margin
     */
    @Override
    public double getMargin(double currentValue, double underlyingCurrentValue) {

        double contractSize = getSecurityFamily().getContractSize();
        CommonConfig commonConfig = ConfigLocator.instance().getCommonConfig();
        return currentValue * contractSize / commonConfig.getInitialMarginMarkup().doubleValue();
    }

    @Override
    public String toString() {

        return getSymbol();
    }


    @Override
    public boolean validateTick(Tick tick) {

        // BId / ASK cannot be negative
        if (tick.getBid() != null && tick.getBid().doubleValue() < 0) {
            return false;
        } else if (tick.getAsk() != null && tick.getAsk().doubleValue() < 0) {
            return false;
        }

        // spread cannot be crossed
        CommonConfig commonConfig = ConfigLocator.instance().getCommonConfig();
        if (commonConfig.isValidateCrossedSpread() && tick.getBid() != null && tick.getAsk() != null && tick.getBidAskSpreadDouble() < 0) {
            logger.warn("crossed spread: bid " + tick.getBid() + " ask " + tick.getAsk() + " for " + this);
            return false;
        } else {
            return true;
        }
    }

    private transient boolean initialized = false;

    public boolean isInitialized() {

        return this.initialized;
    }

    @Override
    public void initialize() {

        if (!isInitialized()) {

            // initialize subscriptions before positions because the lazy loaded (= Proxy) Strategy
            // so subscriptions would also get the Proxy insead of the implementation
            long beforeSubscriptions = System.nanoTime();
            Hibernate.initialize(getSubscriptions());
            MetricsUtil.account("Security.subscriptions", (beforeSubscriptions));

            // initialize positions
            long beforePositions = System.nanoTime();
            Hibernate.initialize(getPositions());
            MetricsUtil.account("Security.positions", (beforePositions));

            // initialize underlying
            long beforeUnderlying = System.nanoTime();
            Hibernate.initialize(getUnderlying());
            MetricsUtil.account("Security.underlying", (beforeUnderlying));

            // initialize securityFamily
            long beforeSecurityFamily = System.nanoTime();
            Hibernate.initialize(getSecurityFamily());
            MetricsUtil.account("Security.securityFamily", (beforeSecurityFamily));

            this.initialized = true;
        }
    }

    @Override
    public void initialize(CacheManager cacheManager) {

        if (!isInitialized()) {

            // initialize subscriptions before positions because the lazy loaded (= Proxy) Strategy
            // so subscriptions would also get the Proxy insead of the implementation
            long beforeSubscriptions = System.nanoTime();
            if (this.getSubscriptions() instanceof AbstractPersistentCollection && !((AbstractPersistentCollection) this.getSubscriptions()).wasInitialized()) {
                setSubscriptions((Collection<Subscription>) cacheManager.initialze(this, "subscriptions"));
            }
            MetricsUtil.account("Security.subscriptions", (beforeSubscriptions));


            // initialize positions
            long beforePositions = System.nanoTime();
            if (this.getPositions() instanceof AbstractPersistentCollection && !((AbstractPersistentCollection) this.getPositions()).wasInitialized()) {
                setPositions((Collection<Position>) cacheManager.initialze(this, "positions"));
            }
            MetricsUtil.account("Security.positions", (beforePositions));

            // initialize underlying
            long beforeUnderlying = System.nanoTime();
            if (this.getUnderlying() instanceof HibernateProxy) {
                setUnderlying((Security) cacheManager.initialze(this, "underlying"));
            }
            MetricsUtil.account("Security.underlying", (beforeUnderlying));

            // initialize securityFamily
            long beforeSecurityFamily = System.nanoTime();
            if (this.getSecurityFamily() instanceof HibernateProxy) {
                setSecurityFamily((SecurityFamily) cacheManager.initialze(this, "securityFamily"));
            }
            MetricsUtil.account("Security.securityFamily", (beforeSecurityFamily));

            this.initialized = true;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Security) {
            Security that = (Security) obj;
            return Objects.equals(this.getIsin(), that.getIsin()) &&
                        Objects.equals(this.getBbgid(), that.getBbgid()) &&
                        Objects.equals(this.getRic(), that.getRic()) &&
                        Objects.equals(this.getConid(), that.getConid()) &&
                        Objects.equals(this.getLmaxid(), that.getLmaxid()) &&
                        Objects.equals(this.getSymbol(), that.getSymbol());

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 37 + Objects.hashCode(this.getIsin());
        hash = hash * 37 + Objects.hashCode(this.getBbgid());
        hash = hash * 37 + Objects.hashCode(this.getRic());
        hash = hash * 37 + Objects.hashCode(this.getConid());
        hash = hash * 37 + Objects.hashCode(this.getLmaxid());
        hash = hash * 37 + Objects.hashCode(this.getSymbol());
        return hash;
    }
}
