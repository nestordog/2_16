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
package ch.algotrader.vo;

/**
 * @author <a href="mailto:vgolding@algotrader.ch">Vince Golding</a>
 * @version $Revision$ $Date$
 */
import java.io.Serializable;
import java.math.BigDecimal;

public class PositionValuationVO implements Serializable {

  private static final long serialVersionUID = 8575812755346489673L;

  private final BigDecimal marketPrice;
  private final BigDecimal marketValue;
  private final BigDecimal averagePrice;
  private final BigDecimal unrealizedPL;

  public PositionValuationVO(BigDecimal marketPrice, BigDecimal marketValue, BigDecimal averagePrice, BigDecimal unrealizedPL) {
    super();
    this.marketPrice = marketPrice;
    this.marketValue = marketValue;
    this.averagePrice = averagePrice;
    this.unrealizedPL = unrealizedPL;
  }

  public BigDecimal getMarketPrice() {
    return this.marketPrice;
  }

  public BigDecimal getMarketValue() {
    return this.marketValue;
  }

  public BigDecimal getAveragePrice() {
    return this.averagePrice;
  }

  public BigDecimal getUnrealizedPL() {
    return this.unrealizedPL;
  }

}
