package com.algoTrader.service.ib;

import java.text.SimpleDateFormat;

import org.apache.commons.lang.time.DateUtils;

import com.algoTrader.entity.security.Forex;
import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.enumeration.Market;
import com.ib.client.Contract;

public class IbUtil {

    private static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat monthFormat = new SimpleDateFormat("yyyyMM");

    public static Contract getContract(Security security) {

        Contract contract = new Contract();

        if (security instanceof StockOption) {

            StockOption stockOption = (StockOption) security;

            contract.m_symbol = stockOption.getUnderlaying().getSymbol();
            contract.m_secType = "OPT";
            contract.m_exchange = IbMarketConverter.marketToString(stockOption.getSecurityFamily().getMarket());
            contract.m_currency = stockOption.getSecurityFamily().getCurrency().toString();
            contract.m_strike = stockOption.getStrike().intValue();
            contract.m_right = stockOption.getType().toString();
            contract.m_multiplier = String.valueOf(stockOption.getSecurityFamily().getContractSize());

            if (security.getSecurityFamily().getMarket().equals(Market.SOFFEX)) {
                // IB expiration is one day before effective expiration for SOFFEX options
                contract.m_expiry = dayFormat.format(DateUtils.addDays(stockOption.getExpiration(), -1));
            } else {
                contract.m_expiry = dayFormat.format(stockOption.getExpiration());
            }
        } else if (security instanceof Future) {

            Future future = (Future) security;

            contract.m_symbol = future.getUnderlaying().getSymbol();
            contract.m_secType = "FUT";
            contract.m_exchange = IbMarketConverter.marketToString(future.getSecurityFamily().getMarket());
            contract.m_currency = future.getSecurityFamily().getCurrency().toString();
            contract.m_expiry = monthFormat.format(future.getExpiration());

        } else if (security instanceof Forex) {

            String[] currencies = security.getSymbol().split("\\.");

            contract.m_symbol = currencies[0];
            contract.m_secType = "CASH";
            contract.m_exchange = IbMarketConverter.marketToString(security.getSecurityFamily().getMarket());
            contract.m_currency = currencies[1];

        } else {

            contract.m_symbol = security.getSymbol();
            contract.m_secType = "IND";
            contract.m_exchange = IbMarketConverter.marketToString(security.getSecurityFamily().getMarket());
        }

        return contract;
    }
}
