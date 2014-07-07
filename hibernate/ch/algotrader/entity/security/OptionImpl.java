package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.enumeration.OptionType;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * The base class of all Securities in the system
 */
@Entity
@Table(name = "option")
public class OptionImpl extends ch.algotrader.entity.security.SecurityImpl implements java.io.Serializable {

    /**
     * The Expiration Date
    */
    private Date expiration;
    /**
     * The strike price.
    */
    private BigDecimal strike;
    /**
     * The {@link OptionType} (i.e. Put or Call)
    */
    private OptionType type;

    public OptionImpl() {
    }

    public OptionImpl(SecurityFamilyImpl securityFamily, Date expiration, BigDecimal strike, OptionType type) {
        super(securityFamily);
        this.expiration = expiration;
        this.strike = strike;
        this.type = type;
    }

    public OptionImpl(String symbol, String isin, String bbgid, String ric, String conid, String lmaxid, SecurityImpl underlying, Set<SubscriptionImpl> subscriptions, Set<PositionImpl> positions,
            SecurityFamilyImpl securityFamily, Date expiration, BigDecimal strike, OptionType type) {
        super(symbol, isin, bbgid, ric, conid, lmaxid, underlying, subscriptions, positions, securityFamily);
        this.expiration = expiration;
        this.strike = strike;
        this.type = type;
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
     *      * The strike price.
     */

    @Column(name = "STRIKE", nullable = false, columnDefinition = "Decimal(15,6)")
    public BigDecimal getStrike() {
        return this.strike;
    }

    public void setStrike(BigDecimal strike) {
        this.strike = strike;
    }

    /**
     *      * The {@link OptionType} (i.e. Put or Call)
     */

    @Column(name = "TYPE", nullable = false, columnDefinition = "VARCHAR(255)")
    public OptionType getType() {
        return this.type;
    }

    public void setType(OptionType type) {
        this.type = type;
    }

}
