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

package ch.algotrader.json;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TIF;

public abstract class StopOrderVOMixIn {

    public StopOrderVOMixIn(
            @JsonProperty(value = "id", required = false) final long id,
            @JsonProperty(value = "intId", required = false) final String intId,
            @JsonProperty(value = "extId", required = false) final String extId,
            @JsonProperty(value = "dateTime", required = false) final Date dateTime,
            @JsonProperty(value = "side", required = true) final Side side,
            @JsonProperty(value = "quantity", required = true) final long quantity,
            @JsonProperty(value = "tif", required = false) final TIF tif,
            @JsonProperty(value = "tifDateTime", required = false) final Date tifDateTime,
            @JsonProperty(value = "exchangeId", required = false) final long exchangeId,
            @JsonProperty(value = "securityId", required = true) final long securityId,
            @JsonProperty(value = "accountId", required = true) final long accountId,
            @JsonProperty(value = "strategyId", required = true) final long strategyId,
            @JsonProperty(value = "stop", required = true) final BigDecimal stop) {
    }

}


