package ch.algotrader.entity.strategy;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.AccountImpl;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Represents an allocation to an individual {@link ch.algotrader.entity.Account Account}.
 * If an OrderPreference is defined for an {@link ch.algotrader.enumeration.OrderType OrderType} that sends orders to multiple Accounts (e.g. {@link ch.algotrader.entity.trade.DistributingOrder DistributingOrder}), allocations to different Accounts can be defined.
 * p
 * iNote: the total of all Allocations needs to be 1.0/i
 */
@Entity
@Table(name = "allocation")
public class AllocationImpl implements java.io.Serializable {

    private int id;
    /**
     * The amount that is allocated to the corresponding Account.
    * p
    * iNote: the total of all Allocations needs to be 1.0/i
    */
    private double value;
    /**
     * Contains certain order default values (e.g. quantity, orderType, delays, etc.). Except for the
    * {@link ch.algotrader.enumeration.OrderType OrderType}, all values have to be defined through
    * Properties.
    */
    private OrderPreferenceImpl orderPreference;
    /**
     * Represents an actual Account / AccountGroup / AllocationProfile with an external Broker / Bank
    */
    private AccountImpl account;

    public AllocationImpl() {
    }

    public AllocationImpl(double value, OrderPreferenceImpl orderPreference, AccountImpl account) {
        this.value = value;
        this.orderPreference = orderPreference;
        this.account = account;
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
     *      * The amount that is allocated to the corresponding Account.
     * p
     * iNote: the total of all Allocations needs to be 1.0/i
     */

    @Column(name = "VALUE", nullable = false, columnDefinition = "DOUBLE")
    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
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
     *      * Represents an actual Account / AccountGroup / AllocationProfile with an external Broker / Bank
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_FK", nullable = false, columnDefinition = "INTEGER")
    public AccountImpl getAccount() {
        return this.account;
    }

    public void setAccount(AccountImpl account) {
        this.account = account;
    }

}
