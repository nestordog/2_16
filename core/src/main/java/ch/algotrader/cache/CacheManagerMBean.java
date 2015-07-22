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
package ch.algotrader.cache;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * MBean for Level-Zero-Cache and EhCache
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@ManagedResource(objectName = "ch.algotrader.cache:name=CacheManager")
public class CacheManagerMBean {

    final CacheManager levelZeroCacheManager;
    final net.sf.ehcache.CacheManager ehCacheManager;

    public CacheManagerMBean(CacheManager levelZeroCacheManager) {
        this.levelZeroCacheManager = levelZeroCacheManager;
        this.ehCacheManager = net.sf.ehcache.CacheManager.getInstance();
    }

    @ManagedOperation
    @ManagedOperationParameters({})
    public void clearAll() {

        this.ehCacheManager.clearAll();
        this.levelZeroCacheManager.clear();
    }

    @ManagedAttribute
    public Map<String, Integer> getLevelZeroCacheSize() {
        return this.levelZeroCacheManager.getCacheSize();
    }

    @ManagedAttribute
    public List<String> getEhCacheNames() {
        return Arrays.asList(this.ehCacheManager.getCacheNames());
    }
}
