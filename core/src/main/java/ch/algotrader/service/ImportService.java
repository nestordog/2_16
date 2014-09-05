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

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface ImportService {

    /**
     * Imports Ticks from a csv file into the db.
     * must be run with simulation=false (to get correct values for bid, ask and settlement).
     * It is also recommended to turn of ehache on commandline (to avoid out of memory error)
     */
    public void importTicks(String isin);

    /**
     * Imports Ticks retrieved from www.iVolatility.com into the db.
     */
    public void importIVolTicks(String optionFamilyId, String fileName);

}
