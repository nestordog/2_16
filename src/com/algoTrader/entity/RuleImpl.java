package com.algoTrader.entity;

public class RuleImpl extends Rule {

    private static final long serialVersionUID = -8481295095357769646L;

    public boolean isPattern() {

        if (getDefinition().startsWith("every")) {
            return true;
        } else if (getDefinition().startsWith("select") || (getDefinition().startsWith("insert"))) {
            return false;
        } else {
            throw new IllegalArgumentException("unknown rule definition");
        }
    }

    public boolean isPrepared() {

        return getDefinition().contains("?");
    }

    public String getPrioritisedDefinition() {

        if (getPriority() != 0) {
            return "@Priority(" + getPriority() + ")\n" + getDefinition();
        } else {
            return getDefinition();
        }
    }
}
