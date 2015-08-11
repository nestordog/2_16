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
package ch.algotrader.ordermgmt;

import java.util.Collection;
import java.util.List;

import ch.algotrader.entity.trade.SimpleOrder;

/**
* Registry of open orders.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*/
public interface OpenOrderRegistry {

    void add(final SimpleOrder order);

    SimpleOrder remove(String intId);

    SimpleOrder findByIntId(String intId);

    List<SimpleOrder> getAll();

    SimpleOrder findOpenOrderByRootIntId(String rootId);

    Collection<SimpleOrder> findOpenOrdersByParentIntId(String parentIntId);

}
