package ch.algotrader.entity;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.TransactionType;
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
 * A transaction stored in the database. Each Fill is recorded as a transaction using this entity. In addition the table transaction also stores transactions like intrest, debit, credit  fees.
 */
@Entity
@Table(name = "transaction")
public class TransactionImpl implements java.io.Serializable {

    private int id;
    /**
     * The dateTime of the Transaction
    */
    private Date dateTime;
    /**
     * The dateTime this Transaction gets settled
    */
    private Date settlementDate;
    /**
     * The external Transaction Id
    */
    private String extId;
    /**
     * The internal Order Id
    */
    private String intOrderId;
    /**
     * The external Order Id
    */
    private String extOrderId;
    /**
     * The quantity of the Transaction. For different {@link TransactionType TransactionTypes} quantities are as follows:
    * ul
    * liBUY: pos/li
    * liSELL: neg/li
    * liEXPIRATION: pos/neg/li
    * liTRANSFER : pos/neg/li
    * liCREDIT: 1/li
    * liINTREST_RECEIVED: 1/li
    * liREFUND : 1/li
    * liDIVIDEND : 1/li
    * liDEBIT: -1/li
    * liINTREST_PAID: -1/li
    * liFEES: -1/li
    * /ul
    */
    private long quantity;
    /**
     * The price of this Transaction. Is always positive
    */
    private BigDecimal price;
    /**
     * The Execution Commission of this Transaction.
    */
    private BigDecimal executionCommission;
    /**
     * The Clearing Commission of this Transaction.
    */
    private BigDecimal clearingCommission;
    /**
     * The Exchange Fees of this Transaction.
    */
    private BigDecimal fee;
    /**
     * The {@link Currency} of this Position.
    */
    private Currency currency;
    /**
     * The {@link TransactionType}
    */
    private TransactionType type;
    /**
     * An arbitrary Description of the Transaction
    */
    private String description;
    /**
     * Represents an actual Account / AccountGroup / AllocationProfile with an external Broker / Bank
    */
    private AccountImpl account;
    /**
     * A position of a particular security owned by a particular strategy. For each opening transaction a
    * position is created. The position object remains in place even if a corresponding closing
    * transaction is carried out and the quantity of the position becomes 0.
    * p
    * Since some values (e.g. {@code marketValue}) depend on whether the position is long or short,
    * aggregated position values for the same security (of different strategies) cannot be retrieved just
    * by adding position values from the corresponding strategies.
    * p
    * Example:
    * ul
    * liSecurity: VIX Dec 2012/li
    * liCurrent Bid: 16.50/li
    * liCurrent Ask: 16.60/li
    * liStrategy A: quantity +10 - marketValue: 10 * 1000 * 16.50 = 165000/li
    * liStrategy B: quantity -10 - marketValue: 10 * 1000 * 16.60 = -166000/li
    * /ul
    * p
    * The sum of above marketValues would be -1000 which is obviously wrong.
    * p
    * As a consequence the {@code PortfolioDAO} provides lookup-methods that aggregate positions from the
    * same security (of different strategies) in the correct manner.
    */
    private PositionImpl position;
    /**
     * The base class of all Securities in the system
    */
    private SecurityImpl security;
    /**
     * Represents a running Strategy within the system. In addition the Base is also represented by an
    * instance of this class.
    */
    private StrategyImpl strategy;

    public TransactionImpl() {
    }

    public TransactionImpl(Date dateTime, long quantity, BigDecimal price, Currency currency, TransactionType type, StrategyImpl strategy) {
        this.dateTime = dateTime;
        this.quantity = quantity;
        this.price = price;
        this.currency = currency;
        this.type = type;
        this.strategy = strategy;
    }

    public TransactionImpl(Date dateTime, Date settlementDate, String extId, String intOrderId, String extOrderId, long quantity, BigDecimal price, BigDecimal executionCommission,
            BigDecimal clearingCommission, BigDecimal fee, Currency currency, TransactionType type, String description, AccountImpl account, PositionImpl position, SecurityImpl security,
            StrategyImpl strategy) {
        this.dateTime = dateTime;
        this.settlementDate = settlementDate;
        this.extId = extId;
        this.intOrderId = intOrderId;
        this.extOrderId = extOrderId;
        this.quantity = quantity;
        this.price = price;
        this.executionCommission = executionCommission;
        this.clearingCommission = clearingCommission;
        this.fee = fee;
        this.currency = currency;
        this.type = type;
        this.description = description;
        this.account = account;
        this.position = position;
        this.security = security;
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
     *      * The dateTime of the Transaction
     */

    @Column(name = "DATE_TIME", nullable = false, columnDefinition = "TIMESTAMP")
    public Date getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    /**
     *      * The dateTime this Transaction gets settled
     */

    @Column(name = "SETTLEMENT_DATE", columnDefinition = "DATETIME")
    public Date getSettlementDate() {
        return this.settlementDate;
    }

    public void setSettlementDate(Date settlementDate) {
        this.settlementDate = settlementDate;
    }

    /**
     *      * The external Transaction Id
     */

    @Column(name = "EXT_ID", unique = true, columnDefinition = "VARCHAR(255)")
    public String getExtId() {
        return this.extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    /**
     *      * The internal Order Id
     */

    @Column(name = "INT_ORDER_ID", unique = true, columnDefinition = "VARCHAR(255)")
    public String getIntOrderId() {
        return this.intOrderId;
    }

    public void setIntOrderId(String intOrderId) {
        this.intOrderId = intOrderId;
    }

    /**
     *      * The external Order Id
     */

    @Column(name = "EXT_ORDER_ID", unique = true, columnDefinition = "VARCHAR(255)")
    public String getExtOrderId() {
        return this.extOrderId;
    }

    public void setExtOrderId(String extOrderId) {
        this.extOrderId = extOrderId;
    }

    /**
     *      * The quantity of the Transaction. For different {@link TransactionType TransactionTypes} quantities are as follows:
     * ul
     * liBUY: pos/li
     * liSELL: neg/li
     * liEXPIRATION: pos/neg/li
     * liTRANSFER : pos/neg/li
     * liCREDIT: 1/li
     * liINTREST_RECEIVED: 1/li
     * liREFUND : 1/li
     * liDIVIDEND : 1/li
     * liDEBIT: -1/li
     * liINTREST_PAID: -1/li
     * liFEES: -1/li
     * /ul
     */

    @Column(name = "QUANTITY", nullable = false, columnDefinition = "BIGINT")
    public long getQuantity() {
        return this.quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    /**
     *      * The price of this Transaction. Is always positive
     */

    @Column(name = "PRICE", nullable = false, columnDefinition = "Decimal(15,6)")
    public BigDecimal getPrice() {
        return this.price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     *      * The Execution Commission of this Transaction.
     */

    @Column(name = "EXECUTION_COMMISSION", columnDefinition = "Decimal(15,6)")
    public BigDecimal getExecutionCommission() {
        return this.executionCommission;
    }

    public void setExecutionCommission(BigDecimal executionCommission) {
        this.executionCommission = executionCommission;
    }

    /**
     *      * The Clearing Commission of this Transaction.
     */

    @Column(name = "CLEARING_COMMISSION", columnDefinition = "Decimal(15,6)")
    public BigDecimal getClearingCommission() {
        return this.clearingCommission;
    }

    public void setClearingCommission(BigDecimal clearingCommission) {
        this.clearingCommission = clearingCommission;
    }

    /**
     *      * The Exchange Fees of this Transaction.
     */

    @Column(name = "FEE", columnDefinition = "Decimal(15,6)")
    public BigDecimal getFee() {
        return this.fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    /**
     *      * The {@link Currency} of this Position.
     */

    @Column(name = "CURRENCY", nullable = false, columnDefinition = "VARCHAR(255)")
    public Currency getCurrency() {
        return this.currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    /**
     *      * The {@link TransactionType}
     */

    @Column(name = "TYPE", nullable = false, columnDefinition = "VARCHAR(255)")
    public TransactionType getType() {
        return this.type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    /**
     *      * An arbitrary Description of the Transaction
     */

    @Column(name = "DESCRIPTION", columnDefinition = "VARCHAR(255)")
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *      * Represents an actual Account / AccountGroup / AllocationProfile with an external Broker / Bank
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_FK", columnDefinition = "INTEGER")
    public AccountImpl getAccount() {
        return this.account;
    }

    public void setAccount(AccountImpl account) {
        this.account = account;
    }

    /**
     *      * A position of a particular security owned by a particular strategy. For each opening transaction a
     * position is created. The position object remains in place even if a corresponding closing
     * transaction is carried out and the quantity of the position becomes 0.
     * p
     * Since some values (e.g. {@code marketValue}) depend on whether the position is long or short,
     * aggregated position values for the same security (of different strategies) cannot be retrieved just
     * by adding position values from the corresponding strategies.
     * p
     * Example:
     * ul
     * liSecurity: VIX Dec 2012/li
     * liCurrent Bid: 16.50/li
     * liCurrent Ask: 16.60/li
     * liStrategy A: quantity +10 - marketValue: 10 * 1000 * 16.50 = 165000/li
     * liStrategy B: quantity -10 - marketValue: 10 * 1000 * 16.60 = -166000/li
     * /ul
     * p
     * The sum of above marketValues would be -1000 which is obviously wrong.
     * p
     * As a consequence the {@code PortfolioDAO} provides lookup-methods that aggregate positions from the
     * same security (of different strategies) in the correct manner.
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POSITION_FK", columnDefinition = "INTEGER")
    public PositionImpl getPosition() {
        return this.position;
    }

    public void setPosition(PositionImpl position) {
        this.position = position;
    }

    /**
     *      * The base class of all Securities in the system
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECURITY_FK", columnDefinition = "INTEGER")
    public SecurityImpl getSecurity() {
        return this.security;
    }

    public void setSecurity(SecurityImpl security) {
        this.security = security;
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
