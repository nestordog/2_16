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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.algo.AlgoOrder;
import ch.algotrader.entity.trade.algo.AlgoOrderStateVO;
import ch.algotrader.enumeration.Status;
import ch.algotrader.service.ServiceException;

/**
 * Default algo order execution service.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public abstract class AbstractAlgoOrderExecService<T extends AlgoOrder, S extends AlgoOrderStateVO> implements AlgoOrderExecService<T> {

    private final ConcurrentMap<String, S> algoOrderStates;

    public AbstractAlgoOrderExecService() {
        this.algoOrderStates = new ConcurrentHashMap<>();
    }

    protected abstract S createAlgoOrderState(final T algoOrder);

    protected S getAlgoOrderState(final AlgoOrder algoOrder) {
        String intId = algoOrder.getIntId();
        if (intId == null) {
            throw new ServiceException("Order intId is null");
        }
        return getAlgoOrderState(intId);
    }

    protected S getAlgoOrderState(final String intId) {
        S algoOrderState = this.algoOrderStates.get(intId);
        if (algoOrderState == null) {
            throw new ServiceException("Unexpected order intId: " + intId);
        }
        return algoOrderState;
    }

    protected void removeAlgoOrderState(final AlgoOrder algoOrder) {
        this.algoOrderStates.remove(algoOrder.getIntId());
    }

    @Override
    public final void validateOrder(final T algoOrder) throws OrderValidationException {

        Validate.notNull(algoOrder, "AlgoOrder is null");

        String intId = algoOrder.getIntId();
        if (intId == null) {
            throw new ServiceException("Order intId is null");
        }
        S algoOrderState = this.algoOrderStates.computeIfAbsent(intId, key -> createAlgoOrderState(algoOrder));
        handleValidateOrder(algoOrder, algoOrderState);
    }

    public abstract void handleValidateOrder(final T algoOrder, final S algoOrderState);

    @Override
    public final void sendOrder(final T algoOrder) {

        Validate.notNull(algoOrder, "AlgoOrder is null");

        String intId = algoOrder.getIntId();
        if (intId == null) {
            throw new ServiceException("Order intId is null");
        }
        handleSendOrder(algoOrder, getAlgoOrderState(algoOrder));
    }

    public abstract void handleSendOrder(final T algoOrder, final S algoOrderState);

    @Override
    public final void modifyOrder(final T algoOrder) {

        Validate.notNull(algoOrder, "AlgoOrder is null");

        handleModifyOrder(algoOrder, getAlgoOrderState(algoOrder));
    }

    public void handleModifyOrder(final T order, final S algoOrderState) {
    }

    @Override
    public final void cancelOrder(final T algoOrder) {

        Validate.notNull(algoOrder, "AlgoOrder is null");

        handleCancelOrder(algoOrder, getAlgoOrderState(algoOrder));
    }

    public void handleCancelOrder(final T order, final S algoOrderState) {
    }

    @Override
    public String getNextOrderId(final Account account) {
        throw new UnsupportedOperationException("getNextOrderId not supported");
    }

    @Override
    public final void handleOrderStatus(final OrderStatus orderStatus) {

        Validate.notNull(orderStatus, "OrderStatus is null");

        String intId = orderStatus.getIntId();
        if (intId == null) {
            throw new ServiceException("Order status intId is null");
        }
        handleOrderStatus(orderStatus, algoOrderStates.get(intId));
    }

    /**
     * Will remove the AlgoOrderState upon full execution. If this method is overwritten {@link #removeAlgoOrderState} needs to be called manually
     */
    public void handleOrderStatus(final OrderStatus orderStatus, final S algoOrderState) {

        Order order = orderStatus.getOrder();
        if (order instanceof AlgoOrder && orderStatus.getStatus() == Status.EXECUTED) {
            removeAlgoOrderState((AlgoOrder) order);
        }
    }

}
