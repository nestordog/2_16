package ch.algotrader.entity.marketData;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.enumeration.FeedType;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Any type of market data related to a particular Security
 */
@Entity
@Table(name = "tick")
public class TickImpl extends ch.algotrader.entity.marketData.MarketDataEventImpl implements java.io.Serializable {

    /**
     * The last price.
    */
    private BigDecimal last;
    /**
     * The dateTime of the last trade.
    */
    private Date lastDateTime;
    /**
     * The bid price.
    */
    private BigDecimal bid;
    /**
     * The ask price.
    */
    private BigDecimal ask;
    /**
     * The volume on the bid side.
    */
    private int volBid;
    /**
     * The volume on the ask side.
    */
    private int volAsk;
    /**
     * The current volume
    */
    private int vol;

    public TickImpl() {
    }

    public TickImpl(Date dateTime, FeedType feedType, SecurityImpl security, int volBid, int volAsk, int vol) {
        super(dateTime, feedType, security);
        this.volBid = volBid;
        this.volAsk = volAsk;
        this.vol = vol;
    }

    public TickImpl(Date dateTime, FeedType feedType, SecurityImpl security, BigDecimal last, Date lastDateTime, BigDecimal bid, BigDecimal ask, int volBid, int volAsk, int vol) {
        super(dateTime, feedType, security);
        this.last = last;
        this.lastDateTime = lastDateTime;
        this.bid = bid;
        this.ask = ask;
        this.volBid = volBid;
        this.volAsk = volAsk;
        this.vol = vol;
    }

    /**
     *      * The last price.
     */

    @Column(name = "LAST", columnDefinition = "Decimal(15,6)")
    public BigDecimal getLast() {
        return this.last;
    }

    public void setLast(BigDecimal last) {
        this.last = last;
    }

    /**
     *      * The dateTime of the last trade.
     */

    @Column(name = "LAST_DATE_TIME", columnDefinition = "TIMESTAMP")
    public Date getLastDateTime() {
        return this.lastDateTime;
    }

    public void setLastDateTime(Date lastDateTime) {
        this.lastDateTime = lastDateTime;
    }

    /**
     *      * The bid price.
     */

    @Column(name = "BID", columnDefinition = "Decimal(15,6)")
    public BigDecimal getBid() {
        return this.bid;
    }

    public void setBid(BigDecimal bid) {
        this.bid = bid;
    }

    /**
     *      * The ask price.
     */

    @Column(name = "ASK", columnDefinition = "Decimal(15,6)")
    public BigDecimal getAsk() {
        return this.ask;
    }

    public void setAsk(BigDecimal ask) {
        this.ask = ask;
    }

    /**
     *      * The volume on the bid side.
     */

    @Column(name = "VOL_BID", nullable = false, columnDefinition = "INTEGER")
    public int getVolBid() {
        return this.volBid;
    }

    public void setVolBid(int volBid) {
        this.volBid = volBid;
    }

    /**
     *      * The volume on the ask side.
     */

    @Column(name = "VOL_ASK", nullable = false, columnDefinition = "INTEGER")
    public int getVolAsk() {
        return this.volAsk;
    }

    public void setVolAsk(int volAsk) {
        this.volAsk = volAsk;
    }

    /**
     *      * The current volume
     */

    @Column(name = "VOL", nullable = false, columnDefinition = "INTEGER")
    public int getVol() {
        return this.vol;
    }

    public void setVol(int vol) {
        this.vol = vol;
    }

}
