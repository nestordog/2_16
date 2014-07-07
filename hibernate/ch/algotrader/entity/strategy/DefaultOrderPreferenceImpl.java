package ch.algotrader.entity.strategy;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.security.SecurityFamilyImpl;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Assignment of an {@link OrderPreference} and a {@link ch.algotrader.entity.security.SecurityFamily SecurityFamily} to a individual strategy. This is useful for situations where the Base has to send an order regarding a {@link ch.algotrader.entity.Position Position} that belongs to a Strategy (e.g. ClosePosition when the ExitValue is reached).
 */
@Entity
@Table(name = "default_order_preference")
public class DefaultOrderPreferenceImpl implements java.io.Serializable {

    private int id;
    /**
     * Contains certain order default values (e.g. quantity, orderType, delays, etc.). Except for the
    * {@link ch.algotrader.enumeration.OrderType OrderType}, all values have to be defined through
    * Properties.
    */
    private OrderPreferenceImpl orderPreference;
    /**
     * Represents an entire family of similar {@link ch.algotrader.entity.security.Security Securities}
    * (e.g. All Options of the SP500)
    */
    private SecurityFamilyImpl securityFamily;
    /**
     * Represents a running Strategy within the system. In addition the Base is also represented by an
    * instance of this class.
    */
    private StrategyImpl strategy;

    public DefaultOrderPreferenceImpl() {
    }

    public DefaultOrderPreferenceImpl(OrderPreferenceImpl orderPreference, SecurityFamilyImpl securityFamily, StrategyImpl strategy) {
        this.orderPreference = orderPreference;
        this.securityFamily = securityFamily;
        this.strategy = strategy;
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

    /**
     *      * Contains certain order default values (e.g. quantity, orderType, delays, etc.). Except for the
     * {@link ch.algotrader.enumeration.OrderType OrderType}, all values have to be defined through
     * Properties.
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_PREFERENCE_FK", nullable = false, columnDefinition = "INTEGER")
    public OrderPreferenceImpl getOrderPreference() {
        return this.orderPreference;
    }

    public void setOrderPreference(OrderPreferenceImpl orderPreference) {
        this.orderPreference = orderPreference;
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

    /**
     *      * Represents a running Strategy within the system. In addition the Base is also represented by an
     * instance of this class.
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STRATEGY_FK", nullable = false, columnDefinition = "INTEGER")
    public StrategyImpl getStrategy() {
        return this.strategy;
    }

    public void setStrategy(StrategyImpl strategy) {
        this.strategy = strategy;
    }

}
