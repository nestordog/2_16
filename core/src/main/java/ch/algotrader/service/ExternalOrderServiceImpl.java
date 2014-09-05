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
package ch.algotrader.service;

import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.util.spring.HibernateSession;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@HibernateSession
public abstract class ExternalOrderServiceImpl implements ExternalOrderService {

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void sendOrder(SimpleOrder order);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void validateOrder(SimpleOrder order);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void cancelOrder(SimpleOrder order);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void modifyOrder(SimpleOrder order);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract OrderServiceType getOrderServiceType();

}
