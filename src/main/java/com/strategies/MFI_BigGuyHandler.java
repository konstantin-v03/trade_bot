package com.strategies;

import com.binance.client.model.enums.MarginType;
import com.binance.client.model.enums.PositionSide;
import com.futures.Amount;
import com.futures.dualside.RequestSender;
import com.log.TradeLogger;
import com.signal.PIFAGOR_KHALIFA_Signal;
import com.signal.PIFAGOR_MFI_Signal;
import com.signal.Signal;
import com.utils.DateUtils;
import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.*;

import static com.utils.Utils.getCandlestickIndex;
import static com.utils.Utils.percentToBigDecimal;

public class MFI_BigGuyHandler extends StrategyHandler {
    public static final String NAME = "PIFAGOR_MFI_BIG_GUY";

    private PIFAGOR_MFI_Signal pifagorMfiSignal;
    private PIFAGOR_KHALIFA_Signal pifagorKhalifaSignal;

    public MFI_BigGuyHandler(RequestSender requestSender, StrategyProps strategyProps) throws IllegalArgumentException {
        super(requestSender, strategyProps);
    }

    @Override
    public synchronized void process(JSONObject inputSignal) {
        int currIndex = getCandlestickIndex(new Date(), strategyProps.getInterval());

        try {
            Class<?> signalClass = Signal.getSignalClass(inputSignal);

            if (signalClass == PIFAGOR_MFI_Signal.class) {
                pifagorMfiSignal = new PIFAGOR_MFI_Signal(inputSignal);
                TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.mfi.signal", pifagorMfiSignal.getAction().toString(), pifagorMfiSignal.getClose()));
                TradeLogger.logTgBot("Current index: " + currIndex + "\n" + pifagorMfiSignal.toString());
            } else if (signalClass == PIFAGOR_KHALIFA_Signal.class) {
                pifagorKhalifaSignal = new PIFAGOR_KHALIFA_Signal(inputSignal);
                TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.khalifa.signal.floor", pifagorKhalifaSignal.getFloor(), pifagorKhalifaSignal.getClose()));
                TradeLogger.logTgBot("Current index: " + currIndex + "\n" + pifagorKhalifaSignal.toString());
            } else {
                throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
            }

            if (pifagorKhalifaSignal != null
                    && pifagorKhalifaSignal.getTicker().equals(strategyProps.getTicker())
                    && pifagorKhalifaSignal.getInterval() == strategyProps.getInterval()
                    && pifagorKhalifaSignal.getFloor() == 3
                    && currIndex - 1 == getCandlestickIndex(pifagorKhalifaSignal.getTime(), strategyProps.getInterval())
                    && pifagorMfiSignal != null
                    && pifagorMfiSignal.getTicker().equals(strategyProps.getTicker())
                    && pifagorMfiSignal.getInterval() == strategyProps.getInterval()
                    && pifagorMfiSignal.getAction().equals(PIFAGOR_MFI_Signal.Action.STRONG_BUY)
                    && currIndex - 1 == getCandlestickIndex(pifagorMfiSignal.getTime(), strategyProps.getInterval())
                    && requestSender.getPosition(strategyProps.getTicker(), PositionSide.LONG) == null) {
                requestSender.openLongPositionMarket(strategyProps.getTicker(), MarginType.ISOLATED, strategyProps.getAmount(), strategyProps.getLeverage());
                TradeLogger.logOpenPosition(requestSender.getPosition(strategyProps.getTicker(), PositionSide.LONG));
                TradeLogger.logTP_SLOrders(requestSender.postTP_SLOrders(strategyProps.getTicker(), PositionSide.LONG, strategyProps.getTakeProfit(), strategyProps.getStopLoss()));
            }
        } catch (JSONException|IllegalArgumentException exception) {
            TradeLogger.logException(exception);
        }
    }
}
