/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service.lmax;

import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.FeedType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class LMAXFixMarketDataServiceImpl extends LMAXFixMarketDataServiceBase {

    private static final long serialVersionUID = 1144501885597028244L;

    @Override
    protected FeedType handleGetFeedType() throws Exception {

        return FeedType.LMAX;
    }

    @Override
    protected String handleGetSessionQualifier() throws Exception {

        return "LMAXMD";
    }

    @Override
    protected void handleSendSubscribeRequest(Security security) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    protected void handleSendUnsubscribeRequest(Security security) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    protected int handleGetTickerId(Security security) throws Exception {
        // TODO Auto-generated method stub
        return 0;
    }
}
