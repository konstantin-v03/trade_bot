package com.signal;

public enum Action {
    BUY("ПОКУПКА", "\uD83D\uDFE2"),
    SELL("ПРОДАЖА", "\uD83D\uDD34");

    private final String alias;
    private final String emoji;

    Action(String alias, String emoji) {
        this.alias = alias;
        this.emoji = emoji;
    }

    public String alias() {
        return alias;
    }

    public String getEmoji() {
        return emoji;
    }
}
