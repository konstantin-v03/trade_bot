package com.strategies;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class StrategyProps implements Serializable {
    private static final long serialVersionUID = 835489771410264765L;

    private final Strategy strategy;
    private final String ticker;
    private final boolean debugMode;
    private final Properties properties;
    private List<Long> logChatIds;

    public StrategyProps(Strategy strategy, String ticker, boolean debugMode, String propertiesString, List<Long> logChatIds) {
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
        this.logChatIds = logChatIds;
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

    public List<Long> getLogChatIds() {
        return logChatIds;
    }

    public void addLogChatId(long logChatId){
        logChatIds.add(logChatId);
    }

    public void setLogChatId(List<Long> logChatIds) {
        this.logChatIds = logChatIds;
    }
}
