package com.algoTrader.entity.trade;

import java.text.SimpleDateFormat;

public class FillImpl extends Fill {

    private static final long serialVersionUID = 1619681349145226990L;
    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");

    @Override
    public String toString() {

        //@formatter:off
        return format.format(getExtDateTime())
            + " " + getSide()
            + " " + getQuantity()
            + (getOrd() != null ? " " + getOrd().getSecurity() : "")
            + (getOrd() != null ? " " + getOrd().getStrategy() : "")
            + " price: " + getPrice()
            + (getOrd() != null ? " " + getOrd().getSecurity().getSecurityFamily().getCurrency() : "")
            + " extId: " + getExtId();
        //@formatter:on
    }

    @Override
    /**
     * make sure this is also associated with the order (in case a Fill is created from an esper statement)
     */
    public void setOrd(Order order) {

        super.setOrd(order);
        order.getFills().add(this);
    }
}
