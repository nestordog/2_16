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

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ch.algotrader.cache.CacheManager;

@RestController
@RequestMapping(path = "/rest")
public class CacheRestController {

    private final CacheManager levelZeroCacheManager;
    private final net.sf.ehcache.CacheManager ehCacheManager;

    public CacheRestController(final CacheManager levelZeroCacheManager) {
        this.levelZeroCacheManager = levelZeroCacheManager;
        this.ehCacheManager = net.sf.ehcache.CacheManager.getInstance();
    }

    @CrossOrigin
    @RequestMapping(path = "/cache/clear-all", method = RequestMethod.POST)
    public void clearAll() {

        this.ehCacheManager.clearAll();
        this.levelZeroCacheManager.clear();
    }

}
