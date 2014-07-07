package ch.algotrader.entity.marketData;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.enumeration.FeedType;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

/**
 * Any type of market data related to a particular Security
 */
@Entity
@Table(name = "market_data_event")
public class MarketDataEventImpl implements java.io.Serializable {

    private int id;
    /**
     * The dateTime of this MarketDataEvent
    */
    private Date dateTime;
    /**
     * The market data feed that provided this  MarketDataEvent
    */
    private FeedType feedType;
    /**
     * The base class of all Securities in the system
    */
    private SecurityImpl security;

    public MarketDataEventImpl() {
    }

    public MarketDataEventImpl(Date dateTime, FeedType feedType, SecurityImpl security) {
        this.dateTime = dateTime;
        this.feedType = feedType;
        this.security = security;
    }

    @GenericGenerator(name = "generator", strategy = "increment")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(name = "ID", nullable = false, columnDefinition = "INTEGER")
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     *      * The dateTime of this MarketDataEvent
     */

    @Column(name = "DATE_TIME", nullable = false, columnDefinition = "TIMESTAMP")
    public Date getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    /**
     *      * The market data feed that provided this  MarketDataEvent
     */

    @Column(name = "FEED_TYPE", nullable = false, columnDefinition = "VARCHAR(255)")
    public FeedType getFeedType() {
        return this.feedType;
    }

    public void setFeedType(FeedType feedType) {
        this.feedType = feedType;
    }

    /**
     *      * The base class of all Securities in the system
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECURITY_FK", nullable = false, columnDefinition = "INTEGER")
    public SecurityImpl getSecurity() {
        return this.security;
    }

    public void setSecurity(SecurityImpl security) {
        this.security = security;
    }

}
