package ch.algotrader.entity.strategy;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.property.PropertyImpl;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Base class of an Entity that can hold {@link Property Properties}.
 */
@Entity
@Table(name = "strategy")
public class StrategyImpl extends ch.algotrader.entity.property.PropertyHolderImpl implements java.io.Serializable {

    private String name;
    /**
     * In simulation mode all Strategies marked as autoActivate are started
    */
    private boolean autoActivate;
    /**
     * Allocation assigned to this strategy
    * p
    * iNote: the total of all Allocations needs to be 1.0/i
    */
    private double allocation;
    /**
     * Comma separated List of modules (e.g. {@code xyz} for {@code module-xyz.epl}). These modules will be deployed before the prefeeding.
    */
    private String initModules;
    /**
     * Comma separated List of modules (e.g. {@code xyz} for {@code module-xyz.epl}). These modules will be deployed after the prefeeding.
    */
    private String runModules;
    /**
     * Represents a running Strategy within the system. In addition the Base is also represented by an
    * instance of this class.
    */
    private Set<PortfolioValueImpl> portfolioValues = new HashSet<PortfolioValueImpl>(0);
    /**
     * Represents a running Strategy within the system. In addition the Base is also represented by an
    * instance of this class.
    */
    private Set<CashBalanceImpl> cashBalances = new HashSet<CashBalanceImpl>(0);
    /**
     * Represents a running Strategy within the system. In addition the Base is also represented by an
    * instance of this class.
    */
    private Set<MeasurementImpl> measurements = new HashSet<MeasurementImpl>(0);
    /**
     * Represents a running Strategy within the system. In addition the Base is also represented by an
    * instance of this class.
    */
    private Set<DefaultOrderPreferenceImpl> defaultOrderPreferences = new HashSet<DefaultOrderPreferenceImpl>(0);

    public StrategyImpl() {
    }

    public StrategyImpl(String name, boolean autoActivate, double allocation) {
        this.name = name;
        this.autoActivate = autoActivate;
        this.allocation = allocation;
    }

    public StrategyImpl(Map<String, PropertyImpl> props, String name, boolean autoActivate, double allocation, String initModules, String runModules, Set<PortfolioValueImpl> portfolioValues,
            Set<CashBalanceImpl> cashBalances, Set<MeasurementImpl> measurements, Set<DefaultOrderPreferenceImpl> defaultOrderPreferences) {
        super(props);
        this.name = name;
        this.autoActivate = autoActivate;
        this.allocation = allocation;
        this.initModules = initModules;
        this.runModules = runModules;
        this.portfolioValues = portfolioValues;
        this.cashBalances = cashBalances;
        this.measurements = measurements;
        this.defaultOrderPreferences = defaultOrderPreferences;
    }

    @Column(name = "NAME", unique = true, nullable = false, columnDefinition = "VARCHAR(255)")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *      * In simulation mode all Strategies marked as autoActivate are started
     */

    @Column(name = "AUTO_ACTIVATE", nullable = false, columnDefinition = "TINYINT")
    public boolean isAutoActivate() {
        return this.autoActivate;
    }

    public void setAutoActivate(boolean autoActivate) {
        this.autoActivate = autoActivate;
    }

    /**
     *      * Allocation assigned to this strategy
     * p
     * iNote: the total of all Allocations needs to be 1.0/i
     */

    @Column(name = "ALLOCATION", nullable = false, columnDefinition = "DOUBLE")
    public double getAllocation() {
        return this.allocation;
    }

    public void setAllocation(double allocation) {
        this.allocation = allocation;
    }

    /**
     *      * Comma separated List of modules (e.g. {@code xyz} for {@code module-xyz.epl}). These modules will be deployed before the prefeeding.
     */

    @Column(name = "INIT_MODULES", columnDefinition = "VARCHAR(255)")
    public String getInitModules() {
        return this.initModules;
    }

    public void setInitModules(String initModules) {
        this.initModules = initModules;
    }

    /**
     *      * Comma separated List of modules (e.g. {@code xyz} for {@code module-xyz.epl}). These modules will be deployed after the prefeeding.
     */

    @Column(name = "RUN_MODULES", columnDefinition = "VARCHAR(255)")
    public String getRunModules() {
        return this.runModules;
    }

    public void setRunModules(String runModules) {
        this.runModules = runModules;
    }

    /**
     *      * Represents a running Strategy within the system. In addition the Base is also represented by an
     * instance of this class.
     */

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "strategy")
    public Set<PortfolioValueImpl> getPortfolioValues() {
        return this.portfolioValues;
    }

    public void setPortfolioValues(Set<PortfolioValueImpl> portfolioValues) {
        this.portfolioValues = portfolioValues;
    }

    /**
     *      * Represents a running Strategy within the system. In addition the Base is also represented by an
     * instance of this class.
     */

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "strategy")
    public Set<CashBalanceImpl> getCashBalances() {
        return this.cashBalances;
    }

    public void setCashBalances(Set<CashBalanceImpl> cashBalances) {
        this.cashBalances = cashBalances;
    }

    /**
     *      * Represents a running Strategy within the system. In addition the Base is also represented by an
     * instance of this class.
     */

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "strategy")
    public Set<MeasurementImpl> getMeasurements() {
        return this.measurements;
    }

    public void setMeasurements(Set<MeasurementImpl> measurements) {
        this.measurements = measurements;
    }

    /**
     *      * Represents a running Strategy within the system. In addition the Base is also represented by an
     * instance of this class.
     */

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "strategy")
    public Set<DefaultOrderPreferenceImpl> getDefaultOrderPreferences() {
        return this.defaultOrderPreferences;
    }

    public void setDefaultOrderPreferences(Set<DefaultOrderPreferenceImpl> defaultOrderPreferences) {
        this.defaultOrderPreferences = defaultOrderPreferences;
    }

}
