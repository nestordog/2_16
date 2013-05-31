/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algorader.adapter.ib;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import ch.algorader.entity.strategy.StrategyImpl;
import ch.algorader.esper.EsperManager;
import ch.algorader.util.MyLogger;
import ch.algorader.util.RoundUtil;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.trade.Fill;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.entity.trade.OrderStatus;
import com.algoTrader.enumeration.Side;
import com.algoTrader.enumeration.Status;
import com.ib.client.Contract;
import com.ib.client.EWrapperMsgGenerator;
import com.ib.client.Execution;

/**
 * Esper specific MessageHandler.
 * Relevant events are sent into the BASE Esper Engine.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public final class IBEsperMessageHandler extends IBDefaultMessageHandler {

    private static Logger logger = MyLogger.getLogger(IBEsperMessageHandler.class.getName());

    public IBEsperMessageHandler(int clientId) {
        super(clientId);
    }

    @Override
    public void connectionClosed() {

        super.connectionClosed();

        //if connection gets closed, try to reconnect
        ServiceLocator.instance().getService("iBClientFactory", IBClientFactory.class).getDefaultClient().connect();
        logger.debug(EWrapperMsgGenerator.connectionClosed());
    }

    // Override EWrapper methods (create events, send them into esper and log them)

    @Override
    public void execDetails(final int reqId, final Contract contract, final Execution execution) {

        // ignore FA transfer execution reporst
        if (execution.m_execId.startsWith("F-") || execution.m_execId.startsWith("U+")) {
            return;
        }

        String intId = String.valueOf(execution.m_orderId);

        // get the order from the OpenOrderWindow
        Order order = ServiceLocator.instance().getLookupService().getOpenOrderByIntId(intId);
        if (order == null) {
            logger.error("order could not be found " + intId + " for execution " + contract + " " + execution);
            return;
        }

        // get the fields
        Date extDateTime = IBUtil.getExecutionDateTime(execution);
        Side side = IBUtil.getSide(execution);
        long quantity = execution.m_shares;
        BigDecimal price = RoundUtil.getBigDecimal(execution.m_price, order.getSecurity().getSecurityFamily().getScale());
        String extExecId = execution.m_execId;

        // assemble the fill
        Fill fill = Fill.Factory.newInstance();
        fill.setDateTime(new Date());
        fill.setExtDateTime(extDateTime);
        fill.setSide(side);
        fill.setQuantity(quantity);
        fill.setPrice(price);
        fill.setExtId(extExecId);

        // associate the fill with the order
        order.addFills(fill);

        logger.debug(EWrapperMsgGenerator.execDetails(reqId, contract, execution));

        EsperManager.sendEvent(StrategyImpl.BASE, fill);
    }

    @Override
    public void orderStatus(final int orderId, final String statusString, final int filled, final int remaining, final double avgFillPrice, final int permId,
            final int parentId, final double lastFillPrice, final int clientId, final String whyHeld) {

        // get the order from the OpenOrderWindow
        Order order = ServiceLocator.instance().getLookupService().getOpenOrderByIntId(String.valueOf(orderId));

        if (order != null) {

            // get the fields
            Status status = IBUtil.getStatus(statusString, filled);
            long filledQuantity = filled;
            long remainingQuantity = remaining;

            // assemble the orderStatus
            OrderStatus orderStatus = OrderStatus.Factory.newInstance();
            orderStatus.setStatus(status);
            orderStatus.setFilledQuantity(filledQuantity);
            orderStatus.setRemainingQuantity(remainingQuantity);
            orderStatus.setOrd(order);

            logger.debug(EWrapperMsgGenerator.orderStatus(orderId, statusString, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld));

            EsperManager.sendEvent(StrategyImpl.BASE, orderStatus);
        }
    }

    @Override
    public void tickPrice(final int tickerId, final int field, final double price, final int canAutoExecute) {

        logger.trace(EWrapperMsgGenerator.tickPrice(tickerId, field, price, canAutoExecute));

        TickPrice o = new TickPrice(tickerId, field, price, canAutoExecute);
        EsperManager.sendEvent(StrategyImpl.BASE, o);
    }

    @Override
    public void tickSize(final int tickerId, final int field, final int size) {
        logger.trace(EWrapperMsgGenerator.tickSize(tickerId, field, size));

        TickSize o = new TickSize(tickerId, field, size);
        EsperManager.sendEvent(StrategyImpl.BASE, o);
    }

    @Override
    public void tickString(final int tickerId, final int tickType, final String value) {

        logger.trace(EWrapperMsgGenerator.tickString(tickerId, tickType, value));

        TickString o = new TickString(tickerId, tickType, value);
        EsperManager.sendEvent(StrategyImpl.BASE, o);
    }
}
