package com.futures;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalVariables {
    public static final Map<String, Coin> enabledCoins = new ConcurrentHashMap<>();
}
