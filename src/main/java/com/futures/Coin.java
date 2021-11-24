package com.futures;

public class Coin {
    public final String symbol;

    private final Amount amount;
    private final int leverage;

    public Coin(String symbol, Amount amount, int leverage) {
        this.symbol = symbol;
        this.amount = amount;
        this.leverage = leverage;
    }

    public Amount getAmount() {
        return amount;
    }

    public int getLeverage() {
        return leverage;
    }

    @Override
    public String toString() {
        return "Coin{" +
                "symbol='" + symbol + '\'' +
                ", amount=" + amount +
                ", leverage=" + leverage +
                '}';
    }
}
