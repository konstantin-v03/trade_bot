package com.strategies;

import com.futures.Amount;

public class StrategyProps {
    private final String ticker;
    private final Amount amount;
    private final int leverage;
    private final int interval;
    private final int takeProfit;
    private final int stopLoss;

    public StrategyProps(String ticker, Amount amount, int leverage, int interval, int takeProfit, int stopLoss) {
        this.ticker = ticker;
        this.amount = amount;
        this.leverage = leverage;
        this.interval = interval;
        this.takeProfit = takeProfit;
        this.stopLoss = stopLoss;
    }

    public String getTicker() {
        return ticker;
    }

    public Amount getAmount() {
        return amount;
    }

    public int getLeverage() {
        return leverage;
    }

    public int getInterval() {
        return interval;
    }

    public int getTakeProfit() {
        return takeProfit;
    }

    public int getStopLoss() {
        return stopLoss;
    }
}
