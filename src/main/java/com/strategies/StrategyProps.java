package com.strategies;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

public class StrategyProps {
    private final Strategy strategy;
    private final String ticker;
    private final boolean debugMode;
    private final Properties properties;

    public StrategyProps(Strategy strategy, String ticker,boolean debugMode, String propertiesString) {
        this.strategy = strategy;
        this.ticker = ticker;
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

    public boolean isDebugMode() {
        return debugMode;
    }

    public Properties getProperties() {
        return properties;
    }
}
