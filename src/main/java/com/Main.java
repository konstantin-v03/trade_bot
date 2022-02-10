package com;

import com.tradebot.TradeBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.TimeZone;

import static com.utils.Utils.readPropertiesFile;

public class Main {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Properties properties = readPropertiesFile(args[0]);

        try {
            new TradeBot(properties.getProperty("context"),
                    properties.getProperty("apikey"),
                    properties.getProperty("secretkey"),
                    properties.getProperty("bottoken"),
                    properties.getProperty("botusername"),
                    Long.parseLong(properties.getProperty("creatorid")));
        } catch (TelegramApiException|IllegalArgumentException|GeneralSecurityException|IOException exception) {
            exception.printStackTrace();
        }
    }
}
