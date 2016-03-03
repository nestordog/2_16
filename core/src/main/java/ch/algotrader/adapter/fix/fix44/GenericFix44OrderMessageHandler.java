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
package ch.algotrader.adapter.fix.fix44;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.BrokerAdapterException;
import ch.algotrader.adapter.fix.DropCopyAllocationVO;
import ch.algotrader.adapter.fix.FixUtil;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.ExternalFill;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.service.OrderExecutionService;
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
 * Generic Fix44OrderMessageHandler. Can still be overwritten by specific broker interfaces.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class GenericFix44OrderMessageHandler extends AbstractFix44OrderMessageHandler {

    private static final Logger LOGGER = LogManager.getLogger(GenericFix44OrderMessageHandler.class);

    public GenericFix44OrderMessageHandler(final OrderExecutionService orderExecutionService) {
        super(orderExecutionService);
    }

    @Override
    protected boolean discardReport(final ExecutionReport executionReport) throws FieldNotFound {

        // ignore PENDING_NEW, PENDING_CANCEL and PENDING_REPLACE
        ExecType execType = executionReport.getExecType();

        if (execType.getValue() == ExecType.PENDING_NEW
                || execType.getValue() == ExecType.PENDING_REPLACE
                || execType.getValue() == ExecType.PENDING_CANCEL) {

            return true;
        } else {

            return false;
        }
    }

    @Override
    protected void handleExternal(final ExecutionReport executionReport) throws FieldNotFound {
    }

    @Override
    protected void handleUnknown(final ExecutionReport executionReport) throws FieldNotFound {

        if (LOGGER.isErrorEnabled() && executionReport.isSetClOrdID()) {
            String orderIntId = executionReport.getClOrdID().getValue();
            LOGGER.error("Cannot find open order with IntID {}", orderIntId);
        }
    }

    @Override
    protected void handleRestated(final ExecutionReport executionReport, final Order order) throws FieldNotFound {

        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("Cannot re-state order with IntID {}", order.getIntId());
        }
    }

    @Override
    protected boolean isOrderRejected(final ExecutionReport executionReport) throws FieldNotFound {

        ExecType execType = executionReport.getExecType();
        return execType.getValue() == ExecType.REJECTED;
    }

    @Override
    protected boolean isOrderReplaced(ExecutionReport executionReport) throws FieldNotFound {

        ExecType execType = executionReport.getExecType();
        return execType.getValue() == ExecType.REPLACE;
    }

    @Override
    protected boolean isOrderRestated(final ExecutionReport executionReport) throws FieldNotFound {
        ExecType execType = executionReport.getExecType();
        return execType.getValue() == ExecType.RESTATED;
    }

    @Override
    protected OrderStatus createStatus(final ExecutionReport executionReport, final Order order) throws FieldNotFound {

        ExecType execType = executionReport.getExecType();
        long orderQty;
        if (executionReport.isSetOrderQty()) {
            orderQty = (long) executionReport.getOrderQty().getValue();
        } else {
            orderQty = order.getQuantity();
        }
        long cumQty = (long) executionReport.getCumQty().getValue();

        Status status = getStatus(execType, orderQty, cumQty);
        long remainingQuantity = orderQty - cumQty;
        long lastQuantity = executionReport.isSetLastQty() ? (long) executionReport.getLastQty().getValue() : 0L;

        String intId = order.getIntId() != null ? order.getIntId(): executionReport.getClOrdID().getValue();
        String extId = executionReport.getOrderID().getValue();

        // assemble the orderStatus
        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setStatus(status);
        orderStatus.setExtId(extId);
        orderStatus.setIntId(intId);
        orderStatus.setSequenceNumber(executionReport.getHeader().getInt(MsgSeqNum.FIELD));
        orderStatus.setFilledQuantity(cumQty);
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
            Side side = FixUtil.getSide(executionReport.getSide());
            long quantity = (long) executionReport.getLastQty().getValue();
            double price = executionReport.getLastPx().getValue();
            String extId = executionReport.getExecID().getValue();

            // assemble the fill
            Fill fill = new Fill();
            fill.setExtId(extId);
            fill.setSequenceNumber(executionReport.getHeader().getInt(MsgSeqNum.FIELD));
            fill.setDateTime(new Date());
            fill.setSide(side);
            fill.setQuantity(quantity);
            fill.setPrice(PriceUtil.normalizePrice(order, price));
            if (executionReport.isSetField(TransactTime.FIELD)) {
                fill.setExtDateTime(executionReport.getTransactTime().getValue());
            }
            fill.setOrder(order);

            return fill;
        } else {

            return null;
        }
    }

    protected ExternalFill createFill(final ExecutionReport executionReport, final DropCopyAllocationVO allocation) throws FieldNotFound {

        char execType = executionReport.getExecType().getValue();
        if (execType != ExecType.PARTIAL_FILL && execType != ExecType.FILL) {
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
                throw new BrokerAdapterException("Unsupported currency " + s);
            }
        }
        if (securityFamily != null) {
            if (currency != null) {
                if (!currency.equals(securityFamily.getCurrency())) {
                    throw new BrokerAdapterException("Transaction currency does not match that defined by the security family");
                }
            } else {
                currency = securityFamily.getCurrency();
            }
        }

        Side side = FixUtil.getSide(executionReport.getSide());
        long quantity = (long) executionReport.getLastQty().getValue();

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
        return null;
    }

    static protected Status getStatus(final ExecType execType, final long orderQty, final long cumQty) {

        if (execType.getValue() == ExecType.NEW) {
            return Status.SUBMITTED;
        } else if (execType.getValue() == ExecType.TRADE) {
            if (cumQty == orderQty) {
                return Status.EXECUTED;
            } else {
                return Status.PARTIALLY_EXECUTED;
            }
        } else if (execType.getValue() == ExecType.CANCELED || execType.getValue() == ExecType.DONE_FOR_DAY
                || execType.getValue() == ExecType.EXPIRED) {
            return Status.CANCELED;
        } else if (execType.getValue() == ExecType.REJECTED) {
            return Status.REJECTED;
        } else if (execType.getValue() == ExecType.REPLACE) {
            if (cumQty == 0) {
                return Status.SUBMITTED;
            } else {
                return Status.PARTIALLY_EXECUTED;
            }
        } else if (execType.getValue() == ExecType.RESTATED) {
            return Status.SUBMITTED;
        } else {
            throw new IllegalArgumentException("unknown execType " + execType.getValue());
        }
    }

}
