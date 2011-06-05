package com.algoTrader.entity;

public class WatchListItemImpl extends WatchListItem {

    private static final long serialVersionUID = -5408055861947044393L;

    public String toString() {

        return getStrategy() + " " + getSecurity();
    }
}
