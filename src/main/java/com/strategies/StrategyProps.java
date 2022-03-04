package com.strategies;

import com.futures.Amount;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

public class StrategyProps {
    private final Strategy strategy;
    private final String ticker;
    private final Amount amount;
    private final int leverage;
    private final int takeProfit;
    private final int stopLoss;
    private final boolean debugMode;
    private final Properties properties;

    public StrategyProps(Strategy strategy, String ticker, Amount amount, int leverage, int takeProfit, int stopLoss, boolean debugMode, String propertiesString) {
        this.strategy = strategy;
        this.ticker = ticker;
        this.amount = amount;
        this.leverage = leverage;
        this.takeProfit = takeProfit;
        this.stopLoss = stopLoss;
        this.debugMode = debugMode;

        Properties properties = null;

        try {
            for (String[] keyValue : Arrays.stream(propertiesString.split(","))
                    .map(str -> str.split("="))
                    .collect(Collectors.toList())) {
                if (properties == null) {
                    properties = new Properties();
                }

                properties.put(keyValue[0], keyValue[1]);
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
            properties = null;
        }

        this.properties = properties;
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

    public int getTakeProfit() {
        return takeProfit;
    }

    public int getStopLoss() {
        return stopLoss;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public Properties getProperties() {
        return properties;
    }
}
