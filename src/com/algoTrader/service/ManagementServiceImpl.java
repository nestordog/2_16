package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.algoTrader.entity.Account;
import com.algoTrader.entity.DSlow;
import com.algoTrader.entity.KFast;
import com.algoTrader.entity.KSlow;
import com.algoTrader.entity.PositionDao;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.RuleName;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.vo.PositionVO;
import com.algoTrader.vo.TickVO;
import com.algoTrader.vo.TransactionVO;

public class ManagementServiceImpl extends ManagementServiceBase {

    private static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy kk:mm");
    private static String currency = PropertiesUtil.getProperty("simulation.currency");

    protected String handleGetCurrentTime() throws Exception {

        return format.format(new Date(EsperService.getCurrentTime()));
    }

    protected BigDecimal handleGetPortfolioValue() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getTotalValue();
    }

    protected BigDecimal handleGetBalance() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getCashBalance();
    }

    protected BigDecimal handleGetMargin() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getMargin();
    }

    protected BigDecimal handleGetAvailableAmount() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getAvailableAmount();
    }

    protected int handleGetOpenPositionCount() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getOpenPositions().size();
    }

    protected double handleGetKFast() throws Exception {

        KFast kFast = (KFast)EsperService.getLastEvent(RuleName.CREATE_K_FAST);

        return (kFast != null) ? kFast.getValue() : 0;
    }

    protected double handleGetKSlow() throws Exception {

        KSlow kSlow = (KSlow)EsperService.getLastEvent(RuleName.CREATE_K_SLOW);

        return (kSlow != null) ? kSlow.getValue() : 0;
    }

    protected double handleGetDSlow() throws Exception {

        DSlow dSlow = (DSlow)EsperService.getLastEvent(RuleName.CREATE_D_SLOW);

        return (dSlow != null) ? dSlow.getValue() : 0;
    }

    @SuppressWarnings("unchecked")
    protected List<TickVO> handleGetLastTicks() throws Exception {

        List ticks = getTickDao().getLastTicks();
        getTickDao().toTickVOCollection(ticks);
        return ticks;
    }

    @SuppressWarnings("unchecked")
    protected List<PositionVO> handleGetOpenPositions() throws Exception {

        return getPositionDao().findOpenPositions(PositionDao.TRANSFORM_POSITIONVO);
    }

    @SuppressWarnings("unchecked")
    protected List<TransactionVO> handleGetTransactions() throws Exception {

        return getTransactionDao().getTransactionsWithinTimerange(null, null, 10);
    }

    protected void handleImmediateBuySignal() throws Exception {

        getRuleService().activate(RuleName.IMMEDIATE_BUY_SIGNAL);
    }

    protected void handleClosePosition(int positionId) throws Exception {

        getStockOptionService().closePosition(positionId);
    }

    protected void handleActivate(String ruleName) throws Exception {

        getRuleService().activate(ruleName);
    }

    protected void handleDeactivate(String ruleName) throws Exception {

        getRuleService().deactivate(ruleName);
    }

    protected void handleKillVM() throws Exception {

        System.exit(0);
    }
}
