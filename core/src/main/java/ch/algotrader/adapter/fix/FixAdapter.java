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
package ch.algotrader.adapter.fix;

import quickfix.Message;
import quickfix.SessionNotFound;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.OrderServiceType;

/**
 * Main entry point to Fix sessions.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface FixAdapter {

    /**
     * creates an individual session
     */
    void createSession(OrderServiceType orderServiceType) throws Exception;

    /**
     * creates an individual session with the given qualifier
     */
    void createSession(String sessionQualifier) throws Exception;

    /**
     * sends a message to the designated session for the given account
     */
    void sendMessage(Message message, Account account) throws SessionNotFound;

    /**
     * sends a message to the designated session with the given qualifier.
     */
    void sendMessage(Message message, String sessionQualifier) throws SessionNotFound;

    /**
     * Gets the next {@code orderId} for the specified {@code account}
     */
    String getNextOrderId(Account account);

    /**
     * Gets the next {@code orderIdVersion} based on the specified {@code order}
     */
    String getNextOrderIdVersion(Order order);

}
