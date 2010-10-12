package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.algoTrader.entity.Account;
import com.algoTrader.entity.DSlow;
import com.algoTrader.entity.KFast;
import com.algoTrader.entity.KSlow;
import com.algoTrader.entity.PositionDao;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.RuleName;
import com.algoTrader.service.ib.IbService;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.vo.PositionVO;
import com.algoTrader.vo.TickVO;
import com.algoTrader.vo.TransactionVO;

public class ManagementServiceImpl extends ManagementServiceBase {

    private static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy kk:mm");
    private static String currency = PropertiesUtil.getProperty("strategie.currency");


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

    protected double handleGetStochasticCallKFast() throws Exception {

        KFast kFast = (KFast)EsperService.getLastEvent(RuleName.CREATE_K_FAST);

        return (kFast != null) ? kFast.getCall() : 0;
    }

    protected double handleGetStochasticCallKSlow() throws Exception {

        KSlow kSlow = (KSlow)EsperService.getLastEvent(RuleName.CREATE_K_SLOW);

        return (kSlow != null) ? kSlow.getCall() : 0;
    }

    protected double handleGetStochasticCallDSlow() throws Exception {

        DSlow dSlow = (DSlow)EsperService.getLastEvent(RuleName.CREATE_D_SLOW);

        return (dSlow != null) ? dSlow.getCall() : 0;
    }

    protected double handleGetStochasticPutKFast() throws Exception {

        KFast kFast = (KFast)EsperService.getLastEvent(RuleName.CREATE_K_FAST);

        return (kFast != null) ? kFast.getPut() : 0;
    }

    protected double handleGetStochasticPutKSlow() throws Exception {

        KSlow kSlow = (KSlow)EsperService.getLastEvent(RuleName.CREATE_K_SLOW);

        return (kSlow != null) ? kSlow.getPut() : 0;
    }

    protected double handleGetStochasticPutDSlow() throws Exception {

        DSlow dSlow = (DSlow)EsperService.getLastEvent(RuleName.CREATE_D_SLOW);

        return (dSlow != null) ? dSlow.getPut() : 0;
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

        return getTransactionDao().getTransactionsWithinTimerange(null, null, 10);
    }

    protected void handleActivate(String ruleName) throws Exception {

        getRuleService().activate(ruleName);
    }

    protected void handleDeactivate(String ruleName) throws Exception {

        getRuleService().deactivate(ruleName);
    }

    @SuppressWarnings("unchecked")
    protected void handleReconnectIB() throws Exception {

        Set<IbService> services = getDispatcherService().getAllIbServices();

        for (IbService service : services) {
            service.connect();
        }
    }
}
