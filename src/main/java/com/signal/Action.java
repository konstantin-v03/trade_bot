package com.signal;

public enum Action {
    BUY("ПОКУПКА"),
    SELL("ПРОДАЖА");

    private final String alias;

    Action(String alias) {
        this.alias = alias;
    }

    public String alias() {
        return alias;
    }
}
