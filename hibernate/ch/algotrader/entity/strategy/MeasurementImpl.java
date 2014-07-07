package ch.algotrader.entity.strategy;

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

/**
 * Custom Measurement of type {@code int}, {@code double}, {@code money}, {@code text} or {@code boolean} related to a Strategy and a particular time
 */
@Entity
@Table(name = "measurement")
public class MeasurementImpl implements java.io.Serializable {

    private int id;
    private String name;
    private Date dateTime;
    private Integer intValue;
    private Double doubleValue;
    private BigDecimal moneyValue;
    private String textValue;
    private Boolean booleanValue;
    /**
     * Represents a running Strategy within the system. In addition the Base is also represented by an
    * instance of this class.
    */
    private StrategyImpl strategy;

    public MeasurementImpl() {
    }

    public MeasurementImpl(String name, Date dateTime, StrategyImpl strategy) {
        this.name = name;
        this.dateTime = dateTime;
        this.strategy = strategy;
    }

    public MeasurementImpl(String name, Date dateTime, Integer intValue, Double doubleValue, BigDecimal moneyValue, String textValue, Boolean booleanValue, StrategyImpl strategy) {
        this.name = name;
        this.dateTime = dateTime;
        this.intValue = intValue;
        this.doubleValue = doubleValue;
        this.moneyValue = moneyValue;
        this.textValue = textValue;
        this.booleanValue = booleanValue;
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

    @Column(name = "NAME", nullable = false, columnDefinition = "VARCHAR(255)")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "DATE_TIME", nullable = false, columnDefinition = "TIMESTAMP")
    public Date getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
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

    @Column(name = "BOOLEAN_VALUE", columnDefinition = "TINYINT")
    public Boolean getBooleanValue() {
        return this.booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
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
