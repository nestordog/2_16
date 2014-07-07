package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Represents one Component of a {@link Combination}.
 */
@Entity
@Table(name = "component")
public class ComponentImpl implements java.io.Serializable {

    private int id;
    private int version;
    private long quantity;
    /**
     * If set to {@code false}, the Component will be removed when resetting the database before a simulation run.
    */
    private boolean persistent;
    /**
     * A synthetic security composed of one or many {@link Component Components}.
    */
    private CombinationImpl combination;
    /**
     * The base class of all Securities in the system
    */
    private SecurityImpl security;

    public ComponentImpl() {
    }

    public ComponentImpl(long quantity, boolean persistent, CombinationImpl combination, SecurityImpl security) {
        this.quantity = quantity;
        this.persistent = persistent;
        this.combination = combination;
        this.security = security;
    }

    @Id
    @GeneratedValue
    @Column(name = "ID", nullable = false, columnDefinition = "INTEGER")
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION", nullable = false)
    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Column(name = "QUANTITY", nullable = false, columnDefinition = "BIGINT")
    public long getQuantity() {
        return this.quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    /**
     *      * If set to {@code false}, the Component will be removed when resetting the database before a simulation run.
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMBINATION_FK", nullable = false, columnDefinition = "INTEGER")
    public CombinationImpl getCombination() {
        return this.combination;
    }

    public void setCombination(CombinationImpl combination) {
        this.combination = combination;
    }

    /**
     *      * The base class of all Securities in the system
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECURITY_FK", nullable = false, columnDefinition = "INTEGER")
    public SecurityImpl getSecurity() {
        return this.security;
    }

    public void setSecurity(SecurityImpl security) {
        this.security = security;
    }

}
