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
package ch.algotrader.service;

import java.math.BigDecimal;
import java.util.Date;

import ch.algotrader.entity.security.Option;
import ch.algotrader.enumeration.OptionType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface OptionService {

    /**
     * performs a Delta Hedge of all Securities of the specified {@code underlyingId}
     */
    public void hedgeDelta(long underlyingId);

    /**
     * Creates a synthetic {@link Option} with the specified {@code optionFamilyId}, {@code
     * expirationDate}, {@code targetStrike} and {@code optionType}.
     * The targetStrike will be rounded to a valid strike defined by {@code
     * SecurityFamily.strikeDistance}
     */
    public Option createOTCOption(long optionFamilyId, Date expirationDate, BigDecimal strike, OptionType type);

    /**
     * Creates a synthetic option with the specified  {@code optionFamilyId}, {@code
     * expirationDate}, {@code targetStrike} and {@code optionType}.
     * The {@code targetStrike} will be rounded to a valid strike defined by {@code
     * SecurityFamily.strikeDistance}.
     */
    public Option createDummyOption(long optionFamilyId, Date targetExpirationDate, BigDecimal targetStrike, OptionType type);

    /**
     * Gets the first Options of the give {@code underlyingId} and {@code optionType} that expire
     * after the specified {@code targetExpirationDate}. The returned Options is the first one a
     * list that is sorted by {@code expirationDate} and distance to the defined {@code
     * underlyingSpot} (the Option with the minimum distance coming first).
     * In simulation mode, if {@code simulateOptions} is configured, a dummy Option will be created
     * if none was found.
     */
    public Option getOptionByMinExpirationAndMinStrikeDistance(long underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot, OptionType optionType);

    /**
     * Gets the first Option of the give {@code underlyingId} and {@code optionType} that expires
     * after the specified {@code targetExpirationDate} and has a strike that is lower (for PUTS)
     * resp. higher (for Calls) than the {@code underlyingSpot}. The returned Option is the first in
     * a list of Options that is sorted by their {@code expirationDate} and distance to the defined
     * {@code underlyingSpot} (the Option with the minimum distance coming first).
     * In simulation mode, if {@code simulateOptions} is configured, a dummy Option will be created
     * if none was found.
     */
    public Option getOptionByMinExpirationAndStrikeLimit(long underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot, OptionType optionType);

    /**
     * Gets all Options of the give {@code underlyingId} and {@code optionType} that expire after
     * the specified {@code targetExpirationDate} and have Ticks on the given {@code date}. The
     * returned Options are sorted by their {@code expirationDate} and distance to the defined
     * {@code underlyingSpot} (the Option with the minimum distance coming first).
     */
    public Option getOptionByMinExpirationAndMinStrikeDistanceWithTicks(long underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot, OptionType optionType, Date date);

    /**
     * Gets all Options of the give {@code underlyingId} and {@code optionType} that expire after
     * the specified {@code targetExpirationDate}, have a strike that is lower (for PUTS) resp.
     * higher (for Calls) than the {@code underlyingSpot} and have Ticks on the given {@code date}.
     * The returned Options are sorted by their {@code expirationDate} and distance to the defined
     * {@code underlyingSpot} (the Option with the minimum distance coming first).
     */
    public Option getOptionByMinExpirationAndStrikeLimitWithTicks(long underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot, OptionType optionType, Date date);

}
