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
package ch.algotrader.dao.trade;

import ch.algotrader.dao.ReadWriteDao;
import ch.algotrader.entity.trade.MarketOrder;

/**
 * DAO for {@link ch.algotrader.entity.trade.MarketOrder} objects.
 *
 * @see ch.algotrader.entity.trade.MarketOrder
 */
public interface MarketOrderDao extends ReadWriteDao<MarketOrder> {

    // spring-dao merge-point
}
