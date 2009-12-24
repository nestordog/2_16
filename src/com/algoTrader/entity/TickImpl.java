package com.algoTrader.entity;

public class TickImpl extends Tick {

    private static final long serialVersionUID = -5959287168728366157L;

    public String toString() {

        StringBuffer buffer = new StringBuffer();

        buffer.append("DateTime=");
        buffer.append(getDateTime());
        buffer.append(", Last=");
        buffer.append(getLast());
        buffer.append(", LastDateTime=");
        buffer.append(getLastDateTime());
        buffer.append(", VolBid=");
        buffer.append(getVolBid());
        buffer.append(", VolAsk=");
        buffer.append(getVolAsk());
        buffer.append(", Bid=");
        buffer.append(getBid());
        buffer.append(" ,Ask=");
        buffer.append(getAsk());
        buffer.append(", Vol=");
        buffer.append(getVol());
        buffer.append(", OpenIntrest=");
        buffer.append(getOpenIntrest());
        buffer.append(", Settlement=");
        buffer.append(getSettlement());

        if (getSecurity() != null) {
            buffer.append(", SecurityId=");
            buffer.append(getSecurity().getId());
        }

        return buffer.toString();

       }
}
