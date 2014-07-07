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
@Table(name = "generic_future_family")
public class GenericFutureFamilyImpl extends ch.algotrader.entity.security.SecurityFamilyImpl implements java.io.Serializable {

    /**
     * The Type of Expiration Logic utilized by {@link GenericFuture GenericFutures} of this GenericFutureFamily.
    */
    private ExpirationType expirationType;
    /**
     * The {@link Duration} between two {@link GenericFuture GenericFutures} of this GenericFutureFamily. (e.g. 1 Month for UX)
    */
    private Duration expirationDistance;

    public GenericFutureFamilyImpl() {
    }

    public GenericFutureFamilyImpl(String name, Currency currency, double contractSize, int scale, String tickSizePattern, boolean tradeable, boolean synthetic, ExchangeImpl exchange,
            ExpirationType expirationType, Duration expirationDistance) {
        super(name, currency, contractSize, scale, tickSizePattern, tradeable, synthetic, exchange);
        this.expirationType = expirationType;
        this.expirationDistance = expirationDistance;
    }

    public GenericFutureFamilyImpl(String name, String symbolRoot, String isinRoot, String ricRoot, String tradingClass, Currency currency, double contractSize, int scale, String tickSizePattern,
            BigDecimal executionCommission, BigDecimal clearingCommission, BigDecimal fee, boolean tradeable, boolean synthetic, TimePeriod periodicity, Integer maxGap, SecurityImpl underlying,
            ExchangeImpl exchange, Set<SecurityImpl> securities, Map<String, BrokerParametersImpl> brokerParameters, ExpirationType expirationType, Duration expirationDistance) {
        super(name, symbolRoot, isinRoot, ricRoot, tradingClass, currency, contractSize, scale, tickSizePattern, executionCommission, clearingCommission, fee, tradeable, synthetic, periodicity,
                maxGap, underlying, exchange, securities, brokerParameters);
        this.expirationType = expirationType;
        this.expirationDistance = expirationDistance;
    }

    /**
     *      * The Type of Expiration Logic utilized by {@link GenericFuture GenericFutures} of this GenericFutureFamily.
     */

    @Column(name = "EXPIRATION_TYPE", nullable = false, columnDefinition = "VARCHAR(255)")
    public ExpirationType getExpirationType() {
        return this.expirationType;
    }

    public void setExpirationType(ExpirationType expirationType) {
        this.expirationType = expirationType;
    }

    /**
     *      * The {@link Duration} between two {@link GenericFuture GenericFutures} of this GenericFutureFamily. (e.g. 1 Month for UX)
     */

    @Column(name = "EXPIRATION_DISTANCE", nullable = false, columnDefinition = "BIGINT")
    public Duration getExpirationDistance() {
        return this.expirationDistance;
    }

    public void setExpirationDistance(Duration expirationDistance) {
        this.expirationDistance = expirationDistance;
    }

}
