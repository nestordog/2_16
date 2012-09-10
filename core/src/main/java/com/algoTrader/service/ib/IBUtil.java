package com.algoTrader.service.ib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import com.algoTrader.entity.security.Forex;
import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.NaturalIndex;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.Stock;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.entity.trade.LimitOrder;
import com.algoTrader.entity.trade.MarketOrder;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.entity.trade.StopOrder;
import com.algoTrader.enumeration.Market;
import com.algoTrader.enumeration.Side;
import com.algoTrader.enumeration.Status;
import com.ib.client.Contract;
import com.ib.client.Execution;

public class IBUtil {

    private static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat monthFormat = new SimpleDateFormat("yyyyMM");
    private static SimpleDateFormat executionFormat = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");

    public static Contract getContract(Security security) {

        Contract contract = new Contract();

        if (security instanceof StockOption) {

            StockOption stockOption = (StockOption) security;

            contract.m_symbol = stockOption.getSecurityFamily().getBaseSymbol();
            contract.m_secType = "OPT";
            contract.m_exchange = "SMART";
            contract.m_primaryExch = IBMarketConverter.marketToString(stockOption.getSecurityFamily().getMarket());
            contract.m_currency = stockOption.getSecurityFamily().getCurrency().toString();
            contract.m_strike = stockOption.getStrike().doubleValue();
            contract.m_right = stockOption.getType().toString();
            contract.m_multiplier = String.valueOf(stockOption.getSecurityFamily().getContractSize());

            if (security.getSecurityFamily().getMarket().equals(Market.CBOE) || security.getSecurityFamily().getMarket().equals(Market.SOFFEX)) {
                // IB expiration is one day before effective expiration for CBOE and SOFFEX options
                contract.m_expiry = dayFormat.format(DateUtils.addDays(stockOption.getExpiration(), -1));
            } else {
                contract.m_expiry = dayFormat.format(stockOption.getExpiration());
            }
        } else if (security instanceof Future) {

            Future future = (Future) security;

            contract.m_symbol = future.getSecurityFamily().getBaseSymbol();
            contract.m_secType = "FUT";
            contract.m_exchange = IBMarketConverter.marketToString(future.getSecurityFamily().getMarket());
            contract.m_currency = future.getSecurityFamily().getCurrency().toString();
            contract.m_expiry = monthFormat.format(future.getExpiration());

        } else if (security instanceof Forex) {

            String[] currencies = security.getSymbol().split("\\.");

            contract.m_symbol = currencies[0];
            contract.m_secType = "CASH";
            contract.m_exchange = IBMarketConverter.marketToString(security.getSecurityFamily().getMarket());
            contract.m_currency = currencies[1];

        } else if (security instanceof Stock) {

            contract.m_currency = security.getSecurityFamily().getCurrency().toString();
            contract.m_symbol = security.getSymbol();
            contract.m_secType = "STK";
            contract.m_exchange = "SMART";
            contract.m_primaryExch = IBMarketConverter.marketToString(security.getSecurityFamily().getMarket());

        } else if (security instanceof NaturalIndex) {

            contract.m_currency = security.getSecurityFamily().getCurrency().toString();
            contract.m_symbol = security.getSymbol();
            contract.m_secType = "IND";
            contract.m_exchange = IBMarketConverter.marketToString(security.getSecurityFamily().getMarket());

        }

        return contract;
    }

    public static String getIBOrderType(Order order) {

        if (order instanceof MarketOrder) {
            return "MKT";
        } else if (order instanceof LimitOrder) {
            return "LMT";
        } else if (order instanceof StopOrder) {
            return "STP";
        } else {
            throw new IllegalArgumentException("unsupported order type " + order.getClass().getName());
        }
    }

    public static Date getExecutionDateTime(Execution execution) {

        try {
            return executionFormat.parse(execution.m_time);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date getLastDateTime(String input) {

        return new Date(Long.parseLong(input + "000"));
    }

    public static Side getSide(Execution execution) {

        String sideString = execution.m_side;
        if ("BOT".equals(sideString)) {
            return Side.BUY;
        } else if ("SLD".equals(sideString)) {
            return Side.SELL;
        } else {
            throw new IllegalArgumentException("unknow side " + sideString);
        }
    }

    public static Status getStatus(String status, int filled) {

        if ("Submitted".equals(status) ||
                "PreSubmitted".equals(status) ||
                "PendingSubmit".equals(status) ||
                "PendingCancel".equals(status)) {
            if (filled == 0) {
                return Status.SUBMITTED;
            } else {
                return Status.PARTIALLY_EXECUTED;
            }
        } else if ("Filled".equals(status)) {
            return Status.EXECUTED;
        } else if ("ApiCancelled".equals(status) ||
                "Cancelled".equals(status) ||
                "Inactive".equals(status)) {
            return Status.CANCELED;
        } else {
            throw new IllegalArgumentException("unknown orderStatus " + status);
        }
    }
}
