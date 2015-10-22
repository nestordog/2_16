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
package ch.algotrader.dao;

import java.util.Date;
import java.util.List;

import ch.algotrader.entity.Position;

/**
 * DAO for {@link ch.algotrader.entity.Position} objects.
 *
 * @see ch.algotrader.entity.Position
 */
public interface PositionDao extends ReadWriteDao<Position> {

    /**
     * Finds a Position by its {@code id}. In addition its Security and {@link
     * ch.algotrader.entity.security.SecurityFamily SecurityFamily} will be initialized.
     * @param id
     * @return Position
     */
    Position findByIdInclSecurityAndSecurityFamily(long id);

    /**
     * Finds all Positions of a Strategy
     * @param strategyName
     * @return List<Position>
     */
    List<Position> findByStrategy(String strategyName);

    /**
     * <p>
     * Does the same thing as {@link #findByStrategy(String)} with an
     * additional conversion of entities to value objects.
     * @param strategyName
     * @param converter
     * @return List<V>
     * </p>
     */
    <V> List<V> findByStrategy(String strategyName, EntityConverter<Position, V> converter);

    /**
     * Finds a Position by Security and Strategy.
     * @param securityId
     * @param strategyName
     * @return Position
     */
    Position findBySecurityAndStrategy(long securityId, String strategyName);

    /**
     * Finds a Position by Security and Strategy and places a database lock on this Position.
     * @param securityId
     * @param strategyId
     * @return Position
     */
    Position findBySecurityAndStrategyIdLocked(long securityId, long strategyId);

    /**
     * Finds all open Position (with a quantity != 0).
     * @return List<Position>
     */
    List<Position> findOpenPositions();

    /**
     * <p>
     * Does the same thing as {@link #findOpenPositions()} with an
     * additional conversion of entities to value objects.
     * @param converter
     * @return List<V>
     * </p>
     */
    <V> List<V> findOpenPositions(EntityConverter<Position, V> converter);

    /**
     * Returns aggregated Position quantities before the specified {@code maxDate}.
     * @param maxDate
     * @return List<Position>
     */
    List<Position> findOpenPositionsByMaxDateAggregated(Date maxDate);

    /**
     * Finds open Positions for the specified Strategy.
     * @param strategyName
     * @return List<Position>
     */
    List<Position> findOpenPositionsByStrategy(String strategyName);

    /**
       * <p>
       * Does the same thing as {@link #findOpenPositionsByStrategy(String)} with an
       * additional conversion of entities to value objects.
       * @param converter
       * @return List<V>
       * </p>
       */
    <V> List<V> findOpenPositionsByStrategy(String strategyName, EntityConverter<Position, V> converter);

    /**
     * Returns Position quantities for the specified Strategy before the specified {@code maxDate}.
     * @param strategyName
     * @param maxDate
     * @return List<Position>
     */
    List<Position> findOpenPositionsByStrategyAndMaxDate(String strategyName, Date maxDate);

    /**
     * Finds open Positions for the specified Strategy and SecurityType.
     * @param strategyName
     * @param type The Security Type which has to be defined as an {@code int} using {@link
     * ch.algotrader.util.HibernateUtil#getDisriminatorValue HibernateUtil}
     * @return List<Position>
     */
    List<Position> findOpenPositionsByStrategyAndType(String strategyName, int type);

    /**
     * Finds open Positions for the specified Strategy and SecurityType.
     * @param strategyName
     * @param type The Security Type which has to be defined as an {@code int} using {@link
     * ch.algotrader.util.HibernateUtil#getDisriminatorValue HibernateUtil}
     * @param underlyingType The Security Type which has to be defined as an {@code int} using {@link
     * ch.algotrader.util.HibernateUtil#getDisriminatorValue HibernateUtil}
     * @return List<Position>
     */
    List<Position> findOpenPositionsByStrategyTypeAndUnderlyingType(String strategyName, int type, int underlyingType);

    /**
     * Finds open Positions for the specified Strategy and SecurityFamily.
     * @param strategyName
     * @param securityFamilyId
     * @return List<Position>
     */
    List<Position> findOpenPositionsByStrategyAndSecurityFamily(String strategyName, long securityFamilyId);

    /**
     * Finds open Positions for the specified underlying.
     * @param underlyingId
     * @return List<Position>
     */
    List<Position> findOpenPositionsByUnderlying(long underlyingId);

    /**
     * Finds open Positions for the specified Security
     * @param securityId
     * @return List<Position>
     */
    List<Position> findOpenPositionsBySecurity(long securityId);

    /**
     * Finds open Positions for tradeable Securities
     * @return List<Position>
     */
    List<Position> findOpenTradeablePositions();

    /**
     * Returns aggregated Position quantities for tradeable Securities
     * @return List<Position>
     */
    List<Position> findOpenTradeablePositionsAggregated();

    /**
     * Finds open Positions of the specified Strategy for tradeable Securities
     * @param strategyName
     * @return List<Position>
     */
    List<Position> findOpenTradeablePositionsByStrategy(String strategyName);

    /**
     * Finds open Forex Positions
     * @return List<Position>
     */
    List<Position> findOpenFXPositions();

    /**
     * Returns aggregated Forex Position quantities
     * @return List<Position>
     */
    List<Position> findOpenFXPositionsAggregated();

    /**
     * Finds open Forex Positions of the specified Strategy
     * @param strategyName
     * @return List<Position>
     */
    List<Position> findOpenFXPositionsByStrategy(String strategyName);

    /**
     * Finds open Positions for {@link ch.algotrader.entity.security.ExpirableI ExpirableI}
     * Securities that have expired.
     * @param currentTime
     * @return List<Position>
     */
    List<Position> findExpirablePositions(Date currentTime);

    /**
     * Finds persistent Positions.
     * @return List<Position>
     */
    List<Position> findPersistent();

    /**
     * Finds non-persistent Positions.
     * @return List<Position>
     */
    List<Position> findNonPersistent();

    /**
     * <p>
     * Does the same thing as {@link #loadAll()} with an
     * additional conversion of entities to value objects.
     *
     * @return the loaded entities.
     */
    <V> List<V> loadAll(EntityConverter<Position, V> converter);

    /**
     * Returns aggregated Position quantities
     * @return List<Position>
     */
    List<Position> findOpenPositionsAggregated();

}
