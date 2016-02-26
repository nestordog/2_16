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
package ch.algotrader.entity.trade.algo;

import java.io.Serializable;

/**
 * Contains the current state of an AlgoOrder
  * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
*/
public abstract class AlgoOrderStateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String intId;

    public String getIntId() {
        return this.intId;
    }

    public void setIntId(String order) {
        this.intId = order;
    }

}


