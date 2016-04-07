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
package ch.algotrader.entity.security;

import java.util.Date;

import ch.algotrader.enumeration.Duration;
import ch.algotrader.util.DateUtil;
import ch.algotrader.visitor.SecurityVisitor;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class GenericFutureImpl extends GenericFuture {

    private static final long serialVersionUID = -5567218864363234118L;

    @Override
    public <R, P> R accept(SecurityVisitor<R, ? super P> visitor, P param) {

        return visitor.visitGenericFuture(this, param);
    }

    @Override
    public Date getExpiration(Date dateTime) {

        GenericFutureFamily family = (GenericFutureFamily) getSecurityFamily();

        int months = (int) (getDuration().getValue() / Duration.MONTH_1.getValue());
        return DateUtil.getExpirationDateNMonths(family.getExpirationType(), dateTime, months);
    }
}
