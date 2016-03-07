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

import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.algo.AlgoOrder;
import ch.algotrader.entity.trade.algo.VariableIncrementalOrder;
import ch.algotrader.entity.trade.algo.VariableIncrementalOrderStateVO;
import ch.algotrader.enumeration.Side;
import ch.algotrader.service.MarketDataCacheService;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.SimpleOrderService;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class VariableIncrementalOrderService extends AbstractAlgoOrderExecService<VariableIncrementalOrder, VariableIncrementalOrderStateVO> {

    private final MarketDataCacheService marketDataCacheService;
    private final SimpleOrderService simpleOrderService;

    public VariableIncrementalOrderService(
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
        return VariableIncrementalOrder.class;
    }

    @Override
    protected VariableIncrementalOrderStateVO handleValidateOrder(final VariableIncrementalOrder algoOrder) {
        Security security = algoOrder.getSecurity();
        SecurityFamily family = security.getSecurityFamily();

        TickVO tick = (TickVO) this.marketDataCacheService.getCurrentMarketDataEvent(security.getId());

        double bidDouble = tick.getBid().doubleValue();
        double askDouble = tick.getAsk().doubleValue();
        double spread = askDouble - bidDouble;
        double increment = algoOrder.getIncrement() * spread;

        double limit;
        double maxLimit;
        if (Side.BUY.equals(algoOrder.getSide())) {
            double limitRaw = bidDouble + algoOrder.getStartOffsetPct() * spread;
            double maxLimitRaw = bidDouble + algoOrder.getEndOffsetPct() * spread;
            limit = RoundUtil.roundToNextN(limitRaw, family.getTickSize(null, limitRaw, true), BigDecimal.ROUND_FLOOR);
            maxLimit = RoundUtil.roundToNextN(maxLimitRaw, family.getTickSize(null, maxLimitRaw, true), BigDecimal.ROUND_CEILING);
        } else {
            double limitRaw = askDouble - algoOrder.getStartOffsetPct() * spread;
            double maxLimitRaw = askDouble - algoOrder.getEndOffsetPct() * spread;
            limit = RoundUtil.roundToNextN(limitRaw, family.getTickSize(null, limitRaw, true), BigDecimal.ROUND_CEILING);
            maxLimit = RoundUtil.roundToNextN(maxLimitRaw, family.getTickSize(null, maxLimitRaw, true), BigDecimal.ROUND_FLOOR);
        }

        // limit and maxLimit are correctly rounded according to tickSizePattern
        BigDecimal startLimit = RoundUtil.getBigDecimal(limit, family.getScale(null));
        BigDecimal endLimit = RoundUtil.getBigDecimal(maxLimit, family.getScale(null));

        return new VariableIncrementalOrderStateVO(startLimit, endLimit, startLimit, increment);
    }

    @Override
    public void handleSendOrder(final VariableIncrementalOrder algoOrder, final VariableIncrementalOrderStateVO algoOrderState) {

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

        this.simpleOrderService.sendOrder(limitOrder);}

    @Override
    protected void handleModifyOrder(final VariableIncrementalOrder order, final VariableIncrementalOrderStateVO algoOrderState) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void handleCancelOrder(final VariableIncrementalOrder order, final VariableIncrementalOrderStateVO algoOrderState) {

    }

    public void adjustLimit(final VariableIncrementalOrder algoOrder) throws ReflectiveOperationException {

        Optional<VariableIncrementalOrderStateVO> optional = getAlgoOrderState(algoOrder);
        if (optional.isPresent()) {

            VariableIncrementalOrderStateVO orderState = optional.get();

            // check limit
            if (!checkLimit(algoOrder, orderState)) {
                cancelOrder(algoOrder);
                return;
            }

            SecurityFamily family = algoOrder.getSecurity().getSecurityFamily();

            if (algoOrder.getSide().equals(Side.BUY)) {

                double tickSize = family.getTickSize(null, orderState.getCurrentLimit().doubleValue(), true);
                double increment = RoundUtil.roundToNextN(orderState.getIncrement(), tickSize, BigDecimal.ROUND_CEILING);
                BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale(null));
                orderState.setCurrentLimit(orderState.getCurrentLimit().add(roundedIncrement));
            } else {

                double tickSize = family.getTickSize(null, orderState.getCurrentLimit().doubleValue(), false);
                double increment = RoundUtil.roundToNextN(orderState.getIncrement(), tickSize, BigDecimal.ROUND_CEILING);
                BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale(null));
                orderState.setCurrentLimit(orderState.getCurrentLimit().subtract(roundedIncrement));
            }

            LimitOrder modifiedOrder = (LimitOrder) BeanUtils.cloneBean(orderState.getLimitOrder());
            modifiedOrder.setId(0L);

            modifiedOrder.setLimit(orderState.getCurrentLimit());
            this.simpleOrderService.modifyOrder(modifiedOrder);
        }
    }

    private boolean checkLimit(AlgoOrder algoOrder, VariableIncrementalOrderStateVO orderState) {

        SecurityFamily family = algoOrder.getSecurity().getSecurityFamily();

        if (algoOrder.getSide().equals(Side.BUY)) {

            double tickSize = family.getTickSize(null, orderState.getCurrentLimit().doubleValue(), true);
            double increment = RoundUtil.roundToNextN(orderState.getIncrement(), tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale(null));

            return orderState.getCurrentLimit().add(roundedIncrement).compareTo(orderState.getEndLimit()) <= 0;
        } else {
            double tickSize = family.getTickSize(null, orderState.getCurrentLimit().doubleValue(), false);
            double increment = RoundUtil.roundToNextN(orderState.getIncrement(), tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale(null));

            return orderState.getCurrentLimit().subtract(roundedIncrement).compareTo(orderState.getEndLimit()) >= 0;
        }
    }

}
