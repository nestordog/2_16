package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Exchange where securities are traded
 */
@Entity
@Table(name = "exchange")
public class ExchangeImpl implements java.io.Serializable {

    private int id;
    /**
     * The market where the Securities of this Family are traded.
    */
    private String name;
    private String code;
    private String timeZone;
    /**
     * Exchange where securities are traded
    */
    private Set<TradingHoursImpl> tradingHours = new HashSet<TradingHoursImpl>(0);
    /**
     * Exchange where securities are traded
    */
    private Set<SecurityFamilyImpl> securityFamilies = new HashSet<SecurityFamilyImpl>(0);
    /**
     * Exchange where securities are traded
    */
    private Set<HolidayImpl> holidays = new HashSet<HolidayImpl>(0);

    public ExchangeImpl() {
    }

    public ExchangeImpl(String name, String code, String timeZone) {
        this.name = name;
        this.code = code;
        this.timeZone = timeZone;
    }

    public ExchangeImpl(String name, String code, String timeZone, Set<TradingHoursImpl> tradingHours, Set<SecurityFamilyImpl> securityFamilies, Set<HolidayImpl> holidays) {
        this.name = name;
        this.code = code;
        this.timeZone = timeZone;
        this.tradingHours = tradingHours;
        this.securityFamilies = securityFamilies;
        this.holidays = holidays;
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
     *      * The market where the Securities of this Family are traded.
     */

    @Column(name = "NAME", unique = true, nullable = false, columnDefinition = "VARCHAR(255)")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "CODE", nullable = false, columnDefinition = "VARCHAR(255)")
    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name = "TIME_ZONE", nullable = false, columnDefinition = "VARCHAR(255)")
    public String getTimeZone() {
        return this.timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     *      * Exchange where securities are traded
     */

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER, mappedBy = "exchange")
    public Set<TradingHoursImpl> getTradingHours() {
        return this.tradingHours;
    }

    public void setTradingHours(Set<TradingHoursImpl> tradingHours) {
        this.tradingHours = tradingHours;
    }

    /**
     *      * Exchange where securities are traded
     */

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "exchange")
    public Set<SecurityFamilyImpl> getSecurityFamilies() {
        return this.securityFamilies;
    }

    public void setSecurityFamilies(Set<SecurityFamilyImpl> securityFamilies) {
        this.securityFamilies = securityFamilies;
    }

    /**
     *      * Exchange where securities are traded
     */

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER, mappedBy = "exchange")
    public Set<HolidayImpl> getHolidays() {
        return this.holidays;
    }

    public void setHolidays(Set<HolidayImpl> holidays) {
        this.holidays = holidays;
    }

}
