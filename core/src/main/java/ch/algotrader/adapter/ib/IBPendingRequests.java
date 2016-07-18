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
package ch.algotrader.adapter.ib;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.ib.client.ContractDetails;

import ch.algotrader.concurrent.PromiseImpl;
import ch.algotrader.entity.marketData.Bar;

/**
 * Class keeping track of all pending contract details and historic data requests
 */
public final class IBPendingRequests {

    private final ConcurrentMap<Integer, IBPendingRequest<Bar>> historicDataRequestMap;
    private final ConcurrentMap<Integer, IBPendingRequest<ContractDetails>> contractRequestMap;

    public IBPendingRequests() {
        this.historicDataRequestMap = new ConcurrentHashMap<>();
        this.contractRequestMap = new ConcurrentHashMap<>();
    }

    public IBPendingRequest<ContractDetails> getContractDetailRequest(final int requestId) {

        return contractRequestMap.get(requestId);
    }

    public IBPendingRequest<Bar> getHistoricDataRequest(final int requestId) {

        return historicDataRequestMap.get(requestId);
    }

    public IBPendingRequest<ContractDetails> addContractDetailRequest(final int requestId, final PromiseImpl<List<ContractDetails>> promise) {

        IBPendingRequest<ContractDetails> pendingRequest = new IBPendingRequest<>(promise);
        this.contractRequestMap.put(requestId, pendingRequest);
        return pendingRequest;
    }

    public IBPendingRequest<Bar> addHistoricDataRequest(final int requestId, final PromiseImpl<List<Bar>> promise) {

        IBPendingRequest<Bar> pendingRequest = new IBPendingRequest<>(promise);
        this.historicDataRequestMap.put(requestId, pendingRequest);
        return pendingRequest;
    }

    public IBPendingRequest<ContractDetails> removeContractDetailRequest(final int requestId) {

        return contractRequestMap.remove(requestId);
    }

    public IBPendingRequest<Bar> removeHistoricDataRequest(final int requestId) {

        return historicDataRequestMap.remove(requestId);
    }

    public IBPendingRequest<?> removeRequest(final int requestId) {

        IBPendingRequest<?> pendingRequest = contractRequestMap.remove(requestId);
        if (pendingRequest == null) {
            pendingRequest = historicDataRequestMap.remove(requestId);
        }
        return pendingRequest;
    }

    public void fail(final int requestId, final int errorCode, final String errorMsg) {

        if (requestId < 1) {
            failAll(new IBSessionException(errorCode, errorMsg));
        } else {
            IBPendingRequest<?> pendingRequest = removeRequest(requestId);
            if (pendingRequest != null) {
                pendingRequest.fail(new IBSessionException(errorCode, errorMsg));
            }
        }
    }

    public void failAll(final Exception ex) {
        for (IBPendingRequest<Bar> pendingRequest: this.historicDataRequestMap.values()) {
            pendingRequest.fail(ex);
        }
        this.historicDataRequestMap.clear();
        for (IBPendingRequest<ContractDetails> pendingRequest: this.contractRequestMap.values()) {
            pendingRequest.fail(ex);
        }
        this.contractRequestMap.clear();
    }

}
