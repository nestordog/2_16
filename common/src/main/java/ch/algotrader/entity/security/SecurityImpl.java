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
package ch.algotrader.entity.security;

import java.util.Objects;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.entity.marketData.MarketDataEvent;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class SecurityImpl extends Security {

    private static final long serialVersionUID = -6631052475125813394L;

    @Override
    public double getLeverage(MarketDataEvent marketDataEvent, MarketDataEvent underlyingMarketDataEvent) {
        return 0;
    }

    /**
     * generic default margin
     */
    @Override
    public double getMargin(double currentValue, double underlyingCurrentValue) {

        double contractSize = getSecurityFamily().getContractSize();
        CommonConfig commonConfig = ConfigLocator.instance().getCommonConfig();
        return currentValue * contractSize / commonConfig.getInitialMarginMarkup().doubleValue();
    }

    @Override
    public String toString() {

        return getSymbol();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Security) {
            Security that = (Security) obj;
            return Objects.equals(this.getIsin(), that.getIsin()) &&
                        Objects.equals(this.getBbgid(), that.getBbgid()) &&
                        Objects.equals(this.getRic(), that.getRic()) &&
                        Objects.equals(this.getConid(), that.getConid()) &&
                        Objects.equals(this.getLmaxid(), that.getLmaxid()) &&
                        Objects.equals(this.getSymbol(), that.getSymbol());

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 37 + Objects.hashCode(this.getIsin());
        hash = hash * 37 + Objects.hashCode(this.getBbgid());
        hash = hash * 37 + Objects.hashCode(this.getRic());
        hash = hash * 37 + Objects.hashCode(this.getConid());
        hash = hash * 37 + Objects.hashCode(this.getLmaxid());
        hash = hash * 37 + Objects.hashCode(this.getSymbol());
        return hash;
    }
}
