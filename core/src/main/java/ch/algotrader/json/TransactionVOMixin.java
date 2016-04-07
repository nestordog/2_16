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

import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.TransactionType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author <a href="mailto:vgolding@algotrader.ch">Vince Golding</a>
 * @version $Revision$ $Date$
 */
public class TransactionVOMixin {

  public TransactionVOMixin(
      @JsonProperty(value = "id", required = false) final long id,
      @JsonProperty(value = "uuid", required = false) final String uuid,
      @JsonProperty(value = "dateTime", required = true) final Date dateTime,
      @JsonProperty(value = "settlementDate", required = false) final Date settlementDate,
      @JsonProperty(value = "extId", required = false) final String extId,
      @JsonProperty(value = "intOrderId", required = false) final String intOrderId,
      @JsonProperty(value = "extOrderId", required = false) final String extOrderId,
      @JsonProperty(value = "quantity", required = true) final long quantity,
      @JsonProperty(value = "price", required = true) final BigDecimal price,
      @JsonProperty(value = "executionCommission", required = false) final BigDecimal executionCommission,
      @JsonProperty(value = "clearingCommission", required = false) final BigDecimal clearingCommission,
      @JsonProperty(value = "fee", required = false) final BigDecimal fee,
      @JsonProperty(value = "currency", required = false) final Currency currency,
      @JsonProperty(value = "type", required = true) final TransactionType type,
      @JsonProperty(value = "description", required = false) final String description,
      @JsonProperty(value = "accountId", required = false) final long accountId,
      @JsonProperty(value = "positionId", required = false) final long positionId,
      @JsonProperty(value = "securityId", required = false) final long securityId,
      @JsonProperty(value = "strategyId", required = true) final long strategyId) {
  }
}
