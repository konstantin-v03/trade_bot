package com;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.futures.dualside.RequestSender;
import com.server.WebhookReceiver;
import com.strategies.MFI_BigGuyHandler;
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

        RequestOptions options = new RequestOptions();

        RequestSender requestSender = new RequestSender(SyncRequestClient.create(properties.getProperty("apikey"), properties.getProperty("secretkey"), options));

        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new TradeBot(properties.getProperty("bottoken"),
                    properties.getProperty("botusername"),
                    Long.parseLong(properties.getProperty("creatorid")),
                    requestSender));
        } catch (TelegramApiException ignored) {

        }

        WebhookReceiver.start("/" + properties.getProperty("context"), new MFI_BigGuyHandler(requestSender));
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
