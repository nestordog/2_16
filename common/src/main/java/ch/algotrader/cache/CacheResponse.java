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

/**
 * Represents the state of an object inside the level-zero-cache
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class CacheResponse {

    private final CacheState cacheState;
    private final Object value;

    public static CacheResponse existingObject(Object existingObject) {
        return new CacheResponse(CacheState.EXISTING, existingObject);
    }

    public static CacheResponse updatedObject(Object updatedObject) {
        return new CacheResponse(CacheState.UPDATED, updatedObject);
    }

    public static CacheResponse newObject() {
        return new CacheResponse(CacheState.NEW, null);
    }

    public static CacheResponse skippedObject() {
        return new CacheResponse(CacheState.SKIPPED, null);
    }

    public static CacheResponse processedObject() {
        return new CacheResponse(CacheState.PROCESSED, null);
    }

    public static CacheResponse removedObject() {
        return new CacheResponse(CacheState.REMOVED, null);
    }

    private CacheResponse(CacheState cacheState, Object value) {
        this.cacheState = cacheState;
        this.value = value;
    }

    protected CacheState getState() {
        return this.cacheState;
    }

    protected Object getValue() {
        return this.value;
    }

    public static enum CacheState {
        NEW, // object newly added to the cache
        EXISTING, // object already existed in the cache
        SKIPPED, // object was skipped due to being a proxy or persistent collection (uninitialized or without role)
        PROCESSED, // object was already processed by the current stack
        REMOVED, // object does not exist anymore and was removed from the cache
        UPDATED; // updated or newly initialized object
    }

    @Override
    public String toString() {
        return this.cacheState + (this.value == null ? "" : ":" + this.value);
    }

}
