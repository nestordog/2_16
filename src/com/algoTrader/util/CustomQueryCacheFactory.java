package com.algoTrader.util;

import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.cache.QueryCache;
import org.hibernate.cache.StandardQueryCacheFactory;
import org.hibernate.cache.UpdateTimestampsCache;
import org.hibernate.cfg.Settings;

public class CustomQueryCacheFactory extends StandardQueryCacheFactory {

    public QueryCache getQueryCache(final String regionName, final UpdateTimestampsCache updateTimestampsCache,
            final Settings settings, final Properties props) throws HibernateException {
        return new CustomQueryCache(settings, props, updateTimestampsCache, regionName);
    }
}
