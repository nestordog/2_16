package com.algoTrader.entity.trade;

import java.text.SimpleDateFormat;

public class FillImpl extends Fill {

    private static final long serialVersionUID = 1619681349145226990L;
    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");

    @Override
    public String toString() {

        //@formatter:off
        return format.format(getDateTime()) + " " +
            getSide() + " " +
            getQuantity() + " " +
            getParentOrder().getSecurity().getSymbol() +
            " price " + getPrice() + " " + getParentOrder().getSecurity().getSecurityFamily().getCurrency() +
            " extId " + getExtId();
        //@formatter:on
    }
}
