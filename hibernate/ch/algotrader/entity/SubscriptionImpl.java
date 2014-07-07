package ch.algotrader.entity;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.property.PropertyImpl;
import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.FeedType;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Base class of an Entity that can hold {@link Property Properties}.
 */
@Entity
@Table(name = "subscription")
public class SubscriptionImpl extends ch.algotrader.entity.property.PropertyHolderImpl implements java.io.Serializable {

    /**
     * The market data feed that this Subscription is valid for.
    */
    private FeedType feedType;
    /**
     * A {@code persistent} Subscription will always be delivered to the strategy (i.e. for SP500). A {@code non-persistent} Subscription requested by a strategy for a specific duration of time (i.e. Options on SP500). When resetting the database before a simulation run, these Subscriptions will be removed
    */
    private boolean persistent;
    /**
     * Represents a running Strategy within the system. In addition the Base is also represented by an
    * instance of this class.
    */
    private StrategyImpl strategy;
    /**
     * The base class of all Securities in the system
    */
    private SecurityImpl security;

    public SubscriptionImpl() {
    }

    public SubscriptionImpl(FeedType feedType, boolean persistent, StrategyImpl strategy, SecurityImpl security) {
        this.feedType = feedType;
        this.persistent = persistent;
        this.strategy = strategy;
        this.security = security;
    }

    public SubscriptionImpl(Map<String, PropertyImpl> props, FeedType feedType, boolean persistent, StrategyImpl strategy, SecurityImpl security) {
        super(props);
        this.feedType = feedType;
        this.persistent = persistent;
        this.strategy = strategy;
        this.security = security;
    }

    /**
     *      * The market data feed that this Subscription is valid for.
     */

    @Column(name = "FEED_TYPE", nullable = false, columnDefinition = "VARCHAR(255)")
    public FeedType getFeedType() {
        return this.feedType;
    }

    public void setFeedType(FeedType feedType) {
        this.feedType = feedType;
    }

    /**
     *      * A {@code persistent} Subscription will always be delivered to the strategy (i.e. for SP500). A {@code non-persistent} Subscription requested by a strategy for a specific duration of time (i.e. Options on SP500). When resetting the database before a simulation run, these Subscriptions will be removed
     */

    @Column(name = "PERSISTENT", nullable = false, columnDefinition = "TINYINT")
    public boolean isPersistent() {
        return this.persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    /**
     *      * Represents a running Strategy within the system. In addition the Base is also represented by an
     * instance of this class.
     */

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "STRATEGY_FK", nullable = false, columnDefinition = "INTEGER")
    public StrategyImpl getStrategy() {
        return this.strategy;
    }

    public void setStrategy(StrategyImpl strategy) {
        this.strategy = strategy;
    }

    /**
     *      * The base class of all Securities in the system
     */

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SECURITY_FK", nullable = false, columnDefinition = "INTEGER")
    public SecurityImpl getSecurity() {
        return this.security;
    }

    public void setSecurity(SecurityImpl security) {
        this.security = security;
    }

}
