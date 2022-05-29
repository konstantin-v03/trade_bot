package com.strategies;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.utils.Constants.LOG_CHAT_IDS_STR;

public class StrategyProps implements Serializable {
    private static final long serialVersionUID = 835489771410264765L;

    private final Strategy strategy;
    private final List<String> tickers;
    private final Map<String, String> properties;
    private final List<Long> logChatIds;

    public StrategyProps(Strategy strategy, List<String> tickers, String propertiesString, long chatId) {
        this.strategy = strategy;
        this.tickers = Collections.unmodifiableList(tickers == null ? new ArrayList<>() : tickers);

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
                .stream(Optional.ofNullable(properties.get(LOG_CHAT_IDS_STR))
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
