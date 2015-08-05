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
package ch.algotrader.dao;

import java.math.BigDecimal;

import org.apache.commons.lang.Validate;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.security.Security;
import ch.algotrader.vo.TransactionVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TransactionVOProducer implements EntityConverter<Transaction, TransactionVO> {

    private final CommonConfig commonConfig;

    public TransactionVOProducer(final CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    @Override
    public TransactionVO convert(final Transaction entity) {

        Validate.notNull(entity, "Transaction is null");

        TransactionVO vo = new TransactionVO();

        vo.setId(entity.getId());
        vo.setDateTime(entity.getDateTime());
        vo.setQuantity(entity.getQuantity());
        vo.setType(entity.getType());
        // No conversion for target.strategy (can't convert source.getStrategy():Strategy to String)
        // No conversion for target.account (can't convert source.getAccount():ch.algotrader.entity.Account to String)
        vo.setCurrency(entity.getCurrency());
        vo.setPrice(entity.getPrice());

        Security security = entity.getSecurity();
        if (security != null) {
            vo.setName(security.toString());

            int scale = security.getSecurityFamily().getScale();
            vo.setPrice(entity.getPrice().setScale(scale, BigDecimal.ROUND_HALF_UP));
        } else {
            vo.setPrice(entity.getPrice().setScale(this.commonConfig.getPortfolioDigits(), BigDecimal.ROUND_HALF_UP));
        }

        vo.setStrategy(entity.getStrategy().toString());
        vo.setValue(entity.getNetValue());
        vo.setTotalCharges(entity.getTotalCharges());

        Account account = entity.getAccount();
        if (account != null) {
            vo.setAccount(account.toString());
        }

        return vo;
    }

}
