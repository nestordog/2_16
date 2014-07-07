package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.enumeration.Currency;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * The base class of all Securities in the system
 */
@Entity
@Table(name = "forex")
public class ForexImpl extends ch.algotrader.entity.security.SecurityImpl implements java.io.Serializable {

    /**
     * The Base Currency of this Forex Contract (e.g. EUR for the EUR.USD Forex)
    */
    private Currency baseCurrency;

    public ForexImpl() {
    }

    public ForexImpl(SecurityFamilyImpl securityFamily, Currency baseCurrency) {
        super(securityFamily);
        this.baseCurrency = baseCurrency;
    }

    public ForexImpl(String symbol, String isin, String bbgid, String ric, String conid, String lmaxid, SecurityImpl underlying, Set<SubscriptionImpl> subscriptions, Set<PositionImpl> positions,
            SecurityFamilyImpl securityFamily, Currency baseCurrency) {
        super(symbol, isin, bbgid, ric, conid, lmaxid, underlying, subscriptions, positions, securityFamily);
        this.baseCurrency = baseCurrency;
    }

    /**
     *      * The Base Currency of this Forex Contract (e.g. EUR for the EUR.USD Forex)
     */

    @Column(name = "BASE_CURRENCY", nullable = false, columnDefinition = "VARCHAR(255)")
    public Currency getBaseCurrency() {
        return this.baseCurrency;
    }

    public void setBaseCurrency(Currency baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

}
