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
package ch.algotrader.enumeration;

/**
 * The Type of Expiration Logic utilized by a {@link ch.algotrader.entity.security.Future Future} or
 * {@link ch.algotrader.entity.security.Option Option}.
 */
public enum ExpirationType {

    /**
     * Expires on every next Friday.
     */
    NEXT_FRIDAY,

    /**
     * Expires on the 3rd Friday every Month.
     */
    NEXT_3_RD_FRIDAY,

    /**
     * Expires on the 3rd Friday in the 3-month Cycle (March, June, Sept and December)
     */
    NEXT_3_RD_FRIDAY_3_MONTHS,

    /**
     * Expires on the 3rd Monday in the 3-month Cycle (March, June, Sept and December)
     */
    NEXT_3_RD_MONDAY_3_MONTHS,

    /**
     * Expires 30 days before the 3rd Friday every Month.
     */
    THIRTY_DAYS_BEFORE_NEXT_3_RD_FRIDAY;

    private static final long serialVersionUID = 6314235244184041823L;

}
