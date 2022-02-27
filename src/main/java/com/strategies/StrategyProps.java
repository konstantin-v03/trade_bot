package com.strategies;

import com.futures.Amount;

public class StrategyProps {
    private final Strategy strategy;
    private final String ticker;
    private final Amount amount;
    private final int leverage;
    private final int interval;
    private final int takeProfit;
    private final int stopLoss;
    private final boolean debugMode;

    public StrategyProps(Strategy strategy, String ticker, Amount amount, int leverage, int interval, int takeProfit, int stopLoss, boolean debugMode) {
        this.strategy = strategy;
        this.ticker = ticker;
        this.amount = amount;
        this.leverage = leverage;
        this.interval = interval;
        this.takeProfit = takeProfit;
        this.stopLoss = stopLoss;
        this.debugMode = debugMode;
    }

    public Strategy getStrategy() {
        return strategy;
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

    public boolean isDebugMode() {
        return debugMode;
    }
}
