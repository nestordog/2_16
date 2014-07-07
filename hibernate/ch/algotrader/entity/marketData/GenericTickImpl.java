package ch.algotrader.entity.marketData;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.TickType;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Any type of market data related to a particular Security
 */
@Entity
@Table(name = "generic_tick")
public class GenericTickImpl extends ch.algotrader.entity.marketData.MarketDataEventImpl implements java.io.Serializable {

    private TickType tickType;
    private BigDecimal moneyValue;
    private Double doubleValue;
    private Integer intValue;

    public GenericTickImpl() {
    }

    public GenericTickImpl(Date dateTime, FeedType feedType, SecurityImpl security, TickType tickType) {
        super(dateTime, feedType, security);
        this.tickType = tickType;
    }

    public GenericTickImpl(Date dateTime, FeedType feedType, SecurityImpl security, TickType tickType, BigDecimal moneyValue, Double doubleValue, Integer intValue) {
        super(dateTime, feedType, security);
        this.tickType = tickType;
        this.moneyValue = moneyValue;
        this.doubleValue = doubleValue;
        this.intValue = intValue;
    }

    @Column(name = "TICK_TYPE", nullable = false, columnDefinition = "VARCHAR(255)")
    public TickType getTickType() {
        return this.tickType;
    }

    public void setTickType(TickType tickType) {
        this.tickType = tickType;
    }

    @Column(name = "MONEY_VALUE", columnDefinition = "Decimal(15,6)")
    public BigDecimal getMoneyValue() {
        return this.moneyValue;
    }

    public void setMoneyValue(BigDecimal moneyValue) {
        this.moneyValue = moneyValue;
    }

    @Column(name = "DOUBLE_VALUE", columnDefinition = "DOUBLE")
    public Double getDoubleValue() {
        return this.doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    @Column(name = "INT_VALUE", columnDefinition = "INTEGER")
    public Integer getIntValue() {
        return this.intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

}
