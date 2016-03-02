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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.Validate;

import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.algo.AlgoOrder;
import ch.algotrader.entity.trade.algo.IncrementalOrderStateVO;
import ch.algotrader.entity.trade.algo.TickwiseIncrementalOrder;
import ch.algotrader.enumeration.Side;
import ch.algotrader.service.MarketDataCacheService;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.ServiceException;
import ch.algotrader.service.SimpleOrderService;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class TickwiseIncrementalOrderService extends AbstractAlgoOrderExecService<TickwiseIncrementalOrder, IncrementalOrderStateVO> {

    private final MarketDataCacheService marketDataCacheService;
    private final SimpleOrderService simpleOrderService;

    public TickwiseIncrementalOrderService(
            final OrderExecutionService orderExecutionService,
            final MarketDataCacheService marketDataCacheService,
            final SimpleOrderService simpleOrderService) {
        super(orderExecutionService, simpleOrderService);

        Validate.notNull(marketDataCacheService, "MarketDataCacheService is null");

        this.marketDataCacheService = marketDataCacheService;
        this.simpleOrderService = simpleOrderService;
    }

    @Override
    public Class<? extends AlgoOrder> getAlgoOrderType() {
        return TickwiseIncrementalOrder.class;
    }

    @Override
    protected IncrementalOrderStateVO createAlgoOrderState(final TickwiseIncrementalOrder algoOrder) {
        Security security = algoOrder.getSecurity();
        SecurityFamily family = security.getSecurityFamily();

        TickVO tick = (TickVO) this.marketDataCacheService.getCurrentMarketDataEvent(security.getId());

        // check spread and adjust offsetTicks if spread is too narrow
        int spreadTicks = family.getSpreadTicks(null, tick.getBid(), tick.getAsk());
        int adjustedStartOffsetTicks = algoOrder.getStartOffsetTicks();
        int adjustedEndOffsetTicks = algoOrder.getEndOffsetTicks();

        // adjust offsetTicks if needed
        if (spreadTicks < (algoOrder.getStartOffsetTicks() - algoOrder.getEndOffsetTicks())) {

            // first reduce startOffsetTicks to min 0
            adjustedStartOffsetTicks = Math.max(spreadTicks + algoOrder.getEndOffsetTicks(), 0);
            if (spreadTicks < (adjustedStartOffsetTicks - algoOrder.getEndOffsetTicks())) {

                // if necessary also increase endOffstTicks to max 0
                adjustedEndOffsetTicks = Math.min(adjustedStartOffsetTicks - spreadTicks, 0);
            }
        }

        BigDecimal startLimit;
        BigDecimal endLimit;
        if (Side.BUY.equals(algoOrder.getSide())) {
            startLimit = family.adjustPrice(null, tick.getBid(), adjustedStartOffsetTicks);
            endLimit = family.adjustPrice(null, tick.getAsk(), adjustedEndOffsetTicks);

            if (startLimit.doubleValue() <= 0.0) {
                startLimit = family.adjustPrice(null, new BigDecimal(0), 1);
            }

        } else {

            startLimit = family.adjustPrice(null, tick.getAsk(), -adjustedStartOffsetTicks);
            endLimit = family.adjustPrice(null, tick.getBid(), -adjustedEndOffsetTicks);

            if (startLimit.doubleValue() <= 0.0) {
                startLimit = family.adjustPrice(null, new BigDecimal(0), 1);
            }

            if (endLimit.doubleValue() <= 0.0) {
                endLimit = family.adjustPrice(null, new BigDecimal(0), 1);
            }
        }

        return new IncrementalOrderStateVO(startLimit, endLimit, startLimit);
    }

    @Override
    public void handleSendOrder(final TickwiseIncrementalOrder algoOrder, final IncrementalOrderStateVO algoOrderState) {

        Security security = algoOrder.getSecurity();
        LimitOrder limitOrder = LimitOrder.Factory.newInstance();
        limitOrder.setSecurity(security);
        limitOrder.setStrategy(algoOrder.getStrategy());
        limitOrder.setSide(algoOrder.getSide());
        limitOrder.setQuantity(algoOrder.getQuantity());
        limitOrder.setLimit(algoOrderState.getCurrentLimit());
        limitOrder.setAccount(algoOrder.getAccount());

        // associate the childOrder with the parentOrder(this)
        limitOrder.setParentOrder(algoOrder);

        this.simpleOrderService.sendOrder(limitOrder);
    }

    @Override
    protected void handleModifyOrder(final TickwiseIncrementalOrder order, final IncrementalOrderStateVO algoOrderState) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void handleCancelOrder(final TickwiseIncrementalOrder order, final IncrementalOrderStateVO algoOrderState) {

        LimitOrder limitOrder = algoOrderState.getLimitOrder();
        if (limitOrder != null) {
            this.simpleOrderService.cancelOrder(limitOrder);
        }
    }

    public void adjustLimit(final TickwiseIncrementalOrder algoOrder) {

        IncrementalOrderStateVO orderState = getAlgoOrderState(algoOrder);

        // check limit
        if (!checkLimit(algoOrder, orderState)) {
            cancelOrder(algoOrder);
            return;
        }

        SecurityFamily family = algoOrder.getSecurity().getSecurityFamily();
        if (algoOrder.getSide().equals(Side.BUY)) {
            orderState.setCurrentLimit(family.adjustPrice(null, orderState.getCurrentLimit(), 1));
        } else {
            orderState.setCurrentLimit(family.adjustPrice(null, orderState.getCurrentLimit(), -1));
        }

        LimitOrder modifiedOrder;
        try {
            modifiedOrder = (LimitOrder) BeanUtils.cloneBean(orderState.getLimitOrder());
            modifiedOrder.setId(0L);
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }

        modifiedOrder.setLimit(orderState.getCurrentLimit());
        this.simpleOrderService.modifyOrder(modifiedOrder);
    }

    private boolean checkLimit(AlgoOrder algoOrder, IncrementalOrderStateVO orderState) {

        SecurityFamily family = algoOrder.getSecurity().getSecurityFamily();

        if (algoOrder.getSide().equals(Side.BUY)) {
            return family.adjustPrice(null, orderState.getCurrentLimit(), 1).compareTo(orderState.getEndLimit()) <= 0;
        } else {
            return family.adjustPrice(null, orderState.getCurrentLimit(), -1).compareTo(orderState.getEndLimit()) >= 0;
        }
    }

}
