package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.enumeration.CombinationType;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The base class of all Securities in the system
 */
@Entity
@Table(name = "combination")
public class CombinationImpl extends ch.algotrader.entity.security.SecurityImpl implements java.io.Serializable {

    /**
     * auto generated unique identifier. Combinations do not have any other natural identifiers.
    */
    private String uuid;
    /**
     * The type of the Combination (e.g. Butterfly, Condor, RatioSpread, etc.)
    */
    private CombinationType type;
    /**
     * If set to {@code false}, the Combination will be removed when resetting the database before a simulation run.
    */
    private boolean persistent;
    /**
     * A synthetic security composed of one or many {@link Component Components}.
    */
    private Set<ComponentImpl> components = new HashSet<ComponentImpl>(0);

    public CombinationImpl() {
    }

    public CombinationImpl(SecurityFamilyImpl securityFamily, String uuid, CombinationType type, boolean persistent) {
        super(securityFamily);
        this.uuid = uuid;
        this.type = type;
        this.persistent = persistent;
    }

    public CombinationImpl(String symbol, String isin, String bbgid, String ric, String conid, String lmaxid, SecurityImpl underlying, Set<SubscriptionImpl> subscriptions,
            Set<PositionImpl> positions, SecurityFamilyImpl securityFamily, String uuid, CombinationType type, boolean persistent, Set<ComponentImpl> components) {
        super(symbol, isin, bbgid, ric, conid, lmaxid, underlying, subscriptions, positions, securityFamily);
        this.uuid = uuid;
        this.type = type;
        this.persistent = persistent;
        this.components = components;
    }

    /**
     *      * auto generated unique identifier. Combinations do not have any other natural identifiers.
     */

    @Column(name = "UUID", unique = true, nullable = false, columnDefinition = "VARCHAR(255)")
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     *      * The type of the Combination (e.g. Butterfly, Condor, RatioSpread, etc.)
     */

    @Column(name = "TYPE", nullable = false, columnDefinition = "VARCHAR(255)")
    public CombinationType getType() {
        return this.type;
    }

    public void setType(CombinationType type) {
        this.type = type;
    }

    /**
     *      * If set to {@code false}, the Combination will be removed when resetting the database before a simulation run.
     */

    @Column(name = "PERSISTENT", nullable = false, columnDefinition = "TINYINT")
    public boolean isPersistent() {
        return this.persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    /**
     *      * A synthetic security composed of one or many {@link Component Components}.
     */

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "combination")
    public Set<ComponentImpl> getComponents() {
        return this.components;
    }

    public void setComponents(Set<ComponentImpl> components) {
        this.components = components;
    }

}
