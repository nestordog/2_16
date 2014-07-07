package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.OptionType;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * The base class of all Securities in the system
 */
@Entity
@Table(name = "implied_volatility")
public class ImpliedVolatilityImpl extends ch.algotrader.entity.security.SecurityImpl implements java.io.Serializable {

    /**
     * Duration defined as one of the Standard {@link Duration Durations}.
    */
    private Duration duration;
    /**
     * The Moneyness of this ImpliedVolatility.
    * p
    * Example:
    * ul
    * liCall: 1 - strike/spot/li
    * liPut: strike/spot - 1/li
    * /ul
    * iNote: The ATM Moneyness is 0/i
    */
    private Double moneyness;
    /**
     * The Delta of this ImpliedVolatility.
    * p
    * Example:
    * ul
    * liCall: 0% (OTM) - 100% (ITM)/li
    * liPut: 0% (ITM) - 100% (OTM)/li
    * /ul
    * iNote: The ATM Delta is 50%/i
    */
    private Double delta;
    /**
     * The {@link OptionType} (i.e. Put or Call)
    */
    private OptionType type;

    public ImpliedVolatilityImpl() {
    }

    public ImpliedVolatilityImpl(SecurityFamilyImpl securityFamily, Duration duration, OptionType type) {
        super(securityFamily);
        this.duration = duration;
        this.type = type;
    }

    public ImpliedVolatilityImpl(String symbol, String isin, String bbgid, String ric, String conid, String lmaxid, SecurityImpl underlying, Set<SubscriptionImpl> subscriptions,
            Set<PositionImpl> positions, SecurityFamilyImpl securityFamily, Duration duration, Double moneyness, Double delta, OptionType type) {
        super(symbol, isin, bbgid, ric, conid, lmaxid, underlying, subscriptions, positions, securityFamily);
        this.duration = duration;
        this.moneyness = moneyness;
        this.delta = delta;
        this.type = type;
    }

    /**
     *      * Duration defined as one of the Standard {@link Duration Durations}.
     */

    @Column(name = "DURATION", nullable = false, columnDefinition = "BIGINT")
    public Duration getDuration() {
        return this.duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    /**
     *      * The Moneyness of this ImpliedVolatility.
     * p
     * Example:
     * ul
     * liCall: 1 - strike/spot/li
     * liPut: strike/spot - 1/li
     * /ul
     * iNote: The ATM Moneyness is 0/i
     */

    @Column(name = "MONEYNESS", columnDefinition = "DOUBLE")
    public Double getMoneyness() {
        return this.moneyness;
    }

    public void setMoneyness(Double moneyness) {
        this.moneyness = moneyness;
    }

    /**
     *      * The Delta of this ImpliedVolatility.
     * p
     * Example:
     * ul
     * liCall: 0% (OTM) - 100% (ITM)/li
     * liPut: 0% (ITM) - 100% (OTM)/li
     * /ul
     * iNote: The ATM Delta is 50%/i
     */

    @Column(name = "DELTA", columnDefinition = "DOUBLE")
    public Double getDelta() {
        return this.delta;
    }

    public void setDelta(Double delta) {
        this.delta = delta;
    }

    /**
     *      * The {@link OptionType} (i.e. Put or Call)
     */

    @Column(name = "TYPE", nullable = false, columnDefinition = "VARCHAR(255)")
    public OptionType getType() {
        return this.type;
    }

    public void setType(OptionType type) {
        this.type = type;
    }

}
