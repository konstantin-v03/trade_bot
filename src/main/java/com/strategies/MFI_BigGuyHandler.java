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

import static com.utils.Utils.percentToBigDecimal;

public class MFI_BigGuyHandler extends StrategyHandler {
    private final String TICKER;
    private final int INTERVAL;
    private final BigDecimal TAKE_PROFIT;
    private final BigDecimal STOP_LOSS;
    private final Amount AMOUNT;
    private final int LEVERAGE;

    private PIFAGOR_MFI_Signal pifagorMfiSignal;
    private PIFAGOR_KHALIFA_Signal pifagorKhalifaSignal;

    public MFI_BigGuyHandler(RequestSender requestSender) throws IllegalArgumentException {
        super(requestSender);
        TICKER = System.getProperty("ticker");
        INTERVAL = Integer.parseInt(System.getProperty("interval"));
        TAKE_PROFIT = percentToBigDecimal(System.getProperty("takeprofit"));
        STOP_LOSS = percentToBigDecimal(System.getProperty("stoploss"));
        AMOUNT = new Amount(System.getProperty("amount"));
        LEVERAGE = Integer.parseInt(System.getProperty("leverage"));
    }

    @Override
    public synchronized void process() {
        int currIndex = getCandlestickIndex(new Date(), INTERVAL);

        try {
            JSONObject jsonObject = new JSONObject(inputRequest);

            Class<?> signalClass = Signal.getSignalClass(jsonObject);

            if (signalClass == PIFAGOR_MFI_Signal.class) {
                pifagorMfiSignal = new PIFAGOR_MFI_Signal(jsonObject);
                TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.mfi.signal", pifagorMfiSignal.getAction().toString(), pifagorMfiSignal.getClose()));
            } else if (signalClass == PIFAGOR_KHALIFA_Signal.class) {
                pifagorKhalifaSignal = new PIFAGOR_KHALIFA_Signal(jsonObject);
                TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.khalifa.signal.floor", pifagorKhalifaSignal.getFloor(), pifagorKhalifaSignal.getClose()));
            } else {
                throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
            }

            if (pifagorKhalifaSignal != null
                    && pifagorKhalifaSignal.getTicker().equals(TICKER)
                    && pifagorKhalifaSignal.getInterval() == INTERVAL
                    && pifagorKhalifaSignal.getFloor() == 3
                    && currIndex == getCandlestickIndex(pifagorKhalifaSignal.getTime(), INTERVAL)
                    && pifagorMfiSignal != null
                    && pifagorMfiSignal.getTicker().equals(TICKER)
                    && pifagorMfiSignal.getInterval() == INTERVAL
                    && pifagorMfiSignal.getAction().equals(PIFAGOR_MFI_Signal.Action.STRONG_BUY)
                    && currIndex == getCandlestickIndex(pifagorMfiSignal.getTime(), INTERVAL)
                    && requestSender.getPosition(TICKER, PositionSide.LONG) == null) {
                requestSender.openLongPositionMarket(TICKER, MarginType.ISOLATED, AMOUNT, LEVERAGE);
                TradeLogger.logOpenPosition(requestSender.getPosition(TICKER, PositionSide.LONG));
                TradeLogger.logTP_SLOrders(requestSender.postTP_SLOrders(TICKER, PositionSide.LONG, TAKE_PROFIT, STOP_LOSS));
            }
        } catch (JSONException|IllegalArgumentException exception) {
            TradeLogger.logException(exception);
        }
    }

    private static int getCandlestickIndex(Date date, int interval) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return (calendar.get(Calendar.HOUR_OF_DAY) *
                DateUtils.MINUTES_IN_HOUR +
                calendar.get(Calendar.MINUTE)) / interval;
    }
}
