package com.strategies;

import com.utils.Constants;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class StrategyProps implements Serializable {
    private static final long serialVersionUID = 835489771410264765L;

    private final Strategy strategy;
    private final List<String> tickers;
    private final List<String> blacklistTickers;
    private final Map<String, String> properties;
    private final List<Long> logChatIds;

    public StrategyProps(Strategy strategy, List<String> tickers, List<String> blacklistTickers, String propertiesString, long chatId) {
        this.strategy = strategy;
        this.tickers = Collections.unmodifiableList(tickers == null ? new ArrayList<>() : tickers);
        this.blacklistTickers = Collections.unmodifiableList(blacklistTickers == null ? new ArrayList<>() : blacklistTickers);

        Map<String, String> properties = new HashMap<>();

        if (propertiesString != null) {
            Arrays.stream(propertiesString.split(";"))
                    .map(str -> str.split("="))
                    .forEach(keyValue -> {
                        if (keyValue.length == 2) {
                            properties.put(keyValue[0], keyValue[1]);
                        }
                    });

            if (properties.size() == 0) {
                throw new IllegalArgumentException("Illegal properties!");
            }
        }

        this.properties = Collections.unmodifiableMap(properties);

        logChatIds = Arrays
                .stream(Optional.ofNullable(properties.get(Constants.LOG_CHAT_IDS.getKey()))
                        .orElse(String.valueOf(chatId))
                        .split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public List<String> getTickers() {
        return tickers;
    }

    public List<String> getBlacklistTickers() {
        return blacklistTickers;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public List<Long> getLogChatIds() {
        return logChatIds;
    }

    public void addLogChatId(long logChatId){
        logChatIds.add(logChatId);
    }

    public void setLogChatId(List<Long> logChatIds) {
        this.logChatIds.clear();
        this.logChatIds.addAll(logChatIds);
    }
}
