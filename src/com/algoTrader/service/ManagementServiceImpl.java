package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.algoTrader.entity.Account;
import com.algoTrader.entity.PositionDao;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TransactionDao;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.vo.PositionVO;
import com.algoTrader.vo.TickVO;
import com.algoTrader.vo.TransactionVO;

public class ManagementServiceImpl extends ManagementServiceBase {

    private static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy kk:mm");
    private static String currency = PropertiesUtil.getProperty("strategie.currency");
    private static String isin = PropertiesUtil.getProperty("strategie.isin");


    protected String handleGetCurrentTime() throws Exception {

        return format.format(new Date(EsperService.getCurrentTime()));
    }

    protected BigDecimal handleGetAccountCashBalance() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getCashBalance();
    }

    protected BigDecimal handleGetAccountSecuritiesCurrentValue() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getSecuritiesCurrentValue();
    }

    protected BigDecimal handleGetAccountMaintenanceMargin() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getMaintenanceMargin();
    }

    protected BigDecimal handleGetAccountNetLiqValue() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getNetLiqValue();
    }

    protected BigDecimal handleGetAccountAvailableFunds() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getAvailableFunds();
    }

    protected int handleGetAccountOpenPositionCount() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getOpenPositions().size();
    }

    protected double handleGetAccountLeverage() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getLeverage();
    }

    protected BigDecimal handleGetAccountUnderlaying() throws Exception {

        Security underlaying = getSecurityDao().findByISIN(isin);
        Tick tick = underlaying.getLastTick();
        if (tick != null) {
            return tick.getLast();
        } else {
            return null;
        }
    }

    protected BigDecimal handleGetAccountVolatility() throws Exception {

        Security underlaying = getSecurityDao().findByISIN(isin);
        Security volatility = underlaying.getVolatility();
        Tick tick = volatility.getLastTick();
        if (tick != null) {
            return tick.getLast();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected List<TickVO> handleGetDataLastTicks() throws Exception {

        List ticks = getTickDao().getLastTicks();
        getTickDao().toTickVOCollection(ticks);
        return ticks;
    }

    @SuppressWarnings("unchecked")
    protected List<PositionVO> handleGetDataOpenPositions() throws Exception {

        return getPositionDao().findOpenPositions(PositionDao.TRANSFORM_POSITIONVO);
    }

    @SuppressWarnings("unchecked")
    protected List<TransactionVO> handleGetDataTransactions() throws Exception {

        return getTransactionDao().findLastNTransactions(TransactionDao.TRANSFORM_TRANSACTIONVO, 10);
    }
}
