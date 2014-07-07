package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.enumeration.IndexType;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * The base class of all Securities in the system
 */
@Entity
@Table(name = "index")
public class IndexImpl extends ch.algotrader.entity.security.SecurityImpl implements java.io.Serializable {

    private IndexType type;

    public IndexImpl() {
    }

    public IndexImpl(SecurityFamilyImpl securityFamily, IndexType type) {
        super(securityFamily);
        this.type = type;
    }

    public IndexImpl(String symbol, String isin, String bbgid, String ric, String conid, String lmaxid, SecurityImpl underlying, Set<SubscriptionImpl> subscriptions, Set<PositionImpl> positions,
            SecurityFamilyImpl securityFamily, IndexType type) {
        super(symbol, isin, bbgid, ric, conid, lmaxid, underlying, subscriptions, positions, securityFamily);
        this.type = type;
    }

    @Column(name = "TYPE", nullable = false, columnDefinition = "VARCHAR(255)")
    public IndexType getType() {
        return this.type;
    }

    public void setType(IndexType type) {
        this.type = type;
    }

}
