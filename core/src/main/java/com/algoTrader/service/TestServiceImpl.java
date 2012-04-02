package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.marketData.TickImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockImpl;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.stockOption.StockOptionUtil;
import com.espertech.esper.client.time.CurrentTimeEvent;

public class TestServiceImpl extends TestServiceBase {

    @Override
    protected void handleTest() throws Exception {

        // implement as needed
    }

    @Override
    protected void handleTestPattern() {

        EventService ruleService = ServiceLocator.instance().getEventService();
        ruleService.initServiceProvider("SMI");
        ruleService.deployStatement("SMI", "test", "TEST");

        Security index = getSecurityDao().load(4);
        Security vola = getSecurityDao().load(5);
        Security option = getSecurityDao().load(5557);

        Tick indexTick1 = new TickImpl();
        indexTick1.setId(1);
        indexTick1.setSecurity(index);
        Tick indexTick2 = new TickImpl();
        indexTick2.setId(2);
        indexTick2.setSecurity(index);

        Tick volaTick1 = new TickImpl();
        volaTick1.setId(1);
        volaTick1.setSecurity(vola);
        Tick volaTick2 = new TickImpl();
        volaTick2.setId(2);
        volaTick2.setSecurity(vola);

        Tick optionTick1 = new TickImpl();
        optionTick1.setId(1);
        optionTick1.setSecurity(option);
        Tick optionTick2 = new TickImpl();
        optionTick2.setId(2);
        optionTick2.setSecurity(option);
        Tick optionTick3 = new TickImpl();
        optionTick3.setId(3);
        optionTick3.setSecurity(option);
        Tick optionTick4 = new TickImpl();
        optionTick4.setId(4);
        optionTick4.setSecurity(option);

        ruleService.sendEvent("SMI", indexTick1);
        ruleService.sendEvent("SMI", volaTick1);
        ruleService.sendEvent("SMI", optionTick1);
        ruleService.sendEvent("SMI", optionTick2);

        ruleService.sendEvent("SMI", indexTick2);
        ruleService.sendEvent("SMI", volaTick2);
        ruleService.sendEvent("SMI", optionTick3);
        ruleService.sendEvent("SMI", optionTick4);

    }

    @Override
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
            case 5:
                getSecurityDao().findSubscribedInclFamily();
                break;
            case 6:
                getSecurityDao().findSubscribedForAutoActivateStrategiesInclFamily();
                break;
            case 8:
                getSecurityDao().findByIdInclFamilyAndUnderlying(6);
                break;

            case 9:
                getStockOptionDao().load(6);
                break;
            case 10:
                getStockOptionDao().loadAll();
                break;
            case 11:
                getStockOptionDao().findByMinExpirationAndStrikeLimit(4, new Date(), new BigDecimal(6500), "PUT");
                break;
            case 12:
                getStockOptionDao().findSubscribedStockOptions();
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

            case 36:
                getTickDao().load(370552);
                break;
            case 37:
                getTickDao().findBySecurityDateTypeAndExpiration(new StockImpl(), new Date(), OptionType.PUT, new Date());
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
                getTickDao().findDailyTickIdsBeforeTime(4, new Date(), new Date());
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
                getStockOptionFamilyDao().findByUnderlying(2);
                break;

        }
    }

    @Override
    protected void handleTestSabr() throws Exception {

        String dateString = "07.12.2010 09:13:00";
        int underlyingId = 4;
        int volaId = 5;
        int stockOptionId = 717;

        Date date = (new SimpleDateFormat("dd.MM.yyyy kk:mm:ss")).parse(dateString);

        EventService ruleService = ServiceLocator.instance().getService("ruleService", EventService.class);
        ruleService.initServiceProvider(StrategyImpl.BASE);
        ruleService.sendEvent(StrategyImpl.BASE, new CurrentTimeEvent(date.getTime()));

        double underlyingValue = getTickDao().findByDateAndSecurity(date, underlyingId).getCurrentValueDouble();
        double volaValue = getTickDao().findByDateAndSecurity(date, volaId).getCurrentValueDouble() / 100.0;

        StockOption stockOption = getStockOptionDao().load(stockOptionId);

        double optionPrice = StockOptionUtil.getOptionPriceSabr(stockOption, underlyingValue, volaValue);

        System.out.println(optionPrice);
    }
}
