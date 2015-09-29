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
package ch.algotrader.adapter.tt;

import ch.algotrader.entity.security.SecurityFamily;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityReqID;
import quickfix.field.SecurityType;
import quickfix.field.Symbol;
import quickfix.fix42.SecurityDefinitionRequest;

/**
 * Trading Technologies security definition request factory.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TTSecurityDefinitionRequestFactory {

    public SecurityDefinitionRequest create(final String requestId, final SecurityFamily securityFamily, final String securityType) {

        SecurityDefinitionRequest request = new SecurityDefinitionRequest();
        request.set(new SecurityReqID(requestId));
        request.set(new SecurityType(securityType));
        String code = securityFamily.getExchange().getCode();
        request.set(new SecurityExchange(code));

        String symbolRoot = securityFamily.getSymbolRoot();
        if (symbolRoot != null) {
            request.set(new Symbol(symbolRoot));
        }

        return request;
    }

}
