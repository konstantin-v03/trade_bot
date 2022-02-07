package com;

import com.binance.client.RequestOptions;
import com.binance.client.SubscriptionClient;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;
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
import java.util.TimeZone;

public class Main {
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        System.getProperties().putAll(readPropertiesFile(args[0]));

        RequestOptions options = new RequestOptions();

        RequestSender requestSender = new RequestSender(SyncRequestClient.create(System.getProperty("apikey"), System.getProperty("secretkey"), options));

        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new TradeBot(System.getProperty("bottoken"),
                    System.getProperty("botusername"),
                    Long.parseLong(System.getProperty("creatorid")),
                    requestSender));
        } catch (TelegramApiException ignored) {

        }

        WebhookReceiver.start("/" + System.getProperty("context"), new MFI_BigGuyHandler(requestSender));
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
