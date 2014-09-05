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
public interface SecurityRetrieverService {

    /**
     * Retrieves the specified {@link ch.algotrader.entity.security.Future Future} or {@link
     * ch.algotrader.entity.security.Option Option} chain.
     */
    public void retrieve(int securityFamilyId);

    /**
     * Retrieves all {@link ch.algotrader.entity.security.Stock Stocks} of the specified {@code
     * securityFamily}
     */
    public void retrieveStocks(int securityFamilyId, String symbol);

}
