package com.strategies;

import com.binance.client.model.enums.MarginType;
import com.binance.client.model.enums.OrderSide;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.MyTrade;
import com.binance.client.model.trade.Order;
import com.binance.client.model.trade.Position;
import com.futures.Amount;
import com.futures.dualside.RequestSender;
import com.log.TradeLogger;
import com.signal.PIFAGOR_ALTCOINS_SIGNAL;
import com.signal.Signal;
import com.utils.Constants;
import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AltcoinsHandler extends StrategyHandler {
    private final Amount amount;
    private final int leverage;

    public AltcoinsHandler(RequestSender requestSender, StrategyProps strategyProps, TradeLogger tradeLogger) throws IllegalArgumentException {
        super(requestSender, strategyProps, tradeLogger);

        amount = new Amount(strategyProps.getProperties().getProperty(Constants.AMOUNT_STR));
        leverage = Integer.parseInt(strategyProps.getProperties().getProperty(Constants.LEVERAGE_STR));
    }

    @Override
    public synchronized void process(JSONObject inputSignal) throws JSONException, IllegalArgumentException {
        Class<?> signalClass = Signal.getSignalClass(inputSignal);

        PIFAGOR_ALTCOINS_SIGNAL pifagorAltcoinsSignal;

        if (signalClass == PIFAGOR_ALTCOINS_SIGNAL.class) {
            pifagorAltcoinsSignal = new PIFAGOR_ALTCOINS_SIGNAL(inputSignal);

            tradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.altcoins.signal",
                    pifagorAltcoinsSignal.getTicker(),
                    pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ? 0 : 1,
                    pifagorAltcoinsSignal.getAction(),
                    pifagorAltcoinsSignal.getClose()));
        } else {
            throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
        }

        Order closePositionOrder = requestSender.closePositionMarket(strategyProps.getTicker(),
                pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ?
                        PositionSide.SHORT : PositionSide.LONG);

        requestSender.openPositionMarket(strategyProps.getTicker(),
                pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ?
                        OrderSide.BUY : OrderSide.SELL,
                MarginType.ISOLATED,
                pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ?
                        PositionSide.LONG : PositionSide.SHORT,
                amount,
                leverage);

        List<MyTrade> myTrades = closePositionOrder != null ? requestSender.getMyTrades(strategyProps.getTicker(),
                closePositionOrder.getOrderId()) : null;

        tradeLogger.logClosePosition(myTrades);
        tradeLogger.logCloseLog(Strategy.ALTCOINS, myTrades);

        Position position = requestSender.getPosition(strategyProps.getTicker(),
                pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ?
                        PositionSide.LONG : PositionSide.SHORT);

        tradeLogger.logOpenPosition(position);
    }

    @Override
    public void close() {

    }
}
