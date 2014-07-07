package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.enumeration.Duration;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * The base class of all Securities in the system
 */
@Entity
@Table(name = "generic_future")
public class GenericFutureImpl extends ch.algotrader.entity.security.SecurityImpl implements java.io.Serializable {

    /**
     * The Duration of this GenericFuture. A Duration of 1 means that this GenericFuture is equal to the physical Front Month Future. After the Expiration Date of the physical Future, the GenericFuture with Duration 1 (that until now represented the expiring Future) now switches to represent the Future that is next to expire.
    */
    private Duration duration;

    public GenericFutureImpl() {
    }

    public GenericFutureImpl(SecurityFamilyImpl securityFamily, Duration duration) {
        super(securityFamily);
        this.duration = duration;
    }

    public GenericFutureImpl(String symbol, String isin, String bbgid, String ric, String conid, String lmaxid, SecurityImpl underlying, Set<SubscriptionImpl> subscriptions,
            Set<PositionImpl> positions, SecurityFamilyImpl securityFamily, Duration duration) {
        super(symbol, isin, bbgid, ric, conid, lmaxid, underlying, subscriptions, positions, securityFamily);
        this.duration = duration;
    }

    /**
     *      * The Duration of this GenericFuture. A Duration of 1 means that this GenericFuture is equal to the physical Front Month Future. After the Expiration Date of the physical Future, the GenericFuture with Duration 1 (that until now represented the expiring Future) now switches to represent the Future that is next to expire.
     */

    @Column(name = "DURATION", nullable = false, columnDefinition = "BIGINT")
    public Duration getDuration() {
        return this.duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

}
