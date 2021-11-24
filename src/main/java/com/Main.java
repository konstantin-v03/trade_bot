package com;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.futures.dualside.RequestSender;
import com.server.WebhookHandler;
import com.server.WebhookReceiver;
import com.tgbot.TradeBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        Properties properties = readPropertiesFile(args[0]);

        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new TradeBot(properties.getProperty("bottoken"),
                    properties.getProperty("botusername"),
                    Long.parseLong(properties.getProperty("creatorid"))));
        } catch (TelegramApiException ignored) {

        }

        RequestOptions options = new RequestOptions();

        SyncRequestClient syncRequestClient = SyncRequestClient.create(properties.getProperty("apikey"), properties.getProperty("secretkey"),
                options);

        WebhookReceiver.start("/" + properties.getProperty("context"), new WebhookHandler(new RequestSender(syncRequestClient)));
    }

    public static Properties readPropertiesFile(String fileName) {
        Properties properties = null;

        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            properties = new Properties();
            properties.load(fileInputStream);
        } catch (IOException ignored) {

        }

        return properties;
    }
}
