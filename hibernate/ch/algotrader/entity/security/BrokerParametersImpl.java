package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.enumeration.Broker;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Represents broker specific variants of parameters defined by the {@link SecurityFamily}
 */
@Entity
@Table(name = "broker_parameters")
public class BrokerParametersImpl implements java.io.Serializable {

    private int id;
    /**
     * The {@link Broker} which these parameters apply to.
    */
    private Broker broker;
    /**
     * The common part of Symbol (e.g. VIX for the VIX Future FVIX AUG/10 1000)
    */
    private String symbolRoot;
    /**
     * The name of the market where the Securities of this Family are traded.
    */
    private String exchangeCode;
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
     * Represents an entire family of similar {@link ch.algotrader.entity.security.Security Securities}
    * (e.g. All Options of the SP500)
    */
    private SecurityFamilyImpl securityFamily;

    public BrokerParametersImpl() {
    }

    public BrokerParametersImpl(Broker broker, SecurityFamilyImpl securityFamily) {
        this.broker = broker;
        this.securityFamily = securityFamily;
    }

    public BrokerParametersImpl(Broker broker, String symbolRoot, String exchangeCode, BigDecimal executionCommission, BigDecimal clearingCommission, BigDecimal fee, SecurityFamilyImpl securityFamily) {
        this.broker = broker;
        this.symbolRoot = symbolRoot;
        this.exchangeCode = exchangeCode;
        this.executionCommission = executionCommission;
        this.clearingCommission = clearingCommission;
        this.fee = fee;
        this.securityFamily = securityFamily;
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
     *      * The {@link Broker} which these parameters apply to.
     */

    @Column(name = "BROKER", nullable = false, columnDefinition = "VARCHAR(255)")
    public Broker getBroker() {
        return this.broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
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
     *      * The name of the market where the Securities of this Family are traded.
     */

    @Column(name = "EXCHANGE_CODE", columnDefinition = "VARCHAR(255)")
    public String getExchangeCode() {
        return this.exchangeCode;
    }

    public void setExchangeCode(String exchangeCode) {
        this.exchangeCode = exchangeCode;
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
     *      * Represents an entire family of similar {@link ch.algotrader.entity.security.Security Securities}
     * (e.g. All Options of the SP500)
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECURITY_FAMILY_FK", nullable = false, columnDefinition = "INTEGER")
    public SecurityFamilyImpl getSecurityFamily() {
        return this.securityFamily;
    }

    public void setSecurityFamily(SecurityFamilyImpl securityFamily) {
        this.securityFamily = securityFamily;
    }

}
