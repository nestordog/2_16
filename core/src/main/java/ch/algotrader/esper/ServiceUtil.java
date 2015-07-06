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
package ch.algotrader.esper;

import com.espertech.esper.collection.Pair;

import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Security;
import ch.algotrader.visitor.TickValidationVisitor;

/**
 * Provides service convenience methods.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ServiceUtil {

    /**
     * attaches the fully initialized Security to the Tick contained in the {@link com.espertech.esper.collection.Pair}
     */
    public static Tick completeTick(Pair<Tick, Object> pair) {

        Tick tick = pair.getFirst();

        long securityId = tick.getSecurity().getId();

        Security security = LookupUtil.getSecurityInitialized(securityId);
        tick.setSecurity(security);

        return tick;
    }

    /**
     * return true if the tick passed the entity specific criteria defined by {@link TickValidationVisitor}
     */
    public static boolean isTickValid(Tick tick) {

        return tick.getSecurity().accept(TickValidationVisitor.INSTANCE, tick);
    }

}
