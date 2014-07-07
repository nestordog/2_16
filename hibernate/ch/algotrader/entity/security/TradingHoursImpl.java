package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

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
 * Weekly Trading Hours for a particular Exchange
 */
@Entity
@Table(name = "trading_hours")
public class TradingHoursImpl implements java.io.Serializable {

    private int id;
    /**
     * the time when the Exchange opens. If close time is before open time it is considered to take place on the next day.
    */
    private Date open;
    /**
     * the time when the Exchange closes. If close time is before open time it is considered to take place on the next day.
    */
    private Date close;
    private boolean sunday;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    /**
     * Exchange where securities are traded
    */
    private ExchangeImpl exchange;

    public TradingHoursImpl() {
    }

    public TradingHoursImpl(Date open, Date close, boolean sunday, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday, ExchangeImpl exchange) {
        this.open = open;
        this.close = close;
        this.sunday = sunday;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.exchange = exchange;
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
     *      * the time when the Exchange opens. If close time is before open time it is considered to take place on the next day.
     */

    @Column(name = "OPEN", nullable = false, columnDefinition = "TIME")
    public Date getOpen() {
        return this.open;
    }

    public void setOpen(Date open) {
        this.open = open;
    }

    /**
     *      * the time when the Exchange closes. If close time is before open time it is considered to take place on the next day.
     */

    @Column(name = "CLOSE", nullable = false, columnDefinition = "TIME")
    public Date getClose() {
        return this.close;
    }

    public void setClose(Date close) {
        this.close = close;
    }

    @Column(name = "SUNDAY", nullable = false, columnDefinition = "TINYINT")
    public boolean isSunday() {
        return this.sunday;
    }

    public void setSunday(boolean sunday) {
        this.sunday = sunday;
    }

    @Column(name = "MONDAY", nullable = false, columnDefinition = "TINYINT")
    public boolean isMonday() {
        return this.monday;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
    }

    @Column(name = "TUESDAY", nullable = false, columnDefinition = "TINYINT")
    public boolean isTuesday() {
        return this.tuesday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }

    @Column(name = "WEDNESDAY", nullable = false, columnDefinition = "TINYINT")
    public boolean isWednesday() {
        return this.wednesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }

    @Column(name = "THURSDAY", nullable = false, columnDefinition = "TINYINT")
    public boolean isThursday() {
        return this.thursday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }

    @Column(name = "FRIDAY", nullable = false, columnDefinition = "TINYINT")
    public boolean isFriday() {
        return this.friday;
    }

    public void setFriday(boolean friday) {
        this.friday = friday;
    }

    @Column(name = "SATURDAY", nullable = false, columnDefinition = "TINYINT")
    public boolean isSaturday() {
        return this.saturday;
    }

    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
    }

    /**
     *      * Exchange where securities are traded
     */

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "EXCHANGE_FK", nullable = false, columnDefinition = "INTEGER")
    public ExchangeImpl getExchange() {
        return this.exchange;
    }

    public void setExchange(ExchangeImpl exchange) {
        this.exchange = exchange;
    }

}
