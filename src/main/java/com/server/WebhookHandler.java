package com.server;

import com.binance.client.exception.BinanceApiException;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.MyTrade;
import com.binance.client.model.trade.Position;
import com.futures.Coin;
import com.futures.GlobalVariables;
import com.futures.dualside.MarginType;
import com.futures.dualside.RequestSender;
import com.log.TradeLogger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class WebhookHandler implements HttpHandler {
    private final RequestSender requestSender;

    public WebhookHandler(RequestSender requestSender) {
        this.requestSender = requestSender;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        WebhookRequest webhookRequest = null;
        Coin coin;

        List<String> stringList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringList.add(line);
            }
        }

        try {
            if (stringList.size() > 1) {
                coin = GlobalVariables.enabledCoins.get(stringList.get(0));

                if (coin != null) {
                    webhookRequest = new WebhookRequest(stringList.get(0),
                            PositionSide.valueOf(stringList.get(1)),
                            coin.getAmount(),
                            coin.getLeverage());
                }
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            return;
        }

        MyTrade myTrade = null;
        Long orderId = null;

        Position position = null;

        boolean isOpened = false;

        if (webhookRequest != null) {
            try {
                if (webhookRequest.getPositionSide().equals(PositionSide.LONG)) {
                    orderId = requestSender.closeShortPositionMarket(webhookRequest.getSymbol());
                    isOpened = requestSender.openLongPositionMarket(webhookRequest.getSymbol(), MarginType.ISOLATED, webhookRequest.getAmount(), webhookRequest.getLeverage());
                } else if (webhookRequest.getPositionSide().equals(PositionSide.SHORT)) {
                    orderId = requestSender.closeLongPositionMarket(webhookRequest.getSymbol());
                    isOpened = requestSender.openShortPositionMarket(webhookRequest.getSymbol(), MarginType.ISOLATED, webhookRequest.getAmount(), webhookRequest.getLeverage());
                }
            } catch (BinanceApiException|NullPointerException exception) {
                TradeLogger.logException(exception);
            }
        }

        if (isOpened) {
            position = requestSender.getPosition(webhookRequest.getSymbol(), webhookRequest.getPositionSide());
        }

        if (orderId != null) {
            myTrade = requestSender.getMyTrade(webhookRequest.getSymbol(), orderId);
        }

        if (myTrade != null) {
            TradeLogger.logCloseOrder(myTrade);
        } else {
            TradeLogger.logException(new NullPointerException("MyTrade = null!"));
        }

        if (position != null) {
            TradeLogger.logOpenOrder(position);
        } else {
            TradeLogger.logException(new NullPointerException("Position = null!"));
        }

        String response = "OK";
        HttpsExchange httpsExchange = (HttpsExchange) httpExchange;
        httpsExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        httpsExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = httpsExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
