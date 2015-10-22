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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ch.algotrader.concurrent.PromiseImpl;

/**
 * Class keeping track of all pending security definition requests
 */
public final class TTPendingRequests {

    private final ConcurrentMap<String, TTPendingRequest<TTSecurityDefVO>> securityDefRequestMap;

    public TTPendingRequests() {
        this.securityDefRequestMap = new ConcurrentHashMap<>();
    }

    public TTPendingRequest<TTSecurityDefVO> getSecurityDefinitionRequest(final String requestId) {

        return securityDefRequestMap.get(requestId);
    }

    public TTPendingRequest<TTSecurityDefVO> addSecurityDefinitionRequest(
            final String requestId, final PromiseImpl<List<TTSecurityDefVO>> promise) {

        TTPendingRequest<TTSecurityDefVO> pendingRequest = new TTPendingRequest<>(promise);
        this.securityDefRequestMap.putIfAbsent(requestId, pendingRequest);
        return pendingRequest;
    }

    public TTPendingRequest<TTSecurityDefVO> removeSecurityDefinitionRequest(final String requestId) {

        return securityDefRequestMap.remove(requestId);
    }

}
