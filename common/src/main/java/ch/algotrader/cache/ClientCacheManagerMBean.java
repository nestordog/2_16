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
package ch.algotrader.cache;

import java.util.List;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * MBean for Level-Zero-Cache
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@ManagedResource(objectName = "ch.algotrader.cache:name=CacheManager")
public class ClientCacheManagerMBean {

    final CacheManager levelZeroCacheManager;

    public ClientCacheManagerMBean(CacheManager levelZeroCacheManager) {
        this.levelZeroCacheManager = levelZeroCacheManager;
    }

    @ManagedOperation
    @ManagedOperationParameters({})
    public void clearAll() {

        this.levelZeroCacheManager.clear();
    }

    @ManagedAttribute
    public int getEntityCacheSize() {
        return this.levelZeroCacheManager.getEntityCacheSize();
    }

    @ManagedAttribute
    public List<String> getQueries() {
        return this.levelZeroCacheManager.getQueries();
    }
}
