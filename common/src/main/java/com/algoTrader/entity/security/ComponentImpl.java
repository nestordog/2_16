package com.algoTrader.entity.security;

import com.algoTrader.entity.security.Component;

public class ComponentImpl extends Component {

    private static final long serialVersionUID = 8647813109138743881L;

    @Override
    public String toString() {

        return getSecurity().getSymbol();
    }
}
