package ch.algotrader.entity.marketData;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.enumeration.Duration;
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
@Table(name = "bar")
public class BarImpl extends ch.algotrader.entity.marketData.MarketDataEventImpl implements java.io.Serializable {

    /**
     * The size of this Bar (e.g. 1Min, 15Min, 1Hour, etc..)
    */
    private Duration barSize;
    /**
     * The opening price of this Bar
    */
    private BigDecimal open;
    /**
     * The highest price during this Bar
    */
    private BigDecimal high;
    /**
     * The lowest price during this Bar
    */
    private BigDecimal low;
    /**
     * The closing price of this Bar
    */
    private BigDecimal close;
    /**
     * The current volume
    */
    private int vol;

    public BarImpl() {
    }

    public BarImpl(Date dateTime, FeedType feedType, SecurityImpl security, Duration barSize, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, int vol) {
        super(dateTime, feedType, security);
        this.barSize = barSize;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.vol = vol;
    }

    /**
     *      * The size of this Bar (e.g. 1Min, 15Min, 1Hour, etc..)
     */

    @Column(name = "BAR_SIZE", unique = true, nullable = false, columnDefinition = "BIGINT")
    public Duration getBarSize() {
        return this.barSize;
    }

    public void setBarSize(Duration barSize) {
        this.barSize = barSize;
    }

    /**
     *      * The opening price of this Bar
     */

    @Column(name = "OPEN", nullable = false, columnDefinition = "Decimal(15,6)")
    public BigDecimal getOpen() {
        return this.open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    /**
     *      * The highest price during this Bar
     */

    @Column(name = "HIGH", nullable = false, columnDefinition = "Decimal(15,6)")
    public BigDecimal getHigh() {
        return this.high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    /**
     *      * The lowest price during this Bar
     */

    @Column(name = "LOW", nullable = false, columnDefinition = "Decimal(15,6)")
    public BigDecimal getLow() {
        return this.low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    /**
     *      * The closing price of this Bar
     */

    @Column(name = "CLOSE", nullable = false, columnDefinition = "Decimal(15,6)")
    public BigDecimal getClose() {
        return this.close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
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
