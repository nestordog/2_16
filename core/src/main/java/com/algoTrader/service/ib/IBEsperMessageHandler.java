package com.algoTrader.service.ib;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.trade.Fill;
import com.algoTrader.entity.trade.OrderStatus;
import com.algoTrader.entity.trade.SimpleOrder;
import com.algoTrader.enumeration.Side;
import com.algoTrader.enumeration.Status;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.espertech.esper.collection.Pair;
import com.ib.client.Contract;
import com.ib.client.EWrapperMsgGenerator;
import com.ib.client.Execution;

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

    @SuppressWarnings("unchecked")
    @Override
    public void execDetails(final int reqId, final Contract contract, final Execution execution) {

        if (!(execution.m_execId.startsWith("F-")) && !(execution.m_execId.startsWith("U+"))) {

            int number = execution.m_orderId;

            // get the order from the OpenOrderWindow
            SimpleOrder order = ((Pair<SimpleOrder, Map<?, ?>>) EsperManager.executeSingelObjectQuery(StrategyImpl.BASE, "select * from OpenOrderWindow where number = " + number)).getFirst();
            if (order == null) {
                logger.error("order could not be found " + number + " for execution " + contract + " " + execution);
                return;
            }

            // get the fields
            Date dateTime = DateUtil.getCurrentEPTime();
            Date extDateTime = IBUtil.getExecutionDateTime(execution);
            Side side = IBUtil.getSide(execution);
            long quantity = execution.m_shares;
            BigDecimal price = RoundUtil.getBigDecimal(execution.m_price, order.getSecurity().getSecurityFamily().getScale());
            String extId = execution.m_execId;

            // assemble the fill
            Fill fill = Fill.Factory.newInstance();
            fill.setDateTime(dateTime);
            fill.setExtDateTime(extDateTime);
            fill.setSide(side);
            fill.setQuantity(quantity);
            fill.setPrice(price);
            fill.setExtId(extId);

            // associate the fill with the order
            order.addFills(fill);

            logger.debug(EWrapperMsgGenerator.execDetails(reqId, contract, execution));

            EsperManager.sendEvent(StrategyImpl.BASE, fill);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void orderStatus(final int orderId, final String statusString, final int filled, final int remaining, final double avgFillPrice, final int permId,
            final int parentId, final double lastFillPrice, final int clientId, final String whyHeld) {

        // get the order from the OpenOrderWindow
        Object object = EsperManager.executeSingelObjectQuery(StrategyImpl.BASE, "select * from OpenOrderWindow where number = " + orderId);

        if (object != null) {

            // get the fields
            SimpleOrder order = ((Pair<SimpleOrder, Map<?, ?>>) object).getFirst();
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
