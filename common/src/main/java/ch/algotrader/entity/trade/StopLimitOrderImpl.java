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
package ch.algotrader.entity.trade;



/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class StopLimitOrderImpl extends StopLimitOrder {

    private static final long serialVersionUID = -6796363895406178181L;

    @Override
    public String getExtDescription() {
        return "stop=" + getStop() + ",limit=" + getLimit();
    }

    @Override
    public void validate() throws OrderValidationException {

        if (getLimit() == null) {
            throw new OrderValidationException("no limit defined for " + this);
        } else if (getStop() == null) {
            throw new OrderValidationException("no stop defined for " + this);
        }
    }
}
