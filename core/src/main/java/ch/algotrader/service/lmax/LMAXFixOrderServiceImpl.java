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
package ch.algotrader.service.lmax;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.lmax.LMAXFixOrderMessageFactory;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.SimpleOrderType;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.fix.fix44.Fix44OrderService;
import ch.algotrader.service.fix.fix44.Fix44OrderServiceImpl;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class LMAXFixOrderServiceImpl extends Fix44OrderServiceImpl implements Fix44OrderService {

    private static final long serialVersionUID = 5822672325566918821L;

    public LMAXFixOrderServiceImpl(
            final FixAdapter fixAdapter,
            final OrderRegistry orderRegistry,
            final OrderPersistenceService orderPersistenceService,
            final CommonConfig commonConfig) {

        super(fixAdapter, orderRegistry, orderPersistenceService, new LMAXFixOrderMessageFactory(), commonConfig);
    }

    @Override
    public void prepareSendOrder(SimpleOrder order, NewOrderSingle newOrder) {
    }

    @Override
    public void prepareModifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest) {
    }

    @Override
    public void prepareCancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest) {
    }

    @Override
    public String getOrderServiceType() {

        return OrderServiceType.LMAX_FIX.name();
    }

    @Override
    public TIF getDefaultTIF(final SimpleOrderType type) {
        if (type == SimpleOrderType.MARKET) {
            return TIF.IOC;
        } else {
            return TIF.DAY;
        }
    }

}
