package com.algoTrader.entity;

public class RuleImpl extends com.algoTrader.entity.Rule {

    private static final long serialVersionUID = -8481295095357769646L;

    public String getPrioritisedDefinition() {

        if (getPriority() != 0) {
            return "@Priority(" + getPriority() + ")\n" + getDefinition();
        } else {
            return getDefinition();
        }
    }
}
