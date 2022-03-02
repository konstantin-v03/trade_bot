package com.strategies;

import com.binance.client.model.enums.MarginType;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.Position;
import com.futures.dualside.RequestSender;
import com.log.TradeLogger;
import com.signal.PIFAGOR_KHALIFA_Signal;
import com.signal.PIFAGOR_MFI_Signal;
import com.signal.Signal;
import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static com.utils.Constants.INTERVAL_5m;
import static com.utils.Utils.getCandlestickIndex;

public class MFI_BigGuyHandler extends StrategyHandler {
    private PIFAGOR_MFI_Signal pifagorMfiSignal;
    private PIFAGOR_KHALIFA_Signal pifagorKhalifaSignal;

    public MFI_BigGuyHandler(RequestSender requestSender, StrategyProps strategyProps) throws IllegalArgumentException {
        super(requestSender, strategyProps);
    }

    @Override
    public synchronized void process(JSONObject inputSignal) throws JSONException, IllegalArgumentException {
        int currIndex = getCandlestickIndex(new Date(), INTERVAL_5m);

        Class<?> signalClass = Signal.getSignalClass(inputSignal);

        if (signalClass == PIFAGOR_MFI_Signal.class) {
            pifagorMfiSignal = new PIFAGOR_MFI_Signal(inputSignal);
            TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.mfi.signal", pifagorMfiSignal.getAction().toString(), pifagorMfiSignal.getClose()));

            if (strategyProps.isDebugMode()) {
                TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.mfi.big.guy.debug",
                            currIndex,
                            pifagorMfiSignal.toString()));
            }
        } else if (signalClass == PIFAGOR_KHALIFA_Signal.class) {
            pifagorKhalifaSignal = new PIFAGOR_KHALIFA_Signal(inputSignal);
            TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.khalifa.signal.floor", pifagorKhalifaSignal.getFloor(), pifagorKhalifaSignal.getClose()));

            if (strategyProps.isDebugMode()) {
                TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.mfi.big.guy.debug",
                            currIndex,
                            pifagorKhalifaSignal.toString()));
            }
        } else {
            throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
        }

        if (pifagorKhalifaSignal != null
                && pifagorKhalifaSignal.getTicker().equals(strategyProps.getTicker())
                && pifagorKhalifaSignal.getInterval() == INTERVAL_5m
                && pifagorKhalifaSignal.getFloor() == 3
                && currIndex - 1 == getCandlestickIndex(pifagorKhalifaSignal.getTime(), INTERVAL_5m)
                && pifagorMfiSignal != null
                && pifagorMfiSignal.getTicker().equals(strategyProps.getTicker())
                && pifagorMfiSignal.getInterval() == INTERVAL_5m
                && pifagorMfiSignal.getAction().equals(PIFAGOR_MFI_Signal.Action.STRONG_BUY)
                && currIndex - 1 == getCandlestickIndex(pifagorMfiSignal.getTime(), INTERVAL_5m)) {
            Position position = requestSender.getPosition(strategyProps.getTicker(), PositionSide.LONG);

            if (position == null) {
                requestSender.openLongPositionMarket(strategyProps.getTicker(), MarginType.ISOLATED, strategyProps.getAmount(), strategyProps.getLeverage());
                requestSender.cancelOrders(strategyProps.getTicker());
                TradeLogger.logOpenPosition(requestSender.getPosition(strategyProps.getTicker(), PositionSide.LONG));
                TradeLogger.logTP_SLOrders(requestSender.postTP_SLOrders(strategyProps.getTicker(), PositionSide.LONG, strategyProps.getTakeProfit(), strategyProps.getStopLoss()));
            } else {
                TradeLogger.logTgBot(I18nSupport.i18n_literals("position.already.opened", position.getEntryPrice()));
            }
        }
    }
}
