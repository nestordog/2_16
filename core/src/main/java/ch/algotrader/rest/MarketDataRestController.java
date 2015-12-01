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
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ch.algotrader.service.MarketDataService;
import ch.algotrader.vo.marketData.MarketDataSubscriptionVO;

@RestController
@RequestMapping(path = "/rest")
public class MarketDataRestController {

    private final MarketDataService marketDataService;

    public MarketDataRestController(final MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @CrossOrigin
    @RequestMapping(path = "/subscription/marketdata/supported-feed", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getSupportedFeeds() {

        return this.marketDataService.getSupportedFeeds().stream()
                .sorted().collect(Collectors.toList());
    }

    @CrossOrigin
    @RequestMapping(path = "/subscription/marketdata/subscribe", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void subscribe(@RequestBody final MarketDataSubscriptionVO subscriptionRequest) {

        String strategyName = subscriptionRequest.getStrategyName();
        long securityId = subscriptionRequest.getSecurityId();
        String feedType = subscriptionRequest.getFeedType();
        if (feedType != null) {
            this.marketDataService.subscribe(strategyName, securityId, feedType);
        } else {
            this.marketDataService.subscribe(strategyName, securityId);
        }
    }

    @CrossOrigin
    @RequestMapping(path = "/subscription/marketdata/unsubscribe", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void unsubscribe(@RequestBody final MarketDataSubscriptionVO subscriptionRequest) {

        String strategyName = subscriptionRequest.getStrategyName();
        long securityId = subscriptionRequest.getSecurityId();
        String feedType = subscriptionRequest.getFeedType();
        if (feedType != null) {
            this.marketDataService.unsubscribe(strategyName, securityId, feedType);
        } else {
            this.marketDataService.unsubscribe(strategyName, securityId);
        }
    }

}
