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

import ch.algotrader.entity.strategy.Measurement;
import java.util.Date;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface MeasurementService {

    /**
     * Creates a Measurement with the specified {@code name} and {@code value} assigned to the
     * specified Strategy. The {@code date} of the Measurement is set to the current Time.
     */
    public Measurement createMeasurement(String strategyName, String name, Object value);

    /**
     * Creates a Measurement with the specified {@code name}, {@code value} and {@code date}
     * assigned to the specified Strategy.
     */
    public Measurement createMeasurement(String strategyName, String name, Date date, Object value);

    /**
     * Deletes the specified Measurement.
     */
    public void deleteMeasurement(int measurementId);

}
