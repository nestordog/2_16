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
package com.algoTrader.entity;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.security.Security;
import com.algoTrader.vo.TransactionVO;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TransactionDaoImpl extends TransactionDaoBase {

    private @Value("${misc.portfolioDigits}") int portfolioDigits;

    @Override
    public void toTransactionVO(Transaction transaction, TransactionVO transactionVO) {

        super.toTransactionVO(transaction, transactionVO);

        completeTransactionVO(transaction, transactionVO);
    }

    @Override
    public TransactionVO toTransactionVO(final Transaction transaction) {

        TransactionVO transactionVO = super.toTransactionVO(transaction);

        completeTransactionVO(transaction, transactionVO);

        return transactionVO;
    }

    private void completeTransactionVO(Transaction transaction, TransactionVO transactionVO) {

        Security security = transaction.getSecurity();
        if (security != null) {
            transactionVO.setName(security.toString());

            int scale = security.getSecurityFamily().getScale();
            transactionVO.setPrice(transaction.getPrice().setScale(scale, BigDecimal.ROUND_HALF_UP));
        } else {
            transactionVO.setPrice(transaction.getPrice().setScale(this.portfolioDigits, BigDecimal.ROUND_HALF_UP));
        }

        transactionVO.setStrategy(transaction.getStrategy().toString());
        transactionVO.setValue(transaction.getNetValue());
        transactionVO.setCommission(transaction.getTotalCommission());

        Account account = transaction.getAccount();
        if (account != null) {
            transactionVO.setAccount(account.toString());
        }
    }

    @Override
    public Transaction transactionVOToEntity(TransactionVO transactionVO) {

        throw new UnsupportedOperationException("transactionVOToEntity not yet implemented.");
    }
}
