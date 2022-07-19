package com.utils;

public class PropertySE {
    private final String key;
    private final Class<?> valueType;

    public PropertySE(String key, Class<?> valueType) {
        this.key = key;
        this.valueType = valueType;
    }

    public String getKey() {
        return key;
    }

    public Class<?> getValueType() {
        return valueType;
    }
}
