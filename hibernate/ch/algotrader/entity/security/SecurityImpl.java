package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.SubscriptionImpl;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The base class of all Securities in the system
 */
@Entity
@Table(name = "security")
public class SecurityImpl implements java.io.Serializable {

    private int id;
    private String symbol;
    /**
     * International Securities Identification Number
    */
    private String isin;
    private String bbgid;
    /**
     * Reuters Instrument Code
    */
    private String ric;
    /**
     * Interactive Brokers conid
    */
    private String conid;
    private String lmaxid;
    /**
     * The base class of all Securities in the system
    */
    private SecurityImpl underlying;
    /**
     * The base class of all Securities in the system
    */
    private Set<SubscriptionImpl> subscriptions = new HashSet<SubscriptionImpl>(0);
    /**
     * The base class of all Securities in the system
    */
    private Set<PositionImpl> positions = new HashSet<PositionImpl>(0);
    /**
     * Represents an entire family of similar {@link ch.algotrader.entity.security.Security Securities}
    * (e.g. All Options of the SP500)
    */
    private SecurityFamilyImpl securityFamily;

    public SecurityImpl() {
    }

    public SecurityImpl(SecurityFamilyImpl securityFamily) {
        this.securityFamily = securityFamily;
    }

    public SecurityImpl(String symbol, String isin, String bbgid, String ric, String conid, String lmaxid, SecurityImpl underlying, Set<SubscriptionImpl> subscriptions, Set<PositionImpl> positions,
            SecurityFamilyImpl securityFamily) {
        this.symbol = symbol;
        this.isin = isin;
        this.bbgid = bbgid;
        this.ric = ric;
        this.conid = conid;
        this.lmaxid = lmaxid;
        this.underlying = underlying;
        this.subscriptions = subscriptions;
        this.positions = positions;
        this.securityFamily = securityFamily;
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

    @Column(name = "SYMBOL", unique = true, columnDefinition = "VARCHAR(255)")
    public String getSymbol() {
        return this.symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     *      * International Securities Identification Number
     */

    @Column(name = "ISIN", unique = true, columnDefinition = "VARCHAR(255)")
    public String getIsin() {
        return this.isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    @Column(name = "BBGID", unique = true, columnDefinition = "VARCHAR(255)")
    public String getBbgid() {
        return this.bbgid;
    }

    public void setBbgid(String bbgid) {
        this.bbgid = bbgid;
    }

    /**
     *      * Reuters Instrument Code
     */

    @Column(name = "RIC", unique = true, columnDefinition = "VARCHAR(255)")
    public String getRic() {
        return this.ric;
    }

    public void setRic(String ric) {
        this.ric = ric;
    }

    /**
     *      * Interactive Brokers conid
     */

    @Column(name = "CONID", unique = true, columnDefinition = "VARCHAR(255)")
    public String getConid() {
        return this.conid;
    }

    public void setConid(String conid) {
        this.conid = conid;
    }

    @Column(name = "LMAXID", unique = true, columnDefinition = "VARCHAR(255)")
    public String getLmaxid() {
        return this.lmaxid;
    }

    public void setLmaxid(String lmaxid) {
        this.lmaxid = lmaxid;
    }

    /**
     *      * The base class of all Securities in the system
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UNDERLYING_FK", columnDefinition = "INTEGER")
    public SecurityImpl getUnderlying() {
        return this.underlying;
    }

    public void setUnderlying(SecurityImpl underlying) {
        this.underlying = underlying;
    }

    /**
     *      * The base class of all Securities in the system
     */

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "security")
    public Set<SubscriptionImpl> getSubscriptions() {
        return this.subscriptions;
    }

    public void setSubscriptions(Set<SubscriptionImpl> subscriptions) {
        this.subscriptions = subscriptions;
    }

    /**
     *      * The base class of all Securities in the system
     */

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "security")
    public Set<PositionImpl> getPositions() {
        return this.positions;
    }

    public void setPositions(Set<PositionImpl> positions) {
        this.positions = positions;
    }

    /**
     *      * Represents an entire family of similar {@link ch.algotrader.entity.security.Security Securities}
     * (e.g. All Options of the SP500)
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECURITY_FAMILY_FK", nullable = false, columnDefinition = "INTEGER")
    public SecurityFamilyImpl getSecurityFamily() {
        return this.securityFamily;
    }

    public void setSecurityFamily(SecurityFamilyImpl securityFamily) {
        this.securityFamily = securityFamily;
    }

}
