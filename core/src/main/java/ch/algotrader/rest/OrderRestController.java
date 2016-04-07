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

package ch.algotrader.rest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ch.algotrader.entity.trade.LimitOrderVO;
import ch.algotrader.entity.trade.MarketOrderVO;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderVO;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.StopLimitOrderVO;
import ch.algotrader.entity.trade.StopOrderVO;
import ch.algotrader.service.OrderService;

@RestController
@RequestMapping(path = "/rest")
public class OrderRestController extends RestControllerBase {

    private final OrderService orderService;

    public OrderRestController(final OrderService orderService) {
        this.orderService = orderService;
    }

    @CrossOrigin
    @RequestMapping(path = "/execution/order", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrderVO> getOpenOrders() {

        return this.orderService.getOpenOrderDetails().stream()
                .map(entry -> entry.getOrder().convertToVO())
                .collect(Collectors.toList());
    }

    @CrossOrigin
    @RequestMapping(path = "/execution/order/{intId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderVO getOpenOrders(@PathVariable final String intId) {

        String key = intId.replace('_', '.');
        return Optional.ofNullable(this.orderService.getOpenOrderByIntId(key))
                .map(Order::convertToVO).orElseThrow(() -> new EntityNotFoundException("Order not found: " + key));
    }

    @CrossOrigin
    @RequestMapping(path = "/execution/order/market", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void submitOrder(@RequestBody final MarketOrderVO order) {

        this.orderService.sendOrder(order);
    }

    @CrossOrigin
    @RequestMapping(path = "/execution/modify/order/market", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void modifyOrder(@RequestBody final MarketOrderVO order) {

        this.orderService.modifyOrder(order);
    }

    @CrossOrigin
    @RequestMapping(path = "/execution/order/limit", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void submitOrder(@RequestBody final LimitOrderVO order) {

        this.orderService.sendOrder(order);
    }

    @CrossOrigin
    @RequestMapping(path = "/execution/modify/order/limit", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void modifyOrder(@RequestBody final LimitOrderVO order) {

        this.orderService.modifyOrder(order);
    }

    @CrossOrigin
    @RequestMapping(path = "/execution/order/stop", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void submitOrder(@RequestBody final StopOrderVO order) {

        this.orderService.sendOrder(order);
    }

    @CrossOrigin
    @RequestMapping(path = "/execution/modify/order/stop", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void modifyOrder(@RequestBody final StopOrderVO order) {

        this.orderService.modifyOrder(order);
    }

    @CrossOrigin
    @RequestMapping(path = "/execution/order/stoplimit", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void submitOrder(@RequestBody final StopLimitOrderVO order) {

        this.orderService.sendOrder(order);
    }

    @CrossOrigin
    @RequestMapping(path = "/execution/modify/order/stoplimit", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void modifyOrder(@RequestBody final StopLimitOrderVO order) {

        this.orderService.modifyOrder(order);
    }

    @CrossOrigin
    @RequestMapping(path = "/execution/order/{intId}", method = RequestMethod.DELETE)
    public void submitOrder(@PathVariable final String intId) {

        this.orderService.cancelOrder(intId);
    }

    @CrossOrigin
    @RequestMapping(path = "/execution/next-order-id", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String getNextOrderId(@RequestBody final long accountId) {

        return this.orderService.getNextOrderId(SimpleOrder.class, accountId);
    }

}
