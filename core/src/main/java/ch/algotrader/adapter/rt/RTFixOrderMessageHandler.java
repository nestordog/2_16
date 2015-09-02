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
package ch.algotrader.adapter.rt;

import ch.algotrader.adapter.fix.fix44.GenericFix44OrderMessageHandler;
import ch.algotrader.esper.Engine;
import ch.algotrader.ordermgmt.OpenOrderRegistry;

/**
 * RealTick specific Fix44MessageHandler.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class RTFixOrderMessageHandler extends GenericFix44OrderMessageHandler {

    public RTFixOrderMessageHandler(final OpenOrderRegistry openOrderRegistry, final Engine serverEngine) {
        super(openOrderRegistry, serverEngine);
    }

}
