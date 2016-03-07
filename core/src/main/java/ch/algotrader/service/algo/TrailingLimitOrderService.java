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
package ch.algotrader.service.algo;

import java.math.BigDecimal;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.algo.AlgoOrder;
import ch.algotrader.entity.trade.algo.TrailingLimitOrder;
import ch.algotrader.entity.trade.algo.TrailingLimitOrderStateVO;
import ch.algotrader.enumeration.Side;
import ch.algotrader.service.MarketDataCacheService;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.SimpleOrderService;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class TrailingLimitOrderService extends AbstractAlgoOrderExecService<TrailingLimitOrder, TrailingLimitOrderStateVO> {

    private static final Logger LOGGER = LogManager.getLogger(TrailingLimitOrderService.class);

    private final SimpleOrderService simpleOrderService;
    private final MarketDataCacheService marketDataCacheService;

    public TrailingLimitOrderService(final OrderExecutionService orderExecutionService, final SimpleOrderService simpleOrderService, final MarketDataCacheService marketDataCacheService) {

        super(orderExecutionService, simpleOrderService);

        Validate.notNull(simpleOrderService, "SimpleOrderService is null");
        Validate.notNull(marketDataCacheService, "MarketDataCacheService is null");

        this.simpleOrderService = simpleOrderService;
        this.marketDataCacheService = marketDataCacheService;
    }

    @Override
    public Class<? extends AlgoOrder> getAlgoOrderType() {
        return TrailingLimitOrder.class;
    }

    @Override
    protected TrailingLimitOrderStateVO createAlgoOrderState(final TrailingLimitOrder algoOrder) throws OrderValidationException {
        return new TrailingLimitOrderStateVO();
    }

    @Override
    protected void handleSendOrder(TrailingLimitOrder algoOrder, TrailingLimitOrderStateVO algoOrderState) {

        Security security = algoOrder.getSecurity();

        TickVO tick = (TickVO) this.marketDataCacheService.getCurrentMarketDataEvent(security.getId());
        if (tick == null) {
            throw new IllegalStateException("no market data subscription for " + security);
        }

        BigDecimal limit = calculateLimit(algoOrder, tick.getLast());

        LimitOrder order = LimitOrder.Factory.newInstance();
        order.setSecurity(security);
        order.setStrategy(algoOrder.getStrategy());
        order.setSide(algoOrder.getSide());
        order.setQuantity(algoOrder.getQuantity());
        order.setAccount(algoOrder.getAccount());
        order.setLimit(limit);

        order.setParentOrder(algoOrder);
        algoOrderState.setLimitOrder(order);

        this.simpleOrderService.sendOrder(order);
    }

    @Override
    protected void handleModifyOrder(TrailingLimitOrder algoOrder, TrailingLimitOrderStateVO algoOrderState) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void handleCancelOrder(TrailingLimitOrder algoOrder, TrailingLimitOrderStateVO algoOrderState) {
    }

    public void adjustLimit(TrailingLimitOrder algoOrder, BigDecimal last) throws ReflectiveOperationException {

        Optional<TrailingLimitOrderStateVO> optional = getAlgoOrderState(algoOrder);
        if (optional.isPresent()) {

            TrailingLimitOrderStateVO orderState = optional.get();

            synchronized (orderState) {

                LimitOrder limitOrder = orderState.getLimitOrder();
                BigDecimal increment = algoOrder.getIncrement();

                if (limitOrder != null) {

                    BigDecimal limit = limitOrder.getLimit();
                    BigDecimal newLimit = calculateLimit(algoOrder, last);
                    if (algoOrder.getSide() == Side.BUY) {
                        if (newLimit.subtract(limit).compareTo(increment) >= 0) {
                            modifyOrder(algoOrder, orderState, newLimit);
                        }
                    } else {
                        if (limit.subtract(newLimit).compareTo(increment) >= 0) {
                            modifyOrder(algoOrder, orderState, newLimit);
                        }
                    }
                }
            }
        }
    }

    private BigDecimal calculateLimit(TrailingLimitOrder algoOrder, BigDecimal last) {

        if (algoOrder.getSide() == Side.BUY) {
            return last.subtract(algoOrder.getTrailingAmount());
        } else {
            return last.add(algoOrder.getTrailingAmount());
        }
    }

    private void modifyOrder(TrailingLimitOrder algoOrder, TrailingLimitOrderStateVO orderState, BigDecimal newLimit) throws ReflectiveOperationException {

        LimitOrder modifiedOrder = (LimitOrder) BeanUtils.cloneBean(orderState.getLimitOrder());
        modifiedOrder.setId(0L);
        modifiedOrder.setLimit(newLimit);

        orderState.setLimitOrder(modifiedOrder);

        this.simpleOrderService.modifyOrder(modifiedOrder);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("adjusted limit of {} to {}", algoOrder.getDescription(), newLimit);
        }
    }

}
