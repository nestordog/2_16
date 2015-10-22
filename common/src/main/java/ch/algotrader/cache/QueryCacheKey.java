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

import java.util.Arrays;
import java.util.Objects;

import ch.algotrader.dao.NamedParam;

/**
 * A CacheKey for Queries composed of a {@code queryString} and {@code namedParameters}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class QueryCacheKey {

    private final String queryString;
    private final int maxResults;
    private final NamedParam[] namedParameters;

    public QueryCacheKey(String queryString, int maxResults, NamedParam... namedParameters) {

        this.queryString = queryString.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll("\\t", " ").replaceAll("\\s+", " ").trim();
        this.maxResults = maxResults;
        this.namedParameters = namedParameters;
    }

    public String getQueryString() {
        return this.queryString;
    }

    public int getMaxResults() {
        return this.maxResults;
    }

    public NamedParam[] getNamedParameters() {
        return this.namedParameters;
    }

    @Override
    public String toString() {

        String result = this.queryString;
        if (this.namedParameters != null) {
            for (NamedParam namedParam : this.namedParameters) {
                result = result.replaceAll(":" + namedParam.getName(), namedParam.getValue().toString());
            }
        }

        return result + (this.maxResults == 0 ? "" : " (" + this.maxResults + ")");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof QueryCacheKey)) {
            return false;
        } else {
            QueryCacheKey that = (QueryCacheKey) obj;
            return Objects.equals(this.getQueryString(), that.getQueryString()) &&
                    this.getMaxResults() == that.getMaxResults() &&
                    Arrays.equals(this.getNamedParameters(), that.getNamedParameters());
        }
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 37 + Objects.hashCode(this.queryString);
        hash = hash * 37 + Objects.hashCode(this.maxResults);
        hash = hash * 37 + Objects.hashCode(Arrays.hashCode(this.namedParameters));
        return hash;
    }
}
