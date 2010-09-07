package com.algoTrader.service.ib;

import java.text.SimpleDateFormat;

import org.apache.commons.lang.time.DateUtils;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.ib.client.Contract;

public class IbUtil {

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    public static Contract getContract(Security security) {

        Contract contract = new Contract();

        if (security instanceof StockOption) {

            StockOption stockOption = (StockOption) security;

            contract.m_symbol = stockOption.getUnderlaying().getSymbol();
            contract.m_secType = "OPT";
            contract.m_exchange = IbMarketConverter.marketToString(stockOption.getMarket());
            contract.m_currency = stockOption.getCurrency().getValue();
            contract.m_expiry = format.format(DateUtils.addDays(stockOption.getExpiration(), -1));
            // IB expiration is one day before effective expiration
            contract.m_strike = stockOption.getStrike().intValue();
            contract.m_right = stockOption.getType().getValue();
            contract.m_multiplier = String.valueOf(stockOption.getContractSize());
        } else {

            contract.m_symbol = security.getSymbol();
            contract.m_secType = "IND";
            contract.m_exchange = IbMarketConverter.marketToString(security.getMarket());
            contract.m_currency = security.getCurrency().getValue();
        }

        return contract;
    }
}
