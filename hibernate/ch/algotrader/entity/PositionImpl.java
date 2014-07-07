package ch.algotrader.entity;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.property.PropertyImpl;
import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.entity.strategy.StrategyImpl;
import java.math.BigDecimal;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Base class of an Entity that can hold {@link Property Properties}.
 */
@Entity
@Table(name = "position")
public class PositionImpl extends ch.algotrader.entity.property.PropertyHolderImpl implements java.io.Serializable {

    /**
     * The current quantity of this Position.
    */
    private long quantity;
    /**
     * the cost associated with the current holdings of this Position. Based on the average cost method.
    */
    private double cost;
    /**
     * the realized Profit-and-Loss of this Position
    */
    private double realizedPL;
    /**
     * The price at which the Position will be closed. If an exitValue is defined, the Position will be closed using the defined {@link ch.algotrader.entity.strategy.DefaultOrderPreference DefaultOrderPreference}. A Position is closed by the Base itself, therefore a corresponding Strategy does not have to be running.
    */
    private BigDecimal exitValue;
    /**
     * The current margin needed by this Position.
    */
    private BigDecimal maintenanceMargin;
    /**
     * If set to {@code false}, the Position will be removed when resetting the database before a simulation run.
    */
    private boolean persistent;
    /**
     * Represents a running Strategy within the system. In addition the Base is also represented by an
    * instance of this class.
    */
    private StrategyImpl strategy;
    /**
     * The base class of all Securities in the system
    */
    private SecurityImpl security;

    public PositionImpl() {
    }

    public PositionImpl(long quantity, double cost, double realizedPL, boolean persistent, StrategyImpl strategy, SecurityImpl security) {
        this.quantity = quantity;
        this.cost = cost;
        this.realizedPL = realizedPL;
        this.persistent = persistent;
        this.strategy = strategy;
        this.security = security;
    }

    public PositionImpl(Map<String, PropertyImpl> props, long quantity, double cost, double realizedPL, BigDecimal exitValue, BigDecimal maintenanceMargin, boolean persistent, StrategyImpl strategy,
            SecurityImpl security) {
        super(props);
        this.quantity = quantity;
        this.cost = cost;
        this.realizedPL = realizedPL;
        this.exitValue = exitValue;
        this.maintenanceMargin = maintenanceMargin;
        this.persistent = persistent;
        this.strategy = strategy;
        this.security = security;
    }

    /**
     *      * The current quantity of this Position.
     */

    @Column(name = "QUANTITY", nullable = false, columnDefinition = "BIGINT")
    public long getQuantity() {
        return this.quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    /**
     *      * the cost associated with the current holdings of this Position. Based on the average cost method.
     */

    @Column(name = "COST", nullable = false, columnDefinition = "DOUBLE")
    public double getCost() {
        return this.cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     *      * the realized Profit-and-Loss of this Position
     */

    @Column(name = "REALIZED_P_L", nullable = false, columnDefinition = "DOUBLE")
    public double getRealizedPL() {
        return this.realizedPL;
    }

    public void setRealizedPL(double realizedPL) {
        this.realizedPL = realizedPL;
    }

    /**
     *      * The price at which the Position will be closed. If an exitValue is defined, the Position will be closed using the defined {@link ch.algotrader.entity.strategy.DefaultOrderPreference DefaultOrderPreference}. A Position is closed by the Base itself, therefore a corresponding Strategy does not have to be running.
     */

    @Column(name = "EXIT_VALUE", columnDefinition = "Decimal(15,6)")
    public BigDecimal getExitValue() {
        return this.exitValue;
    }

    public void setExitValue(BigDecimal exitValue) {
        this.exitValue = exitValue;
    }

    /**
     *      * The current margin needed by this Position.
     */

    @Column(name = "MAINTENANCE_MARGIN", columnDefinition = "Decimal(15,6)")
    public BigDecimal getMaintenanceMargin() {
        return this.maintenanceMargin;
    }

    public void setMaintenanceMargin(BigDecimal maintenanceMargin) {
        this.maintenanceMargin = maintenanceMargin;
    }

    /**
     *      * If set to {@code false}, the Position will be removed when resetting the database before a simulation run.
     */

    @Column(name = "PERSISTENT", nullable = false, columnDefinition = "TINYINT")
    public boolean isPersistent() {
        return this.persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
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
