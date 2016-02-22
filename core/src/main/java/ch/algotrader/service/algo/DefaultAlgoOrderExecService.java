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
package ch.algotrader.service.algo;

import java.util.List;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.entity.marketData.TickI;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.trade.AlgoOrder;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.service.MarketDataCacheService;
import ch.algotrader.service.ServiceException;

/**
 * Default algo execution service.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class DefaultAlgoOrderExecService implements AlgoOrderExecService<AlgoOrder> {

    private final MarketDataCacheService marketDataCacheService;

    public DefaultAlgoOrderExecService(final MarketDataCacheService marketDataCacheService) {

        Validate.notNull(marketDataCacheService, "MarketDataCacheService is null");

        this.marketDataCacheService = marketDataCacheService;
    }

    @Override
    public void validate(final AlgoOrder order) throws OrderValidationException {

        Security security = order.getSecurity();
        MarketDataEventVO marketDataEvent = this.marketDataCacheService.getCurrentMarketDataEvent(security.getId());
        if (marketDataEvent == null) {
            throw new OrderValidationException("no marketDataEvent available to initialize algo order");
        }
    }

    @Override
    public List<SimpleOrder> getInitialOrders(final AlgoOrder order) {

        Security security = order.getSecurity();
        MarketDataEventVO marketDataEvent = this.marketDataCacheService.getCurrentMarketDataEvent(security.getId());
        if (marketDataEvent instanceof TickI) {
            return order.getInitialOrders((TickI) marketDataEvent);
        } else {
            throw new ServiceException("Unexpected market data event type: " + marketDataEvent.getClass());
        }
    }

}
