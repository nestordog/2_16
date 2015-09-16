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

import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.util.MyLogger;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:vgolding@algotrader.ch">Vince Golding</a>
 * @version $Revision$ $Date$
 */
public class OpenOrderCache {

  private final Logger logger = MyLogger.getLogger(OpenOrderCache.class.getName());

  private final Map<String, Order> openOrderCache = new ConcurrentHashMap<>();

  public OpenOrderCache(){}

  public void add(String orderId, Order order){
    if( openOrderCache.put(orderId, order) != null){
      logger.info("updating order cache for id: " + orderId + " order " + order);
    } else {
      logger.info("caching order id " + orderId + " for order " + order);
    }
  }

  public Order getOrder(String orderId){
    return openOrderCache.get(orderId);
  }

  /**
   * Eviction callback from Esper.
   * @param orderStatus
   */
  public void evict(OrderStatus orderStatus){
    Order order = orderStatus.getOrder();
    if(order != null && order.getIntId() != null){
      openOrderCache.remove(order.getIntId());
      logger.info("order for id " + order.getIntId() + " evicted from open order cache order: " + order + " status: " + orderStatus);
    } else {
      logger.warn("attempt to evict an un-cached order for " + orderStatus);
    }
  }
}
