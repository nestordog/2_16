package ch.algotrader.entity.strategy;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import java.math.BigDecimal;
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
 * Represents certain balances (e.g. {@code netLiqValue}, {@code cashBalance}, etc.) of a particular Strategy at a particular time. Every hour PortfolioValues are saved to the database for every Strategy. These PortfolioValues will be displayed in the PortfolioChart of the client.
 */
@Entity
@Table(name = "portfolio_value")
public class PortfolioValueImpl implements java.io.Serializable {

    private int id;
    /**
     * The dateTime of this PortfolioValue
    */
    private Date dateTime;
    /**
     * Current market value of all Assets. {@code cashBalance} + {@code securitiesCurrentValue}
    */
    private BigDecimal netLiqValue;
    /**
     * Current market value of all positions.
    */
    private BigDecimal securitiesCurrentValue;
    /**
     * Total cash
    */
    private BigDecimal cashBalance;
    /**
     * Current Maintenance Margin of all marginable positions
    */
    private BigDecimal maintenanceMargin;
    /**
     * Current (delta-adjusted) Notional Exposure
    */
    private double leverage;
    /**
     * Allocation assigned to the Base / Strategy.
    * p
    * The total of all allocations needs to be {@code 1.0}
    */
    private double allocation;
    /**
     * CashFlow value occurred at the specified time.
    * PortfolioValues are recorded every time a transaction occurs that influences performance.
    * For the Base these are {@code CREDIT} and {@code DEBIT} and for strategies, these are {@code REBALANCE}
    */
    private BigDecimal cashFlow;
    /**
     * Represents a running Strategy within the system. In addition the Base is also represented by an
    * instance of this class.
    */
    private StrategyImpl strategy;

    public PortfolioValueImpl() {
    }

    public PortfolioValueImpl(Date dateTime, BigDecimal netLiqValue, BigDecimal securitiesCurrentValue, BigDecimal cashBalance, BigDecimal maintenanceMargin, double leverage, double allocation,
            StrategyImpl strategy) {
        this.dateTime = dateTime;
        this.netLiqValue = netLiqValue;
        this.securitiesCurrentValue = securitiesCurrentValue;
        this.cashBalance = cashBalance;
        this.maintenanceMargin = maintenanceMargin;
        this.leverage = leverage;
        this.allocation = allocation;
        this.strategy = strategy;
    }

    public PortfolioValueImpl(Date dateTime, BigDecimal netLiqValue, BigDecimal securitiesCurrentValue, BigDecimal cashBalance, BigDecimal maintenanceMargin, double leverage, double allocation,
            BigDecimal cashFlow, StrategyImpl strategy) {
        this.dateTime = dateTime;
        this.netLiqValue = netLiqValue;
        this.securitiesCurrentValue = securitiesCurrentValue;
        this.cashBalance = cashBalance;
        this.maintenanceMargin = maintenanceMargin;
        this.leverage = leverage;
        this.allocation = allocation;
        this.cashFlow = cashFlow;
        this.strategy = strategy;
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
     *      * The dateTime of this PortfolioValue
     */

    @Column(name = "DATE_TIME", nullable = false, columnDefinition = "TIMESTAMP")
    public Date getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    /**
     *      * Current market value of all Assets. {@code cashBalance} + {@code securitiesCurrentValue}
     */

    @Column(name = "NET_LIQ_VALUE", nullable = false, columnDefinition = "Decimal(15,6)")
    public BigDecimal getNetLiqValue() {
        return this.netLiqValue;
    }

    public void setNetLiqValue(BigDecimal netLiqValue) {
        this.netLiqValue = netLiqValue;
    }

    /**
     *      * Current market value of all positions.
     */

    @Column(name = "SECURITIES_CURRENT_VALUE", nullable = false, columnDefinition = "Decimal(15,6)")
    public BigDecimal getSecuritiesCurrentValue() {
        return this.securitiesCurrentValue;
    }

    public void setSecuritiesCurrentValue(BigDecimal securitiesCurrentValue) {
        this.securitiesCurrentValue = securitiesCurrentValue;
    }

    /**
     *      * Total cash
     */

    @Column(name = "CASH_BALANCE", nullable = false, columnDefinition = "Decimal(15,6)")
    public BigDecimal getCashBalance() {
        return this.cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    /**
     *      * Current Maintenance Margin of all marginable positions
     */

    @Column(name = "MAINTENANCE_MARGIN", nullable = false, columnDefinition = "Decimal(15,6)")
    public BigDecimal getMaintenanceMargin() {
        return this.maintenanceMargin;
    }

    public void setMaintenanceMargin(BigDecimal maintenanceMargin) {
        this.maintenanceMargin = maintenanceMargin;
    }

    /**
     *      * Current (delta-adjusted) Notional Exposure
     */

    @Column(name = "LEVERAGE", nullable = false, columnDefinition = "DOUBLE")
    public double getLeverage() {
        return this.leverage;
    }

    public void setLeverage(double leverage) {
        this.leverage = leverage;
    }

    /**
     *      * Allocation assigned to the Base / Strategy.
     * p
     * The total of all allocations needs to be {@code 1.0}
     */

    @Column(name = "ALLOCATION", nullable = false, columnDefinition = "DOUBLE")
    public double getAllocation() {
        return this.allocation;
    }

    public void setAllocation(double allocation) {
        this.allocation = allocation;
    }

    /**
     *      * CashFlow value occurred at the specified time.
     * PortfolioValues are recorded every time a transaction occurs that influences performance.
     * For the Base these are {@code CREDIT} and {@code DEBIT} and for strategies, these are {@code REBALANCE}
     */

    @Column(name = "CASH_FLOW", columnDefinition = "Decimal(15,6)")
    public BigDecimal getCashFlow() {
        return this.cashFlow;
    }

    public void setCashFlow(BigDecimal cashFlow) {
        this.cashFlow = cashFlow;
    }

    /**
     *      * Represents a running Strategy within the system. In addition the Base is also represented by an
     * instance of this class.
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STRATEGY_FK", nullable = false, columnDefinition = "INTEGER")
    public StrategyImpl getStrategy() {
        return this.strategy;
    }

    public void setStrategy(StrategyImpl strategy) {
        this.strategy = strategy;
    }

}
