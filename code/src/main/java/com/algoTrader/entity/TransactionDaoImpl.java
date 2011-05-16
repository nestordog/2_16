package com.algoTrader.entity;

import com.algoTrader.entity.security.Security;
import com.algoTrader.vo.TransactionVO;

public class TransactionDaoImpl extends TransactionDaoBase {

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

        transactionVO.setValue(transaction.getValue());

        Security security = transaction.getSecurity();
        if (security != null) {
            transactionVO.setSymbol(security.getSymbol());
        }
    }

    public Transaction transactionVOToEntity(TransactionVO transactionVO) {

        throw new UnsupportedOperationException("transactionVOToEntity not yet implemented.");
    }
}
