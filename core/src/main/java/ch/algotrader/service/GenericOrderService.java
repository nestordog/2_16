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
package ch.algotrader.service;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderValidationException;

/**
 * Generic order service intended to initiate order operations
 * such as submission of a new order, modification or cancellation of
 * an existing order, order validation.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public interface GenericOrderService<T extends Order> {

    /**
     * Validates the order. It is suggested to call this method by itself prior to sending an Order.
     * However {@link #sendOrder} will invoke this method again.
     */
    void validateOrder(T order) throws OrderValidationException;

    /**
     * Sends the order.
     */
    void sendOrder(T order);

    /**
     * Cancels the order.
     */
    void cancelOrder(T order);

    /**
     * Modifies an existing order by overwriting with the given order.
     */
    void modifyOrder(T order);

    /**
     * Generates next order intId for the given account.
     */
    String getNextOrderId(Account account);

}
