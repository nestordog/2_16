package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.stockOption.StockOptionUtil;
import com.espertech.esper.client.time.CurrentTimeEvent;

import edu.emory.mathcs.backport.java.util.Arrays;

public class TestServiceImpl extends TestServiceBase {

    protected void handleTest() {

        // Collection col = getPositionDao().loadAll();
        // System.out.println(col.size());

         double cv = getStrategyDao().getPortfolioSecuritiesCurrentValueDouble();
        System.out.println(cv);
    }

    protected void handleTestSelects(int number) throws Exception {

        switch (number) {

        case 1:
            getSecurityDao().load(6);
            break;
        case 2:
            getSecurityDao().loadAll();
            break;
        case 3:
            getSecurityDao().findByIsin("1OSMIMA003QQ");
            break;
        case 4:
            getSecurityDao().findSecuritesInPortfolio();
            break;
        case 5:
            getSecurityDao().findSecuritiesOnWatchlist();
            break;
        case 6:
            getSecurityDao().findSecuritiesOnActiveWatchlist();
            break;
        case 7:
            getSecurityDao().findByStrategyName("SMI");
            break;
        case 8:
            getSecurityDao().findByIdFetched(6);
            break;

        case 9:
            getStockOptionDao().load(6);
            break;
        case 10:
            getStockOptionDao().loadAll();
            break;
        case 11:
            getStockOptionDao().findNearestStockOption(4, new Date(), new BigDecimal(6500), "PUT");
            break;
        case 12:
            getStockOptionDao().findStockOptionsOnWatchlist();
            break;

        case 13:
            getPositionDao().load(4986);
            break;
        case 14:
            getPositionDao().loadAll();
            break;
        case 15:
            getPositionDao().findOpenPositions();
            break;
        case 16:
            getPositionDao().findOpenPositionsByStrategy("SMI");
            break;
        case 17:
            getPositionDao().findByIdFetched(4986);
            break;
        case 18:
            getPositionDao().findBySecurityAndStrategy(13761, "SMI");
            break;

        case 19:
            getStrategyDao().load(1);
            break;
        case 20:
            getStrategyDao().loadAll();
            break;
        case 21:
            getStrategyDao().findByName("SMI");
            break;
        case 22:
            getStrategyDao().findByNameFetched("SMI");
            break;
        case 23:
            getStrategyDao().findAutoActivateStrategies();
            break;

        case 24:
            getTransactionDao().load(7);
            break;
        case 25:
            getTransactionDao().loadAll();
            break;
        case 26:
            getTransactionDao().findAllTrades();
            break;
        case 27:
            getTransactionDao().findAllCashflows();
            break;
        case 28:
            getTransactionDao().findLastNTransactions(10);
            break;
        case 29:
            getTransactionDao().findLastNTransactionsByStrategy(10, "SMI");
            break;

        case 30:
            getRuleDao().load(32);
            break;
        case 31:
            getRuleDao().loadAll();
            break;
        case 32:
            getRuleDao().findByName("GET_LAST_TICK");
            break;
        case 33:
            getRuleDao().findAutoActivateRules("SMI");
            break;
        case 34:
            getRuleDao().findPreparedRules();
            break;
        case 35:
            getRuleDao().findInitRules("SMI");
            break;

        case 36:
            getTickDao().load(370552);
            break;
        case 37:
            getTickDao().findByDateTypeAndExpiration(new Date(), OptionType.PUT, new Date());
            break;
        case 38:
            getTickDao().findBySecurity(9);
            break;
        case 39:
            getTickDao().findByDateAndSecurity(new Date(), 4);
            break;
        case 40:
            getTickDao().findLastNTicksForSecurity(4, 10);
            break;
        case 41:
            getTickDao().findLastNTickIdsForSecurity(4, 10);
            break;
        case 42:
            getTickDao().findEndOfDayTickIds(4, 10);
            break;
        case 43:
            getTickDao().findByIdsFetched(Arrays.asList(new Integer[] { new Integer(4), new Integer(5) }));
            break;

        case 44:
            getSecurityFamilyDao().load(6);
            break;
        case 45:
            getSecurityFamilyDao().loadAll();
            break;
        case 46:
            getSecurityFamilyDao().findByIsin("1OSMIMA003QQ");
            break;

        case 47:
            getStockOptionFamilyDao().load(6);
            break;
        case 48:
            getStockOptionFamilyDao().loadAll();
            break;
        case 49:
            getStockOptionFamilyDao().findByUnderlaying(2);
            break;

        }
    }

    protected void handleTestSabr() throws Exception {

        String dateString = "07.12.2010 09:13:00";
        int underlayingId = 4;
        int volaId = 5;
        int stockOptionId = 717;

        Date date = (new SimpleDateFormat("dd.MM.yyyy kk:mm:ss")).parse(dateString);

        RuleService ruleService = (RuleService) ServiceLocator.serverInstance().getService("ruleService");
        ruleService.initServiceProvider(StrategyImpl.BASE);
        ruleService.sendEvent(StrategyImpl.BASE, new CurrentTimeEvent(date.getTime()));

        double underlayingValue = getTickDao().findByDateAndSecurity(date, underlayingId).getCurrentValueDouble();
        double volaValue = getTickDao().findByDateAndSecurity(date, volaId).getCurrentValueDouble() / 100.0;

        StockOption stockOption = (StockOption) getStockOptionDao().load(stockOptionId);

        double optionPrice = StockOptionUtil.getOptionPriceSabr(stockOption, underlayingValue, volaValue);

        System.out.println(optionPrice);
    }
}
