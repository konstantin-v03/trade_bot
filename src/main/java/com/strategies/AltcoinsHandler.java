package com.strategies;

import com.binance.client.exception.BinanceApiException;
import com.binance.client.model.enums.OrderSide;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.MyTrade;
import com.binance.client.model.trade.Position;
import com.futures.Coin;
import com.futures.GlobalVariables;
import com.futures.dualside.MarginType;
import com.futures.dualside.RequestSender;
import com.log.TradeLogger;
import com.server.WebhookRequest;
import com.signal.PIFAGOR_ALTCOINS_SIGNAL;
import com.signal.Signal;
import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

public class AltcoinsHandler extends StrategyHandler {
    public AltcoinsHandler(RequestSender requestSender) {
        super(requestSender);
    }

    @Override
    public void process() {
        PIFAGOR_ALTCOINS_SIGNAL pifagorAltcoinsSignal;

        try {
            JSONObject jsonObject = new JSONObject(inputRequest);

            if (Signal.getSignalClass(jsonObject) == PIFAGOR_ALTCOINS_SIGNAL.class) {
                pifagorAltcoinsSignal = new PIFAGOR_ALTCOINS_SIGNAL(jsonObject);
            } else {
                throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
            }
        } catch (JSONException|IllegalArgumentException exception) {
            TradeLogger.logException(exception);
            return;
        }

        Coin coin = GlobalVariables.enabledCoins.get(pifagorAltcoinsSignal.getTicket());

        if (coin == null) {
            TradeLogger.logTgBot(I18nSupport.i18n_literals("coin.not.enabled", pifagorAltcoinsSignal.getTicket()));
            return;
        }

        PositionSide positionSide = pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ? PositionSide.LONG : PositionSide.SHORT;

        WebhookRequest webhookRequest = new WebhookRequest(coin, positionSide);

        try {
            MyTrade myTrade;
            Position position;
            Long orderId;

            if ((orderId = requestSender.closePositionMarket(webhookRequest.getTicket(),
                    positionSide.equals(PositionSide.LONG) ? PositionSide.SHORT : PositionSide.LONG)) != null) {
                myTrade = requestSender.getMyTrade(webhookRequest.getTicket(), orderId);

                if (myTrade != null) {
                    TradeLogger.logCloseOrder(myTrade);
                } else {
                    TradeLogger.logException(new NullPointerException(I18nSupport.i18n_literals("order.not.close.exception")));
                }
            }

            if (requestSender.openPositionMarket(webhookRequest.getTicket(),
                    positionSide.equals(PositionSide.LONG) ? OrderSide.BUY : OrderSide.SELL,
                    MarginType.ISOLATED,
                    positionSide,
                    webhookRequest.getAmount(),
                    webhookRequest.getLeverage())) {
                position = requestSender.getPosition(webhookRequest.getTicket(), webhookRequest.getPositionSide());

                if (position != null) {
                    TradeLogger.logOpenOrder(position);
                } else {
                    TradeLogger.logException(new NullPointerException(I18nSupport.i18n_literals("order.not.open.exception")));
                }
            }
        } catch (BinanceApiException|NullPointerException exception) {
            TradeLogger.logException(exception);
        }
    }
}
