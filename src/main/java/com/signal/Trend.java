package com.signal;

public enum Trend {
    BULLISH("\uD83D\uDCC8"),
    BEARISH("\uD83D\uDCC9");

    private final String emoji;

    Trend(String emoji) {
        this.emoji = emoji;
    }

    public boolean isBullish() {
        return equals(Trend.BULLISH);
    }

    public boolean isBearish() {
        return equals(Trend.BEARISH);
    }

    public String getEmoji() {
        return emoji;
    }
}
