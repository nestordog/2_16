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
package ch.algotrader.service;

import java.math.BigDecimal;
import java.util.Date;

import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.vo.ATMVolVO;
import ch.algotrader.vo.SABRSmileVO;
import ch.algotrader.vo.SABRSurfaceVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
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
     * Prints SABR {@code alpha} / {@code rho} / {@code volvol} as well as Call & Put {@code
     * atmVola} for defined underlying {@code isin}, {@code expirationDate} and {@code optionType}
     * The calculation is done from {code startDate} to {@code expirationDate} 09:00 - 17:20:00.
     */
    public void printSABRSmileByOptionPrice(String isin, Date expirationDate, OptionType optionType, Date startDate);

    /**
     * Prints SABR {@code alpha} / {@code rho} / {@code volvol} for defined underlying {@code isin}
     * and {@code duration}.
     * The calculation is done from {@code startDate} to {@code endDate} once a day.
     */
    public void printSABRSmileByIVol(String isin, Duration duration, Date startDate, Date endDate);

    /**
     * Calculates the {@link SABRSmileVO} for defined {@code underlyingId}, {@code optionType} and
     * {@code expirationDate} for the specified {@code date}.
     */
    public SABRSmileVO calibrateSABRSmileByOptionPrice(long underlyingId, OptionType type, Date expirationDate, Date date);

    /**
     * Calculates the {@link SABRSmileVO} for defined {@code underlyingId}, and {@code duration} for
     * the specified {@code date}.
     */
    public SABRSmileVO calibrateSABRSmileByIVol(long underlyingId, Duration duration, Date date);

    /**
     * Calculates a {@link SABRSurfaceVO} for defined {@code underlyingId}, and {@code duration} for
     * the specified {@code date}
     */
    public SABRSurfaceVO calibrateSABRSurfaceByIVol(long underlyingId, Date date);

    /**
     * Calculates the {@link ATMVolVO} for defined {@code underlying} and {@code date}
     * the calculation considers the Call Option and Put Option closest to the spot.
     */
    public ATMVolVO calculateATMVol(Security underlying, Date date);

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
