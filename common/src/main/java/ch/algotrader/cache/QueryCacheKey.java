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

import java.util.Objects;

import ch.algotrader.dao.NamedParam;

/**
 * A CacheKey for Queries composed of a {@code queryString} and {@code namedParameters}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class QueryCacheKey {

    private final String queryString;
    private final NamedParam[] namedParameters;

    private final int hashCode;

    public QueryCacheKey(String queryString, NamedParam... namedParameters) {

        this.queryString = queryString;
        this.namedParameters = namedParameters;

        this.hashCode = generateHashCode();
    }

    public int generateHashCode() {

        int hashCode = 17;
        hashCode = 37 * hashCode + (this.namedParameters == null ? 0 : this.namedParameters.hashCode());
        hashCode = 37 * hashCode + this.queryString.hashCode();

        return hashCode;
    }

    public String getQueryString() {
        return this.queryString;
    }

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder(this.queryString);

        if (this.namedParameters != null) {
            buffer.append(",namedParameters=").append(this.namedParameters);
        }

        return buffer.toString();
    }

    @Override
    public boolean equals(Object other) {

        if (!(other instanceof QueryCacheKey)) {
            return false;
        }

        QueryCacheKey that = (QueryCacheKey) other;
        return this.queryString.equals(that.queryString) && Objects.equals(this.namedParameters, that.namedParameters);
    }

    @Override
    public int hashCode() {

        return this.hashCode;
    }
}
