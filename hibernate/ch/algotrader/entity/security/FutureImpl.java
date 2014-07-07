package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.SubscriptionImpl;
import java.util.Date;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * The base class of all Securities in the system
 */
@Entity
@Table(name = "future")
public class FutureImpl extends ch.algotrader.entity.security.SecurityImpl implements java.io.Serializable {

    /**
     * The Expiration Date
    */
    private Date expiration;
    /**
     * The first notice date
    */
    private Date firstNotice;
    /**
     * The last trading date
    */
    private Date lastTrading;

    public FutureImpl() {
    }

    public FutureImpl(SecurityFamilyImpl securityFamily, Date expiration) {
        super(securityFamily);
        this.expiration = expiration;
    }

    public FutureImpl(String symbol, String isin, String bbgid, String ric, String conid, String lmaxid, SecurityImpl underlying, Set<SubscriptionImpl> subscriptions, Set<PositionImpl> positions,
            SecurityFamilyImpl securityFamily, Date expiration, Date firstNotice, Date lastTrading) {
        super(symbol, isin, bbgid, ric, conid, lmaxid, underlying, subscriptions, positions, securityFamily);
        this.expiration = expiration;
        this.firstNotice = firstNotice;
        this.lastTrading = lastTrading;
    }

    /**
     *      * The Expiration Date
     */

    @Column(name = "EXPIRATION", nullable = false, columnDefinition = "DATETIME")
    public Date getExpiration() {
        return this.expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    /**
     *      * The first notice date
     */

    @Column(name = "FIRST_NOTICE", columnDefinition = "DATETIME")
    public Date getFirstNotice() {
        return this.firstNotice;
    }

    public void setFirstNotice(Date firstNotice) {
        this.firstNotice = firstNotice;
    }

    /**
     *      * The last trading date
     */

    @Column(name = "LAST_TRADING", columnDefinition = "DATETIME")
    public Date getLastTrading() {
        return this.lastTrading;
    }

    public void setLastTrading(Date lastTrading) {
        this.lastTrading = lastTrading;
    }

}
