package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.SubscriptionImpl;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * The base class of all Securities in the system
 */
@Entity
@Table(name = "stock")
public class StockImpl extends ch.algotrader.entity.security.SecurityImpl implements java.io.Serializable {

    private String gics;

    public StockImpl() {
    }

    public StockImpl(SecurityFamilyImpl securityFamily) {
        super(securityFamily);
    }

    public StockImpl(String symbol, String isin, String bbgid, String ric, String conid, String lmaxid, SecurityImpl underlying, Set<SubscriptionImpl> subscriptions, Set<PositionImpl> positions,
            SecurityFamilyImpl securityFamily, String gics) {
        super(symbol, isin, bbgid, ric, conid, lmaxid, underlying, subscriptions, positions, securityFamily);
        this.gics = gics;
    }

    @Column(name = "GICS", columnDefinition = "VARCHAR(255)")
    public String getGics() {
        return this.gics;
    }

    public void setGics(String gics) {
        this.gics = gics;
    }

}
