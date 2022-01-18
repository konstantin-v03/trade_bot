package com.futures;

import org.jetbrains.annotations.NonNls;

public class Coin {
    private final String ticket;

    private final Amount amount;
    private final int leverage;

    public Coin(String ticket, Amount amount, int leverage) {
        this.ticket = ticket;
        this.amount = amount;
        this.leverage = leverage;
    }

    public String getTicket() {
        return ticket;
    }

    public Amount getAmount() {
        return amount;
    }

    public int getLeverage() {
        return leverage;
    }

    @NonNls
    @Override
    public String toString() {
        return "Coin{" +
                "ticket='" + ticket + '\'' +
                ", amount=" + amount +
                ", leverage=" + leverage +
                '}';
    }
}
