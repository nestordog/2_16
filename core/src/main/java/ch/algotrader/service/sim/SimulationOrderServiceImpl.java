/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
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
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service.sim;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.LimitOrderI;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.Direction;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.service.LocalLookupService;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SimulationOrderServiceImpl implements SimulationOrderService {

    private final LocalLookupService localLookupService;
    private final OrderRegistry orderRegistry;
    private final EngineManager engineManager;
    private final Engine serverEngine;
    private final AtomicLong counter;

    public SimulationOrderServiceImpl(
            final OrderRegistry orderRegistry,
            final LocalLookupService localLookupService,
            final EngineManager engineManager,
            final Engine serverEngine) {

        Validate.notNull(orderRegistry, "OpenOrderRegistry is null");
        Validate.notNull(localLookupService, "LocalLookupService is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.orderRegistry = orderRegistry;
        this.engineManager = engineManager;
        this.serverEngine = serverEngine;
        this.localLookupService = localLookupService;
        this.counter = new AtomicLong(0);
    }

    @Override
    public void sendOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        String intId = order.getIntId();
        if (intId == null) {

            intId = getNextOrderId(order.getAccount());
            order.setIntId(intId);
        }

        this.orderRegistry.add(order);

        // create and OrderStatus
        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setStatus(Status.EXECUTED);
        orderStatus.setIntId(order.getIntId());
        orderStatus.setFilledQuantity(order.getQuantity());
        orderStatus.setRemainingQuantity(0);
        orderStatus.setOrder(order);

        // send the orderStatus to the AlgoTrader Server
        this.serverEngine.sendEvent(orderStatus);

        // create one fill per order
        Fill fill = new Fill();
        Date d = this.engineManager.getCurrentEPTime();
        fill.setDateTime(d);
        fill.setExtDateTime(d);
        fill.setSide(order.getSide());
        fill.setQuantity(order.getQuantity());
        fill.setPrice(getPrice(order));
        fill.setOrder(order);
        fill.setExecutionCommission(getExecutionCommission(order));
        fill.setClearingCommission(getClearingCommission(order));
        fill.setFee(getFee(order));

        // propagate the fill
        this.serverEngine.sendEvent(fill);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getPrice(final SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        if (order instanceof LimitOrderI) {

            // limit orders are executed at their limit price
            return ((LimitOrderI) order).getLimit();

        } else {

            Security security = order.getSecurity();

            // all other orders are executed the the market
            MarketDataEventVO marketDataEvent = this.localLookupService.getCurrentMarketDataEvent(security.getId());
            return marketDataEvent.getMarketValue(Side.BUY.equals(order.getSide()) ? Direction.SHORT : Direction.LONG)
                    .setScale(security.getSecurityFamily().getScale(), BigDecimal.ROUND_HALF_UP);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getExecutionCommission(final SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        return null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getClearingCommission(final SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        return null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getFee(final SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        return null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextOrderId(final Account account) {

        long n = counter.incrementAndGet();
        return "sim" + n + ".0";
    }

    @Override
    public void validateOrder(SimpleOrder order) {

        // do nothing
    }

    @Override
    public void cancelOrder(SimpleOrder order) {

        throw new UnsupportedOperationException("cancel order not supported in simulation");
    }

    @Override
    public void modifyOrder(SimpleOrder order) {

        throw new UnsupportedOperationException("modify order not supported in simulation");
    }

    @Override
    public OrderServiceType getOrderServiceType() {

        return OrderServiceType.SIMULATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TIF getDefaultTIF() {
        return TIF.DAY;
    }

}
