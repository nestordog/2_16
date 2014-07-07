package ch.algotrader.entity.strategy;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.AccountImpl;
import ch.algotrader.entity.property.PropertyImpl;
import ch.algotrader.enumeration.OrderType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Base class of an Entity that can hold {@link Property Properties}.
 */
@Entity
@Table(name = "order_preference")
public class OrderPreferenceImpl extends ch.algotrader.entity.property.PropertyHolderImpl implements java.io.Serializable {

    private String name;
    private OrderType orderType;
    /**
     * Contains certain order default values (e.g. quantity, orderType, delays, etc.). Except for the
    * {@link ch.algotrader.enumeration.OrderType OrderType}, all values have to be defined through
    * Properties.
    */
    private Set<AllocationImpl> allocations = new HashSet<AllocationImpl>(0);
    /**
     * Represents an actual Account / AccountGroup / AllocationProfile with an external Broker / Bank
    */
    private AccountImpl defaultAccount;

    public OrderPreferenceImpl() {
    }

    public OrderPreferenceImpl(String name, OrderType orderType) {
        this.name = name;
        this.orderType = orderType;
    }

    public OrderPreferenceImpl(Map<String, PropertyImpl> props, String name, OrderType orderType, Set<AllocationImpl> allocations, AccountImpl defaultAccount) {
        super(props);
        this.name = name;
        this.orderType = orderType;
        this.allocations = allocations;
        this.defaultAccount = defaultAccount;
    }

    @Column(name = "NAME", unique = true, nullable = false, columnDefinition = "VARCHAR(255)")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "ORDER_TYPE", nullable = false, columnDefinition = "VARCHAR(255)")
    public OrderType getOrderType() {
        return this.orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    /**
     *      * Contains certain order default values (e.g. quantity, orderType, delays, etc.). Except for the
     * {@link ch.algotrader.enumeration.OrderType OrderType}, all values have to be defined through
     * Properties.
     */

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "orderPreference")
    public Set<AllocationImpl> getAllocations() {
        return this.allocations;
    }

    public void setAllocations(Set<AllocationImpl> allocations) {
        this.allocations = allocations;
    }

    /**
     *      * Represents an actual Account / AccountGroup / AllocationProfile with an external Broker / Bank
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEFAULT_ACCOUNT_FK", columnDefinition = "INTEGER")
    public AccountImpl getDefaultAccount() {
        return this.defaultAccount;
    }

    public void setDefaultAccount(AccountImpl defaultAccount) {
        this.defaultAccount = defaultAccount;
    }

}
