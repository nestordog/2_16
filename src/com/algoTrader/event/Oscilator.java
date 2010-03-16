package com.algoTrader.event;

import com.algoTrader.entity.Entity;

public class Oscilator extends Entity {

    private Double value;

    public Oscilator() {
    }

    public Oscilator(Double value) {
        this.value = value;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
