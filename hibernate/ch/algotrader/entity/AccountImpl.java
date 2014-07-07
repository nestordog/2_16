package ch.algotrader.entity;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.OrderServiceType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represents an actual Account / AccountGroup / AllocationProfile with an external Broker / Bank
 */
@Entity
@Table(name = "account")
public class AccountImpl implements java.io.Serializable {

    private int id;
    private String name;
    /**
     * An {@code active} Account will start a corresponding Session in Live Trading.
    */
    private boolean active;
    /**
     * The {@link Broker} associated with this Account.
    */
    private Broker broker;
    /**
     * The {@link OrderServiceType} associated with this Account.
    */
    private OrderServiceType orderServiceType;
    /**
     * The name of the session in place for Live Trading. Primarily used for FIX Connections.
    */
    private String sessionQualifier;
    /**
     * External Account Number
    */
    private String extAccount;
    /**
     * External Account Group
    */
    private String extAccountGroup;
    /**
     * External Allocation Profile
    */
    private String extAllocationProfile;
    /**
     * External Clearing Account Number
    */
    private String extClearingAccount;

    public AccountImpl() {
    }

    public AccountImpl(String name, boolean active, Broker broker, OrderServiceType orderServiceType) {
        this.name = name;
        this.active = active;
        this.broker = broker;
        this.orderServiceType = orderServiceType;
    }

    public AccountImpl(String name, boolean active, Broker broker, OrderServiceType orderServiceType, String sessionQualifier, String extAccount, String extAccountGroup, String extAllocationProfile,
            String extClearingAccount) {
        this.name = name;
        this.active = active;
        this.broker = broker;
        this.orderServiceType = orderServiceType;
        this.sessionQualifier = sessionQualifier;
        this.extAccount = extAccount;
        this.extAccountGroup = extAccountGroup;
        this.extAllocationProfile = extAllocationProfile;
        this.extClearingAccount = extClearingAccount;
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

    @Column(name = "NAME", unique = true, nullable = false, columnDefinition = "VARCHAR(255)")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *      * An {@code active} Account will start a corresponding Session in Live Trading.
     */

    @Column(name = "ACTIVE", nullable = false, columnDefinition = "TINYINT")
    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     *      * The {@link Broker} associated with this Account.
     */

    @Column(name = "BROKER", nullable = false, columnDefinition = "VARCHAR(255)")
    public Broker getBroker() {
        return this.broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    /**
     *      * The {@link OrderServiceType} associated with this Account.
     */

    @Column(name = "ORDER_SERVICE_TYPE", nullable = false, columnDefinition = "VARCHAR(255)")
    public OrderServiceType getOrderServiceType() {
        return this.orderServiceType;
    }

    public void setOrderServiceType(OrderServiceType orderServiceType) {
        this.orderServiceType = orderServiceType;
    }

    /**
     *      * The name of the session in place for Live Trading. Primarily used for FIX Connections.
     */

    @Column(name = "SESSION_QUALIFIER", columnDefinition = "VARCHAR(255)")
    public String getSessionQualifier() {
        return this.sessionQualifier;
    }

    public void setSessionQualifier(String sessionQualifier) {
        this.sessionQualifier = sessionQualifier;
    }

    /**
     *      * External Account Number
     */

    @Column(name = "EXT_ACCOUNT", unique = true, columnDefinition = "VARCHAR(255)")
    public String getExtAccount() {
        return this.extAccount;
    }

    public void setExtAccount(String extAccount) {
        this.extAccount = extAccount;
    }

    /**
     *      * External Account Group
     */

    @Column(name = "EXT_ACCOUNT_GROUP", unique = true, columnDefinition = "VARCHAR(255)")
    public String getExtAccountGroup() {
        return this.extAccountGroup;
    }

    public void setExtAccountGroup(String extAccountGroup) {
        this.extAccountGroup = extAccountGroup;
    }

    /**
     *      * External Allocation Profile
     */

    @Column(name = "EXT_ALLOCATION_PROFILE", unique = true, columnDefinition = "VARCHAR(255)")
    public String getExtAllocationProfile() {
        return this.extAllocationProfile;
    }

    public void setExtAllocationProfile(String extAllocationProfile) {
        this.extAllocationProfile = extAllocationProfile;
    }

    /**
     *      * External Clearing Account Number
     */

    @Column(name = "EXT_CLEARING_ACCOUNT", unique = true, columnDefinition = "VARCHAR(255)")
    public String getExtClearingAccount() {
        return this.extClearingAccount;
    }

    public void setExtClearingAccount(String extClearingAccount) {
        this.extClearingAccount = extClearingAccount;
    }

}
