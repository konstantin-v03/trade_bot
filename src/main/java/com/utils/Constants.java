package com.utils;

import com.futures.Amount;
import com.signal.Trend;

public class Constants {
    public static final int INTERVAL_5m = 5;
    public static final int INTERVAL_1h = 60;
    public static final int INTERVAL_4h = 4 * 60;

    public static final String INTERVAL_1H_STR = "1h";
    public static final String INTERVAL_4H_STR = "4h";

    public static final PropertySE TAKE_PROFIT = new PropertySE("takeProfit", Integer.class);
    public static final PropertySE STOP_LOSS = new PropertySE("stopLoss", Integer.class);
    public static final PropertySE AMOUNT = new PropertySE("amount", Amount.class);
    public static final PropertySE LEVERAGE = new PropertySE("leverage", Integer.class);
    public static final PropertySE LOG_CHAT_IDS = new PropertySE("logChatIds", Integer[].class);
    public static final PropertySE SCHEDULER = new PropertySE("scheduler", Boolean.class);
    public static final PropertySE DEBUG_MODE = new PropertySE("debugMode", Boolean.class);
    public static final PropertySE TEST_MODE = new PropertySE("testMode", Boolean.class);
    public static final PropertySE IS_LOG_ONCE_PER_MINUTE = new PropertySE("isLogOncePerMinute", Boolean.class);

    public static final PropertySE TOKEN_SYMBOL = new PropertySE("tokenSymbol", String.class);
    public static final PropertySE CONTRACT_ADDRESS = new PropertySE("contractAddress", String.class);
    public static final PropertySE ADDRESS = new PropertySE("address", String.class);
    public static final PropertySE API_KEY = new PropertySE("apiKey", String.class);
    public static final PropertySE DECIMALS = new PropertySE("decimals", Integer.class);
    public static final PropertySE ALIAS = new PropertySE("alias", String.class);

    public static final PropertySE TREND = new PropertySE("trend", Trend.class);

    public static final PropertySE YELLOW_OPEN_LONG_PERCENTAGE = new PropertySE("yellowOpenLongPercentage", Integer.class);
    public static final PropertySE GREEN_OPEN_LONG_PERCENTAGE = new PropertySE("greenOpenLongPercentage", Integer.class);
    public static final PropertySE ORANGE_CLOSE_LONG_PERCENTAGE = new PropertySE("orangeCloseLongPercentage", Integer.class);
    public static final PropertySE RED_CLOSE_LONG_PERCENTAGE = new PropertySE("redCloseLongPercentage", Integer.class);

    public static final PropertySE BLACK_OPEN_SHORT_PERCENTAGE = new PropertySE("blackOpenShortPercentage", Integer.class);
    public static final PropertySE ORANGE_OPEN_SHORT_PERCENTAGE = new PropertySE("orangeOpenShortPercentage", Integer.class);
    public static final PropertySE YELLOW_CLOSE_SHORT_PERCENTAGE = new PropertySE("yellowCloseShortPercentage", Integer.class);
    public static final PropertySE GREEN_CLOSE_SHORT_PERCENTAGE = new PropertySE("greenCloseShortPercentage", Integer.class);

    public static final String NULL = "null";
}