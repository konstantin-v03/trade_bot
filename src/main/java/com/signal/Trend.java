package com.signal;

public enum Trend {
    BULLISH, BEARISH;

    public boolean isBullish() {
        return equals(Trend.BULLISH);
    }

    public boolean isBearish() {
        return equals(Trend.BEARISH);
    }
}
