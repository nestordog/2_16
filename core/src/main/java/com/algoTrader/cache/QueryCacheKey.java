/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading. The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation, disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading Badenerstrasse 16 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.cache;


/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class QueryCacheKey {

    private final String queryString;
    private final int hashCode;

    public QueryCacheKey(String queryString) {

        this.queryString = queryString;

        this.hashCode = 17 * 37 + this.queryString.hashCode();
    }

    public String getQueryString() {
        return this.queryString;
    }

    @Override
    public String toString() {

        return this.queryString;
    }

    @Override
    public boolean equals(Object other) {

        if (!(other instanceof QueryCacheKey)) {
            return false;
        }

        QueryCacheKey that = (QueryCacheKey) other;
        return this.queryString.equals(that.queryString);
    }

    @Override
    public int hashCode() {

        return this.hashCode;
    }
}
