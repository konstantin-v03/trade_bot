package com.futures.dualside;

public enum MarginType {
    ISOLATED("ISOLATED"),
    CROSSED("CROSSED");

    private final String type;

    MarginType(String type) {
        this.type = type;
    }

    public boolean isIsolated() {
        return type.equals("ISOLATED");
    }

    public boolean isCrossed() {
        return type.equals("CROSSED");
    }

    @Override
    public String toString() {
        return type;
    }
}
