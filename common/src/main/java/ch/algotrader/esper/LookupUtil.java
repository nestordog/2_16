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
package ch.algotrader.esper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.map.SingletonMap;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionFactoryImpl;

import ch.algotrader.ServiceLocator;
import ch.algotrader.cache.CacheManager;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.service.LookupService;

/**
 * Provides static Lookup methods based mainly on the {@link ch.algotrader.service.LookupService}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class LookupUtil {

    private static boolean hasCacheManager() {
        return ServiceLocator.instance().containsService("cacheManager");
    }

    private static CacheManager getCacheManager() {
        return ServiceLocator.instance().getService("cacheManager", CacheManager.class);
    }

    private static LookupService getLookupService() {
        return ServiceLocator.instance().getLookupService();
    }

    private static String getQueryString(String queryName) {
        SessionFactoryImpl sessionFactory = (SessionFactoryImpl) ServiceLocator.instance().getService("sessionFactory", SessionFactory.class);
        return sessionFactory.getNamedQuery(queryName).getQueryString();
    }

    /**
     * Gets a Security by its {@code id} and initializes {@link Subscription Subscriptions}, {@link
     * Position Positions}, Underlying {@link Security} and {@link ch.algotrader.entity.security.SecurityFamily} to make sure that
     * they are available when the Hibernate Session is closed and this Security is in a detached
     * state.
     */
    public static Security getSecurityInitialized(int securityId) {

        if (hasCacheManager()) {
            return getCacheManager().get(SecurityImpl.class, securityId);
        } else {
            return getLookupService().getSecurityInitialized(securityId);
        }
    }

    /**
     * Gets a {@link ch.algotrader.entity.security.SecurityFamily} id by the {@code securityId} of one of its Securities
     */
    public static int getSecurityFamilyIdBySecurity(int securityId) {

        Security security = getSecurityInitialized(securityId);
        return security != null ? security.getSecurityFamily().getId() : 0;
    }

    /**
     * Gets a Security by its {@code isin}.
     */
    public static Security getSecurityByIsin(String isin) {

        String queryString = getQueryString("Security.findByIsin");

        Map<String, Object> namedParameters = new SingletonMap<String, Object>("isin", isin);

        if (hasCacheManager()) {
            return (Security) getCacheManager().queryUnique(queryString, namedParameters);
        } else {
            return (Security) getLookupService().getUnique(queryString, namedParameters);
        }
    }

    /**
     * Gets a Subscriptions by the defined {@code strategyName} and {@code securityId}.
     */
    public static Subscription getSubscription(String strategyName, int securityId) {

        String queryString = getQueryString("Subscription.findByStrategyAndSecurity");

        Map<String, Object> namedParameters = new HashMap<String, Object>();
        namedParameters.put("strategyName", strategyName);
        namedParameters.put("securityId", securityId);

        if (hasCacheManager()) {
            return (Subscription) getCacheManager().queryUnique(queryString, namedParameters);
        } else {
            return (Subscription) getLookupService().get(queryString, namedParameters);
        }
    }

    /**
     * Gets all Options that are subscribed by at least one Strategy.
     */
    public static Option[] getSubscribedOptions() {

        String queryString = getQueryString("Option.findSubscribedOptions");

        if (hasCacheManager()) {
            return getCacheManager().query(queryString).toArray(new Option[] {});
        } else {
            return getLookupService().get(queryString).toArray(new Option[] {});
        }
    }

    /**
     * Gets all Futures that are subscribed by at least one Strategy.
     */
    public static Future[] getSubscribedFutures() {

        String queryString = getQueryString("Future.findSubscribedFutures");

        if (hasCacheManager()) {
            return getCacheManager().query(queryString).toArray(new Future[] {});
        } else {
            return getLookupService().get(queryString).toArray(new Future[] {});
        }
    }

    /**
     * Gets a Position by Security and Strategy.
     */
    public static Position getPositionBySecurityAndStrategy(int securityId, String strategyName) {

        String queryString = getQueryString("Position.findBySecurityAndStrategy");

        Map<String, Object> namedParameters = new HashMap<String, Object>();
        namedParameters.put("strategyName", strategyName);
        namedParameters.put("securityId", securityId);

        if (hasCacheManager()) {
            return (Position) getCacheManager().queryUnique(queryString, namedParameters);
        } else {
            return (Position) getLookupService().getUnique(queryString, namedParameters);
        }
    }

    /**
     * Gets all open Position (with a quantity != 0).
     */
    public static Position[] getOpenPositions() {

        String queryString = getQueryString("Position.findOpenPositions");

        if (hasCacheManager()) {
            return getCacheManager().query(queryString).toArray(new Position[] {});
        } else {
            return getLookupService().get(queryString).toArray(new Position[] {});
        }
    }

    /**
     * Gets open Positions for the specified Strategy.
     */
    public static Position[] getOpenPositionsByStrategy(String strategyName) {

        String queryString = getQueryString("Position.findOpenPositionsByStrategy");

        Map<String, Object> namedParameters = new SingletonMap<String, Object>("strategyName", strategyName);

        if (hasCacheManager()) {
            return getCacheManager().query(queryString, namedParameters).toArray(new Position[] {});
        } else {
            return getLookupService().get(queryString, namedParameters).toArray(new Position[] {});
        }
    }

    /**
     * Gets open Positions for the specified Security
     */
    public static Position[] getOpenPositionsBySecurity(int securityId) {

        String queryString = getQueryString("Position.findOpenPositionsBySecurity");

        Map<String, Object> namedParameters = new SingletonMap<String, Object>("securityId", securityId);

        if (hasCacheManager()) {
            return getCacheManager().query(queryString, namedParameters).toArray(new Position[] {});
        } else {
            return getLookupService().get(queryString, namedParameters).toArray(new Position[] {});
        }
    }

    /**
     * Gets open Positions for the specified Strategy and SecurityFamily.
     */
    public static Position[] getOpenPositionsByStrategyAndSecurityFamily(String strategyName, int securityFamilyId) {

        String queryString = getQueryString("Position.findOpenPositionsByStrategyAndSecurityFamily");

        Map<String, Object> namedParameters = new HashMap<String, Object>();
        namedParameters.put("strategyName", strategyName);
        namedParameters.put("securityFamilyId", securityFamilyId);

        if (hasCacheManager()) {
            return getCacheManager().query(queryString, namedParameters).toArray(new Position[] {});
        } else {
            return getLookupService().get(queryString, namedParameters).toArray(new Position[] {});
        }
    }

    /**
     * Gets the first Tick of the defined Security that is before the maxDate (but not earlier than
     * one minute before that the maxDate).
     */
    public static Tick getTickByDateAndSecurity(int securityId, Date date) {

        return getLookupService().getTickBySecurityAndMaxDate(securityId, date);
    }

}
