package com.algoTrader.entity;

import java.util.Date;
import java.util.List;

import com.algoTrader.criteria.TransactionCriteria;
import com.algoTrader.vo.TransactionVO;

public class TransactionDaoImpl extends TransactionDaoBase {

    @SuppressWarnings("unchecked")
    protected List<TransactionVO> handleGetTransactionsWithinTimerange(Date startDateTime, Date endDateTime, int maxResults) {

        TransactionCriteria criteria = new TransactionCriteria(startDateTime, endDateTime);
        criteria.setMaximumResultSize(maxResults);

        List<TransactionVO> transactions = this.findByCriteria(TransactionDao.TRANSFORM_TRANSACTIONVO, criteria);
        return transactions;
    }

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

        transactionVO.setCurrency(transaction.getAccount().getCurrency());
        Security security = transaction.getSecurity();
        if (security != null) {
            transactionVO.setSymbol(security.getSymbol());
            Tick tick = security.getLastTick();
            if (tick != null) {
                transactionVO.setCurrentValue(tick.getCurrentValue());
            }
        }
    }

    public Transaction transactionVOToEntity(TransactionVO transactionVO) {

        throw new UnsupportedOperationException("transactionVOToEntity not yet implemented.");
    }
}
