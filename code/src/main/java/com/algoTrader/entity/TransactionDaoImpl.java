package com.algoTrader.entity;

import com.algoTrader.entity.security.Security;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.vo.TransactionVO;

public class TransactionDaoImpl extends TransactionDaoBase {

    private static final int portfolioDigits = ConfigurationUtil.getBaseConfig().getInt("portfolioDigits");

    public void toTransactionVO(Transaction transaction, TransactionVO transactionVO) {

        super.toTransactionVO(transaction, transactionVO);

        completeTransactionVO(transaction, transactionVO);
    }

    public TransactionVO toTransactionVO(final Transaction transaction) {

        TransactionVO transactionVO = super.toTransactionVO(transaction);

        completeTransactionVO(transaction, transactionVO);

        return transactionVO;
    }

    private void completeTransactionVO(Transaction transaction, TransactionVO transactionVO) {


        Security security = transaction.getSecurity();
        if (security != null) {

            transactionVO.setSymbol(security.getSymbol());
            transactionVO.setPrice(transaction.getPrice().setScale(security.getSecurityFamily().getScale()));
        } else {
            transactionVO.setPrice(transaction.getPrice().setScale(portfolioDigits));
        }

        transactionVO.setValue(transaction.getValue().setScale(portfolioDigits));
        transactionVO.setCommission(transaction.getCommission().setScale(portfolioDigits));
    }

    public Transaction transactionVOToEntity(TransactionVO transactionVO) {

        throw new UnsupportedOperationException("transactionVOToEntity not yet implemented.");
    }
}
