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
package ch.algotrader.adapter.tt;

import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.adapter.fix.fix42.GenericFix42OrderMessageFactory;
import ch.algotrader.enumeration.TIF;
import quickfix.field.TimeInForce;

/**
 *  Trading Technologies order message factory.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TTFixOrderMessageFactory extends GenericFix42OrderMessageFactory {

    public TTFixOrderMessageFactory() {
        super(new TTSymbologyResolver());
    }

    @Override
    protected TimeInForce resolveTimeInForce(TIF tif) {

        if (tif == TIF.ATC) {
            throw new FixApplicationException("Time in force '" + tif + "' is not supported by TT");
        }
        return super.resolveTimeInForce(tif);
    }

}
