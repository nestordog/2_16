package com.algoTrader.service.fix;

import java.text.SimpleDateFormat;
import java.util.Date;

import quickfix.field.CFICode;
import quickfix.field.ClOrdID;
import quickfix.field.ContractMultiplier;
import quickfix.field.Currency;
import quickfix.field.MaturityDate;
import quickfix.field.MaturityMonthYear;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.SecurityType;
import quickfix.field.StopPx;
import quickfix.field.StrikePrice;
import quickfix.field.TransactTime;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import com.algoTrader.entity.security.Forex;
import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.Stock;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.entity.trade.LimitOrderI;
import com.algoTrader.entity.trade.SimpleOrder;
import com.algoTrader.entity.trade.StopOrderI;
import com.algoTrader.enumeration.OptionType;

public abstract class Fix44OrderServiceImpl extends Fix44OrderServiceBase {

    private static final long serialVersionUID = -3694423160435186473L;

    private static SimpleDateFormat monthFormat = new SimpleDateFormat("yyyyMM");
    private static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");

    @Override
    protected void handleValidateOrder(SimpleOrder order) throws Exception {
        // to be implememented
    }

    @Override
    protected void handleSendOrder(SimpleOrder order) throws Exception {

        Security security = order.getSecurityInitialized();

        // use system time for orderNumber
        order.setIntId(getFixClient().getNextOrderId(getMarketChannel()));

        NewOrderSingle newOrder = new NewOrderSingle();

        // common info
        newOrder.set(new TransactTime(new Date()));
        newOrder.set(new ClOrdID(String.valueOf(order.getIntId())));

        newOrder.set(FixUtil.getFixSymbol(security));
        newOrder.set(FixUtil.getFixSide(order.getSide()));
        newOrder.set(new OrderQty(order.getQuantity()));
        newOrder.set(FixUtil.getFixOrderType(order));

        // populate security information
        if (security instanceof StockOption) {

            StockOption stockOption = (StockOption) security;

            newOrder.set(new SecurityType(SecurityType.OPTION));
            newOrder.set(new Currency(stockOption.getSecurityFamily().getCurrency().toString()));
            newOrder.set(new CFICode("O" + (OptionType.PUT.equals(stockOption.getType()) ? "P" : "C")));
            newOrder.set(new StrikePrice(stockOption.getStrike().doubleValue()));
            newOrder.set(new ContractMultiplier(stockOption.getSecurityFamily().getContractSize()));
            newOrder.set(new MaturityMonthYear(monthFormat.format(stockOption.getExpiration())));
            newOrder.set(new MaturityDate(dayFormat.format(stockOption.getExpiration())));

        } else if (security instanceof Future) {

            Future future = (Future) security;

            newOrder.set(new SecurityType(SecurityType.FUTURE));
            newOrder.set(new Currency(future.getSecurityFamily().getCurrency().toString()));
            newOrder.set(new MaturityMonthYear(monthFormat.format(future.getExpiration())));
            newOrder.set(new MaturityDate(dayFormat.format(future.getExpiration())));

        } else if (security instanceof Forex) {

            String[] currencies = security.getSymbol().split("\\.");
            newOrder.set(new SecurityType(SecurityType.CASH));
            newOrder.set(new Currency(currencies[1]));

        } else if (security instanceof Stock) {

            Stock stock = (Stock) security;

            newOrder.set(new SecurityType(SecurityType.COMMON_STOCK));
            newOrder.set(new Currency(stock.getSecurityFamily().getCurrency().toString()));
        }

        //set the limit price if order is a limit order or stop limit order
        if (order instanceof LimitOrderI) {
            newOrder.set(new Price(((LimitOrderI) order).getLimit().doubleValue()));
        }

        //set the stop price if order is a stop order or stop limit order
        if (order instanceof StopOrderI) {
            newOrder.set(new StopPx(((StopOrderI) order).getStop().doubleValue()));
        }

        // broker-specific settings
        sendOrder(order, newOrder);

        // send the message
        sendAndPropagateMessage(order, newOrder);
    }

    @Override
    protected void handleModifyOrder(SimpleOrder order) throws Exception {

        Security security = order.getSecurityInitialized();

        // assign a new order number
        long origClOrdID = order.getIntId();
        order.setIntId(getFixClient().getNextOrderId(getMarketChannel()));

        OrderCancelReplaceRequest replaceRequest = new OrderCancelReplaceRequest();

        // common info
        replaceRequest.set(new ClOrdID(String.valueOf(order.getIntId())));
        replaceRequest.set(new OrigClOrdID(String.valueOf(origClOrdID)));

        replaceRequest.set(FixUtil.getFixSymbol(security));
        replaceRequest.set(FixUtil.getFixSide(order.getSide()));
        replaceRequest.set(new OrderQty(order.getQuantity()));
        replaceRequest.set(FixUtil.getFixOrderType(order));

        // populate security information
        if (security instanceof StockOption) {

            StockOption stockOption = (StockOption) security;

            replaceRequest.set(new SecurityType(SecurityType.OPTION));
            replaceRequest.set(new CFICode("O" + (OptionType.PUT.equals(stockOption.getType()) ? "P" : "C")));
            replaceRequest.set(new StrikePrice(stockOption.getStrike().doubleValue()));
            replaceRequest.set(new MaturityMonthYear(monthFormat.format(stockOption.getExpiration())));

        } else if (security instanceof Future) {

            Future future = (Future) security;

            replaceRequest.set(new SecurityType(SecurityType.FUTURE));
            replaceRequest.set(new MaturityMonthYear(monthFormat.format(future.getExpiration())));

        } else if (security instanceof Forex) {

            replaceRequest.set(new SecurityType(SecurityType.CASH));

        } else if (security instanceof Stock) {
            replaceRequest.set(new SecurityType(SecurityType.COMMON_STOCK));
        }

        //set the limit price if order is a limit order or stop limit order
        if (order instanceof LimitOrderI) {
            replaceRequest.set(new Price(((LimitOrderI) order).getLimit().doubleValue()));
        }

        //set the stop price if order is a stop order or stop limit order
        if (order instanceof StopOrderI) {
            replaceRequest.set(new StopPx(((StopOrderI) order).getStop().doubleValue()));
        }

        // broker-specific settings
        modifyOrder(order, replaceRequest);

        // send the message
        sendAndPropagateMessage(order, replaceRequest);
    }

    @Override
    protected void handleCancelOrder(SimpleOrder order) throws Exception {

        Security security = order.getSecurityInitialized();

        // assign a new order number
        long origClOrdID = order.getIntId();
        order.setIntId(getFixClient().getNextOrderId(getMarketChannel()));

        OrderCancelRequest cancelRequest = new OrderCancelRequest();

        // common info
        cancelRequest.set(new ClOrdID(String.valueOf(order.getIntId())));
        cancelRequest.set(new OrigClOrdID(String.valueOf(origClOrdID)));

        cancelRequest.set(FixUtil.getFixSymbol(security));
        cancelRequest.set(FixUtil.getFixSide(order.getSide()));
        cancelRequest.set(new OrderQty(order.getQuantity()));

        // populate security information
        if (security instanceof StockOption) {

            StockOption stockOption = (StockOption) security;

            cancelRequest.set(new SecurityType(SecurityType.OPTION));
            cancelRequest.set(new CFICode()); // todo
            cancelRequest.set(new StrikePrice(stockOption.getStrike().doubleValue()));
            cancelRequest.set(new MaturityMonthYear(monthFormat.format(stockOption.getExpiration())));

        } else if (security instanceof Future) {

            Future future = (Future) security;

            cancelRequest.set(new SecurityType(SecurityType.FUTURE));
            cancelRequest.set(new MaturityMonthYear(monthFormat.format(future.getExpiration())));

        } else if (security instanceof Forex) {

            cancelRequest.set(new SecurityType(SecurityType.CASH));

        } else if (security instanceof Stock) {
            cancelRequest.set(new SecurityType(SecurityType.COMMON_STOCK));
        }

        // broker-specific settings
        cancelOrder(order, cancelRequest);

        // send the message
        sendAndPropagateMessage(order, cancelRequest);
    }
}
