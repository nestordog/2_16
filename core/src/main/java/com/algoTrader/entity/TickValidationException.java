package com.algoTrader.entity;

public class TickValidationException extends RuntimeException {

    private static final long serialVersionUID = 1728320729148701692L;

    public TickValidationException(String string) {
        super(string);
    }
}
