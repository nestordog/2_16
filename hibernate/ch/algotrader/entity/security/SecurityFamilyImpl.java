package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.TimePeriod;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Represents an entire family of similar {@link ch.algotrader.entity.security.Security Securities} (e.g. All Options of the SP500)
 */
@Entity
@Table(name = "security_family")
public class SecurityFamilyImpl implements java.io.Serializable {

    private int id;
    /**
     * The name of this SecurityFamily
    */
    private String name;
    /**
     * The common part of Symbol (e.g. VIX for the VIX Future FVIX AUG/10 1000)
    */
    private String symbolRoot;
    /**
     * The common part of the ISIN (e.g. VIX for the VIX Future 0FVIXFD00000). If no value is set the {@code baseSymbol} is taken.
    */
    private String isinRoot;
    /**
     * The common part of RIC (e.g. VX for the VIX Future VXQ1:VE). If no value is set the {@code baseSymbol} is taken.
    */
    private String ricRoot;
    /**
     * eg. SPX for monthly SP options adn SPXW for weekly SP options
    */
    private String tradingClass;
    /**
     * The currency of the Securities of this SecurityFamily
    */
    private Currency currency;
    /**
     * The contractSize of the Securities of this SecurityFamily (e.g. 100 for SPX Options).
    */
    private double contractSize;
    /**
     * The number of digits that prices of Securities of this SecurityFamily are quoted in.
    */
    private int scale;
    /**
     * A {@link java.text.ChoiceFormat} representing a pattern that defines the TickSize at different Price Levels.
    * p
    * For example the pattern 00.05 | 30.1 says, that the TickSize is 0.05 for prices from 0 to (but not including) 3 and 0.1 for prices above 3.
    */
    private String tickSizePattern;
    /**
     * The Execution Commission for one Contract of a Security of this SecurityFamily.
    */
    private BigDecimal executionCommission;
    /**
     * The Clearing Commission for one Contract of a Security of this SecurityFamily.
    */
    private BigDecimal clearingCommission;
    /**
     * The Exchange Fee for one Contract of a Security of this SecurityFamily.
    */
    private BigDecimal fee;
    /**
     * Represents a Security for which an order can be directly sent to the Market or via an OTC order
    */
    private boolean tradeable;
    /**
     * Represents virtual Securities that are only known to the Framework. Market Data needs to be calculated manually (e.g. a Combination)
    */
    private boolean synthetic;
    /**
     * Periodicity when Market Data is saved to the database (DAY, HOUR or MINUTE). Not valid for synthetic securities
    */
    private TimePeriod periodicity;
    /**
     * The Maximum Market Data Gap (in minutes) that is expected in normal Market Conditions. An exception is thrown if no market data arrives for a period longer than this value which might indicate a problem with the external Market Data Provider.
    */
    private Integer maxGap;
    /**
     * The base class of all Securities in the system
    */
    private SecurityImpl underlying;
    /**
     * Exchange where securities are traded
    */
    private ExchangeImpl exchange;
    /**
     * Represents an entire family of similar {@link ch.algotrader.entity.security.Security Securities}
    * (e.g. All Options of the SP500)
    */
    private Set<SecurityImpl> securities = new HashSet<SecurityImpl>(0);
    /**
     * Represents an entire family of similar {@link ch.algotrader.entity.security.Security Securities}
    * (e.g. All Options of the SP500)
    */
    private Map<String, BrokerParametersImpl> brokerParameters = new HashMap<String, BrokerParametersImpl>(0);

    public SecurityFamilyImpl() {
    }

    public SecurityFamilyImpl(String name, Currency currency, double contractSize, int scale, String tickSizePattern, boolean tradeable, boolean synthetic, ExchangeImpl exchange) {
        this.name = name;
        this.currency = currency;
        this.contractSize = contractSize;
        this.scale = scale;
        this.tickSizePattern = tickSizePattern;
        this.tradeable = tradeable;
        this.synthetic = synthetic;
        this.exchange = exchange;
    }

    public SecurityFamilyImpl(String name, String symbolRoot, String isinRoot, String ricRoot, String tradingClass, Currency currency, double contractSize, int scale, String tickSizePattern,
            BigDecimal executionCommission, BigDecimal clearingCommission, BigDecimal fee, boolean tradeable, boolean synthetic, TimePeriod periodicity, Integer maxGap, SecurityImpl underlying,
            ExchangeImpl exchange, Set<SecurityImpl> securities, Map<String, BrokerParametersImpl> brokerParameters) {
        this.name = name;
        this.symbolRoot = symbolRoot;
        this.isinRoot = isinRoot;
        this.ricRoot = ricRoot;
        this.tradingClass = tradingClass;
        this.currency = currency;
        this.contractSize = contractSize;
        this.scale = scale;
        this.tickSizePattern = tickSizePattern;
        this.executionCommission = executionCommission;
        this.clearingCommission = clearingCommission;
        this.fee = fee;
        this.tradeable = tradeable;
        this.synthetic = synthetic;
        this.periodicity = periodicity;
        this.maxGap = maxGap;
        this.underlying = underlying;
        this.exchange = exchange;
        this.securities = securities;
        this.brokerParameters = brokerParameters;
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
     *      * The name of this SecurityFamily
     */

    @Column(name = "NAME", nullable = false, columnDefinition = "VARCHAR(255)")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *      * The common part of Symbol (e.g. VIX for the VIX Future FVIX AUG/10 1000)
     */

    @Column(name = "SYMBOL_ROOT", columnDefinition = "VARCHAR(255)")
    public String getSymbolRoot() {
        return this.symbolRoot;
    }

    public void setSymbolRoot(String symbolRoot) {
        this.symbolRoot = symbolRoot;
    }

    /**
     *      * The common part of the ISIN (e.g. VIX for the VIX Future 0FVIXFD00000). If no value is set the {@code baseSymbol} is taken.
     */

    @Column(name = "ISIN_ROOT", columnDefinition = "VARCHAR(255)")
    public String getIsinRoot() {
        return this.isinRoot;
    }

    public void setIsinRoot(String isinRoot) {
        this.isinRoot = isinRoot;
    }

    /**
     *      * The common part of RIC (e.g. VX for the VIX Future VXQ1:VE). If no value is set the {@code baseSymbol} is taken.
     */

    @Column(name = "RIC_ROOT", columnDefinition = "VARCHAR(255)")
    public String getRicRoot() {
        return this.ricRoot;
    }

    public void setRicRoot(String ricRoot) {
        this.ricRoot = ricRoot;
    }

    /**
     *      * eg. SPX for monthly SP options adn SPXW for weekly SP options
     */

    @Column(name = "TRADING_CLASS", columnDefinition = "VARCHAR(255)")
    public String getTradingClass() {
        return this.tradingClass;
    }

    public void setTradingClass(String tradingClass) {
        this.tradingClass = tradingClass;
    }

    /**
     *      * The currency of the Securities of this SecurityFamily
     */

    @Column(name = "CURRENCY", nullable = false, columnDefinition = "VARCHAR(255)")
    public Currency getCurrency() {
        return this.currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    /**
     *      * The contractSize of the Securities of this SecurityFamily (e.g. 100 for SPX Options).
     */

    @Column(name = "CONTRACT_SIZE", nullable = false, columnDefinition = "DOUBLE")
    public double getContractSize() {
        return this.contractSize;
    }

    public void setContractSize(double contractSize) {
        this.contractSize = contractSize;
    }

    /**
     *      * The number of digits that prices of Securities of this SecurityFamily are quoted in.
     */

    @Column(name = "SCALE", nullable = false, columnDefinition = "INTEGER")
    public int getScale() {
        return this.scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    /**
     *      * A {@link java.text.ChoiceFormat} representing a pattern that defines the TickSize at different Price Levels.
     * p
     * For example the pattern 00.05 | 30.1 says, that the TickSize is 0.05 for prices from 0 to (but not including) 3 and 0.1 for prices above 3.
     */

    @Column(name = "TICK_SIZE_PATTERN", nullable = false, columnDefinition = "VARCHAR(255)")
    public String getTickSizePattern() {
        return this.tickSizePattern;
    }

    public void setTickSizePattern(String tickSizePattern) {
        this.tickSizePattern = tickSizePattern;
    }

    /**
     *      * The Execution Commission for one Contract of a Security of this SecurityFamily.
     */

    @Column(name = "EXECUTION_COMMISSION", columnDefinition = "Decimal(15,6)")
    public BigDecimal getExecutionCommission() {
        return this.executionCommission;
    }

    public void setExecutionCommission(BigDecimal executionCommission) {
        this.executionCommission = executionCommission;
    }

    /**
     *      * The Clearing Commission for one Contract of a Security of this SecurityFamily.
     */

    @Column(name = "CLEARING_COMMISSION", columnDefinition = "Decimal(15,6)")
    public BigDecimal getClearingCommission() {
        return this.clearingCommission;
    }

    public void setClearingCommission(BigDecimal clearingCommission) {
        this.clearingCommission = clearingCommission;
    }

    /**
     *      * The Exchange Fee for one Contract of a Security of this SecurityFamily.
     */

    @Column(name = "FEE", columnDefinition = "Decimal(15,6)")
    public BigDecimal getFee() {
        return this.fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    /**
     *      * Represents a Security for which an order can be directly sent to the Market or via an OTC order
     */

    @Column(name = "TRADEABLE", nullable = false, columnDefinition = "TINYINT")
    public boolean isTradeable() {
        return this.tradeable;
    }

    public void setTradeable(boolean tradeable) {
        this.tradeable = tradeable;
    }

    /**
     *      * Represents virtual Securities that are only known to the Framework. Market Data needs to be calculated manually (e.g. a Combination)
     */

    @Column(name = "SYNTHETIC", nullable = false, columnDefinition = "TINYINT")
    public boolean isSynthetic() {
        return this.synthetic;
    }

    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }

    /**
     *      * Periodicity when Market Data is saved to the database (DAY, HOUR or MINUTE). Not valid for synthetic securities
     */

    @Column(name = "PERIODICITY", columnDefinition = "VARCHAR(255)")
    public TimePeriod getPeriodicity() {
        return this.periodicity;
    }

    public void setPeriodicity(TimePeriod periodicity) {
        this.periodicity = periodicity;
    }

    /**
     *      * The Maximum Market Data Gap (in minutes) that is expected in normal Market Conditions. An exception is thrown if no market data arrives for a period longer than this value which might indicate a problem with the external Market Data Provider.
     */

    @Column(name = "MAX_GAP", columnDefinition = "INTEGER")
    public Integer getMaxGap() {
        return this.maxGap;
    }

    public void setMaxGap(Integer maxGap) {
        this.maxGap = maxGap;
    }

    /**
     *      * The base class of all Securities in the system
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UNDERLYING_FK", columnDefinition = "INTEGER")
    public SecurityImpl getUnderlying() {
        return this.underlying;
    }

    public void setUnderlying(SecurityImpl underlying) {
        this.underlying = underlying;
    }

    /**
     *      * Exchange where securities are traded
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXCHANGE_FK", nullable = false, columnDefinition = "INTEGER")
    public ExchangeImpl getExchange() {
        return this.exchange;
    }

    public void setExchange(ExchangeImpl exchange) {
        this.exchange = exchange;
    }

    /**
     *      * Represents an entire family of similar {@link ch.algotrader.entity.security.Security Securities}
     * (e.g. All Options of the SP500)
     */

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "securityFamily")
    public Set<SecurityImpl> getSecurities() {
        return this.securities;
    }

    public void setSecurities(Set<SecurityImpl> securities) {
        this.securities = securities;
    }

    /**
     *      * Represents an entire family of similar {@link ch.algotrader.entity.security.Security Securities}
     * (e.g. All Options of the SP500)
     */

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER, mappedBy = "securityFamily")
    public Map<String, BrokerParametersImpl> getBrokerParameters() {
        return this.brokerParameters;
    }

    public void setBrokerParameters(Map<String, BrokerParametersImpl> brokerParameters) {
        this.brokerParameters = brokerParameters;
    }

}
