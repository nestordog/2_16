package ch.algotrader.entity.property;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import java.math.BigDecimal;
import java.util.Date;
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
 * Custom Property of type {@code int}, {@code double}, {@code money}, {@code text}, {@code date} or {@code boolean} that can be assigned to a {@link PropertyHolder}
 */
@Entity
@Table(name = "property")
public class PropertyImpl implements java.io.Serializable {

    private int id;
    private int version;
    private String name;
    /**
     * If set to {@code false}, the Property will be removed when resetting the database before a simulation run.
    */
    private boolean persistent;
    private Integer intValue;
    private Double doubleValue;
    private BigDecimal moneyValue;
    private String textValue;
    private Date dateTimeValue;
    private Boolean booleanValue;
    /**
     * Base class of an Entity that can hold {@link Property Properties}.
    */
    private PropertyHolderImpl propertyHolder;

    public PropertyImpl() {
    }

    public PropertyImpl(String name, boolean persistent, PropertyHolderImpl propertyHolder) {
        this.name = name;
        this.persistent = persistent;
        this.propertyHolder = propertyHolder;
    }

    public PropertyImpl(String name, boolean persistent, Integer intValue, Double doubleValue, BigDecimal moneyValue, String textValue, Date dateTimeValue, Boolean booleanValue,
            PropertyHolderImpl propertyHolder) {
        this.name = name;
        this.persistent = persistent;
        this.intValue = intValue;
        this.doubleValue = doubleValue;
        this.moneyValue = moneyValue;
        this.textValue = textValue;
        this.dateTimeValue = dateTimeValue;
        this.booleanValue = booleanValue;
        this.propertyHolder = propertyHolder;
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

    @Column(name = "NAME", nullable = false, columnDefinition = "VARCHAR(255)")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *      * If set to {@code false}, the Property will be removed when resetting the database before a simulation run.
     */

    @Column(name = "PERSISTENT", nullable = false, columnDefinition = "TINYINT")
    public boolean isPersistent() {
        return this.persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    @Column(name = "INT_VALUE", columnDefinition = "INTEGER")
    public Integer getIntValue() {
        return this.intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    @Column(name = "DOUBLE_VALUE", columnDefinition = "DOUBLE")
    public Double getDoubleValue() {
        return this.doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    @Column(name = "MONEY_VALUE", columnDefinition = "Decimal(15,6)")
    public BigDecimal getMoneyValue() {
        return this.moneyValue;
    }

    public void setMoneyValue(BigDecimal moneyValue) {
        this.moneyValue = moneyValue;
    }

    @Column(name = "TEXT_VALUE", columnDefinition = "VARCHAR(255)")
    public String getTextValue() {
        return this.textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    @Column(name = "DATE_TIME_VALUE", columnDefinition = "TIMESTAMP")
    public Date getDateTimeValue() {
        return this.dateTimeValue;
    }

    public void setDateTimeValue(Date dateTimeValue) {
        this.dateTimeValue = dateTimeValue;
    }

    @Column(name = "BOOLEAN_VALUE", columnDefinition = "TINYINT")
    public Boolean getBooleanValue() {
        return this.booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    /**
     *      * Base class of an Entity that can hold {@link Property Properties}.
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROPERTY_HOLDER_FK", nullable = false, columnDefinition = "INTEGER")
    public PropertyHolderImpl getPropertyHolder() {
        return this.propertyHolder;
    }

    public void setPropertyHolder(PropertyHolderImpl propertyHolder) {
        this.propertyHolder = propertyHolder;
    }

}
