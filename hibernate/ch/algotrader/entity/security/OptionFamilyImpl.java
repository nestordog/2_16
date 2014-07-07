package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.ExpirationType;
import ch.algotrader.enumeration.TimePeriod;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Represents an entire family of similar {@link ch.algotrader.entity.security.Security Securities} (e.g. All Options of the SP500)
 */
@Entity
@Table(name = "option_family")
public class OptionFamilyImpl extends ch.algotrader.entity.security.SecurityFamilyImpl implements java.io.Serializable {

    /**
     * The current Intrest relevant to the pricing of {@link Option Options} of this OptionFamily.
    */
    private double intrest;
    /**
     * The current Dividend relevant to the pricing of {@link Option Options} of this OptionFamily.
    */
    private double dividend;
    /**
     * The Margin Parameter used to calculate the Margin of a {@code Option} Contract of this OptionFamily.
    */
    private double marginParameter;
    /**
     * The Type of Expiration Logic utilized by {@link Option Options} of this OptionFamily.
    */
    private ExpirationType expirationType;
    /**
     * The {@link Duration} between two {@link Option Options} of this OptionFamily. (e.g. 1 Month for VIX Options)
    */
    private Duration expirationDistance;
    /**
     * The distance between two strikes of {@link Option Options} of this OptionFamily.
    */
    private double strikeDistance;
    /**
     * marks a weekly option series as opposed to a monthly option series
    */
    private boolean weekly;

    public OptionFamilyImpl() {
    }

    public OptionFamilyImpl(String name, Currency currency, double contractSize, int scale, String tickSizePattern, boolean tradeable, boolean synthetic, ExchangeImpl exchange, double intrest,
            double dividend, double marginParameter, ExpirationType expirationType, Duration expirationDistance, double strikeDistance, boolean weekly) {
        super(name, currency, contractSize, scale, tickSizePattern, tradeable, synthetic, exchange);
        this.intrest = intrest;
        this.dividend = dividend;
        this.marginParameter = marginParameter;
        this.expirationType = expirationType;
        this.expirationDistance = expirationDistance;
        this.strikeDistance = strikeDistance;
        this.weekly = weekly;
    }

    public OptionFamilyImpl(String name, String symbolRoot, String isinRoot, String ricRoot, String tradingClass, Currency currency, double contractSize, int scale, String tickSizePattern,
            BigDecimal executionCommission, BigDecimal clearingCommission, BigDecimal fee, boolean tradeable, boolean synthetic, TimePeriod periodicity, Integer maxGap, SecurityImpl underlying,
            ExchangeImpl exchange, Set<SecurityImpl> securities, Map<String, BrokerParametersImpl> brokerParameters, double intrest, double dividend, double marginParameter,
            ExpirationType expirationType, Duration expirationDistance, double strikeDistance, boolean weekly) {
        super(name, symbolRoot, isinRoot, ricRoot, tradingClass, currency, contractSize, scale, tickSizePattern, executionCommission, clearingCommission, fee, tradeable, synthetic, periodicity,
                maxGap, underlying, exchange, securities, brokerParameters);
        this.intrest = intrest;
        this.dividend = dividend;
        this.marginParameter = marginParameter;
        this.expirationType = expirationType;
        this.expirationDistance = expirationDistance;
        this.strikeDistance = strikeDistance;
        this.weekly = weekly;
    }

    /**
     *      * The current Intrest relevant to the pricing of {@link Option Options} of this OptionFamily.
     */

    @Column(name = "INTREST", nullable = false, columnDefinition = "DOUBLE")
    public double getIntrest() {
        return this.intrest;
    }

    public void setIntrest(double intrest) {
        this.intrest = intrest;
    }

    /**
     *      * The current Dividend relevant to the pricing of {@link Option Options} of this OptionFamily.
     */

    @Column(name = "DIVIDEND", nullable = false, columnDefinition = "DOUBLE")
    public double getDividend() {
        return this.dividend;
    }

    public void setDividend(double dividend) {
        this.dividend = dividend;
    }

    /**
     *      * The Margin Parameter used to calculate the Margin of a {@code Option} Contract of this OptionFamily.
     */

    @Column(name = "MARGIN_PARAMETER", nullable = false, columnDefinition = "DOUBLE")
    public double getMarginParameter() {
        return this.marginParameter;
    }

    public void setMarginParameter(double marginParameter) {
        this.marginParameter = marginParameter;
    }

    /**
     *      * The Type of Expiration Logic utilized by {@link Option Options} of this OptionFamily.
     */

    @Column(name = "EXPIRATION_TYPE", nullable = false, columnDefinition = "VARCHAR(255)")
    public ExpirationType getExpirationType() {
        return this.expirationType;
    }

    public void setExpirationType(ExpirationType expirationType) {
        this.expirationType = expirationType;
    }

    /**
     *      * The {@link Duration} between two {@link Option Options} of this OptionFamily. (e.g. 1 Month for VIX Options)
     */

    @Column(name = "EXPIRATION_DISTANCE", nullable = false, columnDefinition = "BIGINT")
    public Duration getExpirationDistance() {
        return this.expirationDistance;
    }

    public void setExpirationDistance(Duration expirationDistance) {
        this.expirationDistance = expirationDistance;
    }

    /**
     *      * The distance between two strikes of {@link Option Options} of this OptionFamily.
     */

    @Column(name = "STRIKE_DISTANCE", nullable = false, columnDefinition = "DOUBLE")
    public double getStrikeDistance() {
        return this.strikeDistance;
    }

    public void setStrikeDistance(double strikeDistance) {
        this.strikeDistance = strikeDistance;
    }

    /**
     *      * marks a weekly option series as opposed to a monthly option series
     */

    @Column(name = "WEEKLY", nullable = false, columnDefinition = "TINYINT")
    public boolean isWeekly() {
        return this.weekly;
    }

    public void setWeekly(boolean weekly) {
        this.weekly = weekly;
    }

}
