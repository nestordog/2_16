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

import java.util.Date;
import java.util.Map;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.visitor.TickValidationVisitor;

import com.espertech.esper.collection.Pair;

/**
 * Provides service convenience methods.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ServiceUtil {

    /**
     * Gets the current {@link ch.algotrader.entity.strategy.PortfolioValue} of the system
     */
    public static PortfolioValue getPortfolioValue() {

        return ServiceLocator.instance().getPortfolioService().getPortfolioValue();
    }

    /**
     * Returns true if the MarketDataWindow contains any {@link ch.algotrader.entity.marketData.MarketDataEvent MarketDataEvents}
     */
    @SuppressWarnings("unchecked")
    public static boolean hasCurrentMarketDataEvents() {

        Map<String, Long> map = (Map<String, Long>) ServiceLocator.instance().getEngineManager().getServerEngine().executeSingelObjectQuery("select count(*) as cnt from MarketDataWindow");
        return (map.get("cnt") > 0);
    }

    /**
     * attaches the fully initialized Security as well as the specified Date to the Tick contained in the {@link com.espertech.esper.collection.Pair}
     */
    public static Tick completeTick(Pair<Tick, Object> pair) {

        Tick tick = pair.getFirst();

        int securityId = tick.getSecurity().getId();

        Security security = LookupUtil.getSecurityInitialized(securityId);
        tick.setSecurity(security);

        return tick;
    }

    /**
     * return true if the tick passed the entity specific criteria defined by {@link TickValidationVisitor}
     */
    public static boolean isTickValid(Security security, Tick tick) {

        return security.accept(TickValidationVisitor.INSTANCE, tick);
    }

    /**
     * Returns true if the specified {@code currentDateTime} is within the Market Hours of the specified {@link ch.algotrader.entity.security.SecurityFamily}
     */
    public static boolean isMarketOpen(SecurityFamily securityFamily, Date currentDateTime) {

        return ServiceLocator.instance().getCalendarService().isOpen(securityFamily.getExchange().getId(), currentDateTime);
    }

}
