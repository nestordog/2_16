package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.enumeration.CommodityType;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * The base class of all Securities in the system
 */
@Entity
@Table(name = "commodity")
public class CommodityImpl extends ch.algotrader.entity.security.SecurityImpl implements java.io.Serializable {

    private CommodityType type;

    public CommodityImpl() {
    }

    public CommodityImpl(SecurityFamilyImpl securityFamily, CommodityType type) {
        super(securityFamily);
        this.type = type;
    }

    public CommodityImpl(String symbol, String isin, String bbgid, String ric, String conid, String lmaxid, SecurityImpl underlying, Set<SubscriptionImpl> subscriptions, Set<PositionImpl> positions,
            SecurityFamilyImpl securityFamily, CommodityType type) {
        super(symbol, isin, bbgid, ric, conid, lmaxid, underlying, subscriptions, positions, securityFamily);
        this.type = type;
    }

    @Column(name = "TYPE", nullable = false, columnDefinition = "VARCHAR(255)")
    public CommodityType getType() {
        return this.type;
    }

    public void setType(CommodityType type) {
        this.type = type;
    }

}
