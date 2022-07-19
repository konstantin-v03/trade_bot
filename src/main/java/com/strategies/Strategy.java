package com.strategies;

import com.utils.Constants;
import com.utils.PropertySE;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Strategy {
    MFI_BIG_GUY(
            Stream.of(Constants.AMOUNT, Constants.LEVERAGE, Constants.TAKE_PROFIT, Constants.STOP_LOSS).
                    collect(Collectors.toSet()),
            Stream.of(Constants.LOG_CHAT_IDS, Constants.DEBUG_MODE).collect(Collectors.toSet())),
    ALTCOINS(
            Stream.of(Constants.AMOUNT, Constants.LEVERAGE).
                    collect(Collectors.toSet()),
            Stream.of(Constants.LOG_CHAT_IDS).collect(Collectors.toSet())),
    ALTCOINS_1h_4h(
            Stream.of(Constants.AMOUNT, Constants.LEVERAGE).collect(Collectors.toSet()),
            Stream.of(Constants.LOG_CHAT_IDS, Constants.DEBUG_MODE).collect(Collectors.toSet())),
    ALARM(
            null,
            Stream.of(Constants.LOG_CHAT_IDS, Constants.SCHEDULER, Constants.IS_LOG_ONCE_PER_MINUTE).collect(Collectors.toSet())),
    CHIA_BALANCE_ALARM(
            Stream.of(Constants.ADDRESS, Constants.ALIAS).collect(Collectors.toSet()),
            Stream.of(Constants.LOG_CHAT_IDS).collect(Collectors.toSet())),
    SAFETY(
            Stream.of(Constants.YELLOW_OPEN_LONG_PERCENTAGE,
                            Constants.GREEN_OPEN_LONG_PERCENTAGE,
                            Constants.ORANGE_CLOSE_LONG_PERCENTAGE,
                            Constants.RED_CLOSE_LONG_PERCENTAGE,
                            Constants.BLACK_OPEN_SHORT_PERCENTAGE,
                            Constants.ORANGE_OPEN_SHORT_PERCENTAGE,
                            Constants.YELLOW_CLOSE_SHORT_PERCENTAGE,
                            Constants.GREEN_CLOSE_SHORT_PERCENTAGE,
                            Constants.LEVERAGE, Constants.TREND)
                    .collect(Collectors.toSet()),
            Stream.of(Constants.LOG_CHAT_IDS).collect(Collectors.toSet()));

    private final Set<PropertySE> requiredArguments;
    private final Set<PropertySE> additionalProperties;

    Strategy(Set<PropertySE> requiredArguments, Set<PropertySE> additionalProperties) {
        this.requiredArguments = requiredArguments;
        this.additionalProperties = additionalProperties;
    }

    public Set<PropertySE> getRequiredArguments() {
        return requiredArguments;
    }

    public Set<PropertySE> getAdditionalProperties() {
        return additionalProperties;
    }
}
