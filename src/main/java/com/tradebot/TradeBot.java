package com.tradebot;

import com.binance.client.SyncRequestClient;
import com.futures.dualside.RequestSender;
import com.log.TradeLogger;
import com.server.WebhookReceiver;
import com.strategies.StrategyHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tgbot.TelegramTradeBot;
import com.utils.I18nSupport;
import com.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TradeBot implements HttpHandler {

    public final Map<String, StrategyHandler> enabledStrategies;

    public TradeBot(String webhookRequestContext,
                    String binanceApiKey,
                    String binanceSecretKey,
                    String tgBotToken,
                    String tgBotUsername,
                    long tgBotCreatorId) throws TelegramApiException, GeneralSecurityException, IOException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(new TelegramTradeBot(tgBotToken,
                tgBotUsername,
                tgBotCreatorId,
                new RequestSender(SyncRequestClient.create(binanceApiKey, binanceSecretKey)),
                this));
        enabledStrategies = new ConcurrentHashMap<>();

        WebhookReceiver.start("/" + webhookRequestContext, this);
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        process(Utils.readAllFromInputStream(httpExchange.getRequestBody()));
        Utils.answerOkToHttpsRequest(httpExchange);
    }

    public void process(String inputSignal) {
        try {
            JSONObject inputRequest = new JSONObject(inputSignal);

            StrategyHandler strategyHandler = enabledStrategies.get(inputRequest.getString("ticker"));

            if (strategyHandler != null) {
                strategyHandler.process(inputRequest);
            }
        } catch (JSONException|IllegalArgumentException exception) {
            TradeLogger.logException(exception);
        }
    }
}
