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
@Table(name = "intrest_rate")
public class IntrestRateImpl extends ch.algotrader.entity.security.SecurityImpl implements java.io.Serializable {

    private Duration duration;

    public IntrestRateImpl() {
    }

    public IntrestRateImpl(SecurityFamilyImpl securityFamily, Duration duration) {
        super(securityFamily);
        this.duration = duration;
    }

    public IntrestRateImpl(String symbol, String isin, String bbgid, String ric, String conid, String lmaxid, SecurityImpl underlying, Set<SubscriptionImpl> subscriptions,
            Set<PositionImpl> positions, SecurityFamilyImpl securityFamily, Duration duration) {
        super(symbol, isin, bbgid, ric, conid, lmaxid, underlying, subscriptions, positions, securityFamily);
        this.duration = duration;
    }

    @Column(name = "DURATION", nullable = false, columnDefinition = "BIGINT")
    public Duration getDuration() {
        return this.duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

}
