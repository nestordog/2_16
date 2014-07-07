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
 * A trading holiday of a particular market. If either {@code lateOpen} or {@code earlyOpen} or both are specified the market is still open on that day but with unusual trading hours.
 */
@Entity
@Table(name = "holiday")
public class HolidayImpl implements java.io.Serializable {

    private int id;
    private Date date;
    /**
     * the late opening time of the market on that day.
    */
    private Date lateOpen;
    /**
     * the early closing time of the market on that day.
    */
    private Date earlyClose;
    /**
     * Exchange where securities are traded
    */
    private ExchangeImpl exchange;

    public HolidayImpl() {
    }

    public HolidayImpl(Date date, ExchangeImpl exchange) {
        this.date = date;
        this.exchange = exchange;
    }

    public HolidayImpl(Date date, Date lateOpen, Date earlyClose, ExchangeImpl exchange) {
        this.date = date;
        this.lateOpen = lateOpen;
        this.earlyClose = earlyClose;
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

    @Column(name = "DATE", nullable = false, columnDefinition = "DATETIME")
    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     *      * the late opening time of the market on that day.
     */

    @Column(name = "LATE_OPEN", columnDefinition = "TIME")
    public Date getLateOpen() {
        return this.lateOpen;
    }

    public void setLateOpen(Date lateOpen) {
        this.lateOpen = lateOpen;
    }

    /**
     *      * the early closing time of the market on that day.
     */

    @Column(name = "EARLY_CLOSE", columnDefinition = "TIME")
    public Date getEarlyClose() {
        return this.earlyClose;
    }

    public void setEarlyClose(Date earlyClose) {
        this.earlyClose = earlyClose;
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
