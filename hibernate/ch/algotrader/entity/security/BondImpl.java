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
@Table(name = "bond")
public class BondImpl extends ch.algotrader.entity.security.SecurityImpl implements java.io.Serializable {

    /**
     * The Maturity Date
    */
    private Date maturity;
    /**
     * The coupon of the Bond specified as a double
    */
    private double coupon;

    public BondImpl() {
    }

    public BondImpl(SecurityFamilyImpl securityFamily, Date maturity, double coupon) {
        super(securityFamily);
        this.maturity = maturity;
        this.coupon = coupon;
    }

    public BondImpl(String symbol, String isin, String bbgid, String ric, String conid, String lmaxid, SecurityImpl underlying, Set<SubscriptionImpl> subscriptions, Set<PositionImpl> positions,
            SecurityFamilyImpl securityFamily, Date maturity, double coupon) {
        super(symbol, isin, bbgid, ric, conid, lmaxid, underlying, subscriptions, positions, securityFamily);
        this.maturity = maturity;
        this.coupon = coupon;
    }

    /**
     *      * The Maturity Date
     */

    @Column(name = "MATURITY", nullable = false, columnDefinition = "DATETIME")
    public Date getMaturity() {
        return this.maturity;
    }

    public void setMaturity(Date maturity) {
        this.maturity = maturity;
    }

    /**
     *      * The coupon of the Bond specified as a double
     */

    @Column(name = "COUPON", unique = true, nullable = false, columnDefinition = "DOUBLE")
    public double getCoupon() {
        return this.coupon;
    }

    public void setCoupon(double coupon) {
        this.coupon = coupon;
    }

}
