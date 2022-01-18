package com.utils;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class I18nSupport {
    @NonNls
    private static final ResourceBundle defaultBundle =
            ResourceBundle.getBundle("literals", new UTF8Control());


    public static String i18n_literals(@PropertyKey(resourceBundle = "literals") String key, Object... params) {
        String value = defaultBundle.getString(key);

        if (params.length > 0) {
            return MessageFormat.format(value, params);
        }

        return value;
    }

}