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
package ch.algotrader.entity;

import java.math.BigDecimal;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.security.Security;
import ch.algotrader.vo.TransactionVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TransactionDaoImpl extends TransactionDaoBase {

    private CommonConfig commonConfig;
    public void setCommonConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

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
            transactionVO.setPrice(transaction.getPrice().setScale(this.commonConfig.getPortfolioDigits(), BigDecimal.ROUND_HALF_UP));
        }

        transactionVO.setStrategy(transaction.getStrategy().toString());
        transactionVO.setValue(transaction.getNetValue());
        transactionVO.setTotalCharges(transaction.getTotalCharges());

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
