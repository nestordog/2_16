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

import java.util.concurrent.atomic.AtomicLong;

import quickfix.field.SubscriptionRequestType;
import quickfix.fix42.Message;

/**
 * Trading Technologies position request factory.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TTFixPositionRequestFactory {

    private final AtomicLong count = new AtomicLong(0);

    public Message create() {

        Message request = new Message();
        request.getHeader().setField(new quickfix.field.MsgType("UAN"));
        request.setString(16710, "uan-at-" + this.count.incrementAndGet());
        request.setInt(16724, 1); // PosReqType TRADES
        request.setChar(SubscriptionRequestType.FIELD, '0');

        return request;
    }

}
