package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Account;
import com.algoTrader.entity.DSlow;
import com.algoTrader.entity.KFast;
import com.algoTrader.entity.KSlow;
import com.algoTrader.entity.PositionDao;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.RuleName;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.vo.InterpolationVO;
import com.algoTrader.vo.PositionVO;
import com.algoTrader.vo.TickVO;
import com.algoTrader.vo.TransactionVO;

public class ManagementServiceImpl extends ManagementServiceBase {

    private static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy kk:mm");
    private static String currency = PropertiesUtil.getProperty("simulation.currency");

    protected String handleGetCurrentTime() throws Exception {

        return format.format(new Date(EsperService.getCurrentTime()));
    }

    protected BigDecimal handleGetAccountTotalValue() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getTotalValue();
    }

    protected BigDecimal handleGetAccountBalance() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getCashBalance();
    }

    protected BigDecimal handleGetAccountMargin() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getMargin();
    }

    protected BigDecimal handleGetAccountAvailableAmount() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getAvailableAmount();
    }

    protected int handleGetAccountOpenPositionCount() throws Exception {

        Account account = getAccountDao().findByCurrency(Currency.fromString(currency));
        return account.getOpenPositions().size();
    }

    protected double handleGetStochasticKFast() throws Exception {

        KFast kFast = (KFast)EsperService.getLastEvent(RuleName.CREATE_K_FAST);

        return (kFast != null) ? kFast.getValue() : 0;
    }

    protected double handleGetStochasticKSlow() throws Exception {

        KSlow kSlow = (KSlow)EsperService.getLastEvent(RuleName.CREATE_K_SLOW);

        return (kSlow != null) ? kSlow.getValue() : 0;
    }

    protected double handleGetStochasticDSlow() throws Exception {

        DSlow dSlow = (DSlow)EsperService.getLastEvent(RuleName.CREATE_D_SLOW);

        return (dSlow != null) ? dSlow.getValue() : 0;
    }

    protected double handleGetInterpolationA() throws Exception {

        InterpolationVO interpolation = ServiceLocator.instance().getSimulationService().getInterpolation();

        if (interpolation == null) return 0;

        return interpolation.getA();
    }

    protected double handleGetInterpolationB() throws Exception {

        InterpolationVO interpolation = ServiceLocator.instance().getSimulationService().getInterpolation();

        if (interpolation == null) return 0;

        return interpolation.getB();
    }

    protected double handleGetInterpolationR() throws Exception {

        InterpolationVO interpolation = ServiceLocator.instance().getSimulationService().getInterpolation();

        if (interpolation == null) return 0;

        return interpolation.getR();
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

    protected void handleKillVM() throws Exception {

        System.exit(0);
    }
}
