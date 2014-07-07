package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.QuotationStyle;
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
@Table(name = "bond_family")
public class BondFamilyImpl extends ch.algotrader.entity.security.SecurityFamilyImpl implements java.io.Serializable {

    /**
     * The {@link Duration} between two {@link Bond Bonds} of this BondFamily. (e.g. 3 Months)
    */
    private Duration maturityDistance;
    /**
     * Represents the length of this Bond Chain (i.e. how many Bonds exist at one particular point in time)
    */
    private int length;
    /**
     * The type of quotes received for this BondFamily
    */
    private QuotationStyle quotationStyle;

    public BondFamilyImpl() {
    }

    public BondFamilyImpl(String name, Currency currency, double contractSize, int scale, String tickSizePattern, boolean tradeable, boolean synthetic, ExchangeImpl exchange,
            Duration maturityDistance, int length, QuotationStyle quotationStyle) {
        super(name, currency, contractSize, scale, tickSizePattern, tradeable, synthetic, exchange);
        this.maturityDistance = maturityDistance;
        this.length = length;
        this.quotationStyle = quotationStyle;
    }

    public BondFamilyImpl(String name, String symbolRoot, String isinRoot, String ricRoot, String tradingClass, Currency currency, double contractSize, int scale, String tickSizePattern,
            BigDecimal executionCommission, BigDecimal clearingCommission, BigDecimal fee, boolean tradeable, boolean synthetic, TimePeriod periodicity, Integer maxGap, SecurityImpl underlying,
            ExchangeImpl exchange, Set<SecurityImpl> securities, Map<String, BrokerParametersImpl> brokerParameters, Duration maturityDistance, int length, QuotationStyle quotationStyle) {
        super(name, symbolRoot, isinRoot, ricRoot, tradingClass, currency, contractSize, scale, tickSizePattern, executionCommission, clearingCommission, fee, tradeable, synthetic, periodicity,
                maxGap, underlying, exchange, securities, brokerParameters);
        this.maturityDistance = maturityDistance;
        this.length = length;
        this.quotationStyle = quotationStyle;
    }

    /**
     *      * The {@link Duration} between two {@link Bond Bonds} of this BondFamily. (e.g. 3 Months)
     */

    @Column(name = "MATURITY_DISTANCE", nullable = false, columnDefinition = "BIGINT")
    public Duration getMaturityDistance() {
        return this.maturityDistance;
    }

    public void setMaturityDistance(Duration maturityDistance) {
        this.maturityDistance = maturityDistance;
    }

    /**
     *      * Represents the length of this Bond Chain (i.e. how many Bonds exist at one particular point in time)
     */

    @Column(name = "LENGTH", nullable = false, columnDefinition = "INTEGER")
    public int getLength() {
        return this.length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    /**
     *      * The type of quotes received for this BondFamily
     */

    @Column(name = "QUOTATION_STYLE", nullable = false, columnDefinition = "VARCHAR(255)")
    public QuotationStyle getQuotationStyle() {
        return this.quotationStyle;
    }

    public void setQuotationStyle(QuotationStyle quotationStyle) {
        this.quotationStyle = quotationStyle;
    }

}
