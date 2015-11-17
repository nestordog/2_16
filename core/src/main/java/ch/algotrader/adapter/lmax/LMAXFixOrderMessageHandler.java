/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.adapter.lmax;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.fix.DropCopyAllocationVO;
import ch.algotrader.adapter.fix.DropCopyAllocator;
import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.adapter.fix.FixUtil;
import ch.algotrader.adapter.fix.fix44.GenericFix44OrderMessageHandler;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.ExternalFill;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.TransactionService;
import ch.algotrader.util.PriceUtil;
import ch.algotrader.util.RoundUtil;
import quickfix.FieldNotFound;
import quickfix.field.AvgPx;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.MsgSeqNum;
import quickfix.field.TransactTime;
import quickfix.fix44.ExecutionReport;

/**
 * LMFX specific Fix44MessageHandler.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class LMAXFixOrderMessageHandler extends GenericFix44OrderMessageHandler {

    private static final double MULTIPLIER = 10000.0;

    private static final Logger LOGGER = LogManager.getLogger(LMAXFixOrderMessageHandler.class);

    private final OrderExecutionService orderExecutionService;
    private final TransactionService transactionService;
    private final Engine serverEngine;
    private final DropCopyAllocator dropCopyAllocator;

    public LMAXFixOrderMessageHandler(final OrderExecutionService orderExecutionService, final TransactionService transactionService, final Engine serverEngine, final DropCopyAllocator dropCopyAllocator) {
        super(orderExecutionService, transactionService, serverEngine);
        this.orderExecutionService = orderExecutionService;
        this.transactionService = transactionService;
        this.serverEngine = serverEngine;
        this.dropCopyAllocator = dropCopyAllocator;
    }

    @Override
    protected void handleUnknown(final ExecutionReport executionReport) throws FieldNotFound {

        if (this.dropCopyAllocator == null) {
            super.handleUnknown(executionReport);
        } else {
            handleExternalReport(executionReport);
        }
    }

    private void handleExternalReport(final ExecutionReport executionReport) throws FieldNotFound {

        String extId = executionReport.getOrderID().getValue();
        ExecType execType = executionReport.getExecType();
        long orderQty = executionReport.isSetOrderQty() ? (long) executionReport.getOrderQty().getValue() : 0L;
        long cumQty = executionReport.isSetCumQty() ? (long) executionReport.getCumQty().getValue() : 0L;
        Status status = getStatus(execType, orderQty, cumQty);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received order status {} for external order {}", status, extId);
        }

        if (status != Status.PARTIALLY_EXECUTED && status != Status.EXECUTED) {
            return;
        }

        String lmaxid = executionReport.getSecurityID().getValue();
        String extAccount = executionReport.getAccount().getValue();

        DropCopyAllocationVO allocation = this.dropCopyAllocator.allocate(lmaxid, extAccount);
        if (allocation == null || allocation.getSecurity() == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Unable to allocate an external fill: " +
                        "extID = {}, LMAX security id = {}, external account = {}", extId, lmaxid, extAccount);
            }
            return;
        }
        if (allocation.getStrategy() == null) {
            throw new FixApplicationException("External fill could not be allocated to a strategy");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("External (drop-copy) fill {} allocated to " +
                    "strategy {}, account = {}", extId, allocation.getStrategy(), allocation.getAccount());
        }

        ExternalFill fill = createFill(executionReport, allocation);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(fill);
        }

        if (fill != null) {
            this.serverEngine.sendEvent(fill);
            this.transactionService.createTransaction(fill);
            this.orderExecutionService.handleFill(fill);
        }
    }

    @Override
    protected OrderStatus createStatus(final ExecutionReport executionReport, final Order order) throws FieldNotFound {

        ExecType execType = executionReport.getExecType();
        Status status = getStatus(execType, (long) executionReport.getOrderQty().getValue(), (long) executionReport.getCumQty().getValue());
        long filledQuantity = Math.round(executionReport.getCumQty().getValue() * MULTIPLIER);
        long remainingQuantity = Math.round((executionReport.getOrderQty().getValue() - executionReport.getCumQty().getValue()) * MULTIPLIER);
        long lastQuantity = Math.round((executionReport.isSetLastQty() ? (long) executionReport.getLastQty().getValue() : 0L) * MULTIPLIER);

        String intId = order.getIntId() != null ? order.getIntId(): executionReport.getClOrdID().getValue();
        String extId = executionReport.getOrderID().getValue();

        // assemble the orderStatus
        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setStatus(status);
        orderStatus.setIntId(intId);
        orderStatus.setExtId(extId);
        orderStatus.setSequenceNumber(executionReport.getHeader().getInt(MsgSeqNum.FIELD));
        orderStatus.setFilledQuantity(filledQuantity);
        orderStatus.setRemainingQuantity(remainingQuantity);
        orderStatus.setLastQuantity(lastQuantity);
        orderStatus.setOrder(order);
        if (executionReport.isSetField(TransactTime.FIELD)) {

            orderStatus.setExtDateTime(executionReport.getTransactTime().getValue());
        }
        if (executionReport.isSetField(LastPx.FIELD)) {

            double lastPrice = executionReport.getLastPx().getValue();
            if (lastPrice != 0.0) {
                orderStatus.setLastPrice(PriceUtil.normalizePrice(order, lastPrice));
            }
        }
        if (executionReport.isSetField(AvgPx.FIELD)) {

            double avgPrice = executionReport.getAvgPx().getValue();
            if (avgPrice != 0.0) {
                orderStatus.setAvgPrice(PriceUtil.normalizePrice(order, avgPrice));
            }
        }

        return orderStatus;
    }

    @Override
    protected Fill createFill(ExecutionReport executionReport, Order order) throws FieldNotFound {

        ExecType execType = executionReport.getExecType();
        // only create fills if status is TRADE
        if (execType.getValue() == ExecType.TRADE) {

            // get the fields
            Date extDateTime = executionReport.getTransactTime().getValue();
            Side side = FixUtil.getSide(executionReport.getSide());
            long quantity = Math.round(executionReport.getLastQty().getValue() * MULTIPLIER);
            double price = executionReport.getLastPx().getValue();
            String extId = executionReport.getExecID().getValue();

            // assemble the fill
            Fill fill = new Fill();
            fill.setDateTime(new Date());
            fill.setExtDateTime(extDateTime);
            fill.setExtId(extId);
            fill.setSequenceNumber(executionReport.getHeader().getInt(MsgSeqNum.FIELD));
            fill.setSide(side);
            fill.setQuantity(quantity);
            fill.setPrice(PriceUtil.normalizePrice(order,price));
            fill.setOrder(order);

            return fill;
        } else {

            return null;
        }
    }

    protected ExternalFill createFill(final ExecutionReport executionReport, final DropCopyAllocationVO allocation) throws FieldNotFound {

        char execType = executionReport.getExecType().getValue();
        if (execType != ExecType.PARTIAL_FILL && execType != ExecType.FILL && execType != ExecType.TRADE) {
            throw new IllegalArgumentException("Unexpected execType: " + execType);
        }

        Security security = allocation.getSecurity();
        SecurityFamily securityFamily = security != null ? security.getSecurityFamily() : null;

        Currency currency = null;
        if (executionReport.isSetCurrency()) {
            String s = executionReport.getCurrency().getValue();
            try {
                currency = Currency.valueOf(s);
            } catch (IllegalArgumentException ex) {
                throw new FixApplicationException("Unsupported currency " + s);
            }
        }
        if (securityFamily != null) {
            if (currency != null) {
                if (!currency.equals(securityFamily.getCurrency())) {
                    throw new FixApplicationException("Transaction currency does not match that defined by the security family");
                }
            } else {
                currency = securityFamily.getCurrency();
            }
        }

        Side side = FixUtil.getSide(executionReport.getSide());
        long quantity = Math.round(executionReport.getLastQty().getValue() * MULTIPLIER);

        Account account = allocation.getAccount();
        String broker = account != null ? account.getBroker() : getDefaultBroker();

        double price = executionReport.getLastPx().getValue();

        BigDecimal normalizedPrice;
        if (securityFamily != null) {
            double priceMultiplier = securityFamily.getPriceMultiplier(broker);
            normalizedPrice = RoundUtil.getBigDecimal(price / priceMultiplier, securityFamily.getScale());
        } else {
            normalizedPrice = new BigDecimal(price);
        }

        Strategy strategy = allocation.getStrategy();
        String extOrderId = executionReport.isSetOrderID() ? executionReport.getOrderID().getValue() : null;
        String extId = executionReport.getExecID().getValue();

        // assemble the fill
        ExternalFill fill = new ExternalFill();
        fill.setSecurity(security);
        fill.setCurrency(currency);
        fill.setAccount(account);
        fill.setStrategy(strategy);
        fill.setDateTime(new Date());
        fill.setExtOrderId(extOrderId);
        fill.setExtId(extId);
        fill.setSequenceNumber(executionReport.getHeader().getInt(MsgSeqNum.FIELD));
        fill.setSide(side);
        fill.setQuantity(quantity);
        fill.setPrice(normalizedPrice);
        if (executionReport.isSetField(TransactTime.FIELD)) {
            fill.setExtDateTime(executionReport.getTransactTime().getValue());
        }
        return fill;
    }

    @Override
    protected String getDefaultBroker() {

        return Broker.LMAX.name();
    }

}
