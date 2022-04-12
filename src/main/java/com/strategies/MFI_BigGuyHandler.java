package com.strategies;

import com.binance.client.model.enums.MarginType;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.MyTrade;
import com.binance.client.model.trade.Position;
import com.futures.Amount;
import com.futures.TP_SL;
import com.futures.dualside.RequestSender;
import com.log.TradeLogger;
import com.signal.PIFAGOR_KHALIFA_Signal;
import com.signal.PIFAGOR_MFI_Signal;
import com.signal.Signal;
import com.utils.Constants;
import com.utils.I18nSupport;
import com.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static com.utils.Constants.INTERVAL_5m;
import static com.utils.Utils.getCandlestickIndex;

@Deprecated
public class MFI_BigGuyHandler extends StrategyHandler {
    private PIFAGOR_MFI_Signal pifagorMfiSignal;
    private PIFAGOR_KHALIFA_Signal pifagorKhalifaSignal;

    private final Amount amount;
    private final int leverage;
    private final int takeProfit;
    private final int stopLoss;

    private final Object lock = new Object();
    private TP_SL tp_sl;

    private final Timer timer = new Timer();

    public MFI_BigGuyHandler(RequestSender requestSender, StrategyProps strategyProps, TradeLogger tradeLogger) throws IllegalArgumentException {
        super(requestSender, strategyProps, tradeLogger);

        Properties properties = strategyProps.getProperties();

        amount = new Amount(properties.getProperty(Constants.AMOUNT_STR));
        leverage = Integer.parseInt(properties.getProperty(Constants.LEVERAGE_STR));
        takeProfit = Integer.parseInt(properties.getProperty(Constants.TAKE_PROFIT_STR));
        stopLoss = Integer.parseInt(properties.getProperty(Constants.STOP_LOSS_STR));

        timer.schedule(new TimerTask() {
            public void run() {
                if (tp_sl != null) {
                    logTP_SL();
                }
            }
        }, 0, 5 * 60 * 1000);
    }

    @Override
    public synchronized void process(JSONObject inputSignal) throws JSONException, IllegalArgumentException {
        int currIndex = getCandlestickIndex(new Date(), INTERVAL_5m);

        Class<?> signalClass = Signal.getSignalClass(inputSignal);

        if (signalClass == PIFAGOR_MFI_Signal.class) {
            pifagorMfiSignal = new PIFAGOR_MFI_Signal(inputSignal);
            tradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.mfi.signal", pifagorMfiSignal.getAction().toString(), pifagorMfiSignal.getClose()));

            if (strategyProps.isDebugMode()) {
                tradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.mfi.big.guy.debug",
                            currIndex,
                            pifagorMfiSignal.toString()));
            }
        } else if (signalClass == PIFAGOR_KHALIFA_Signal.class) {
            pifagorKhalifaSignal = new PIFAGOR_KHALIFA_Signal(inputSignal);
            tradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.khalifa.signal.floor", pifagorKhalifaSignal.getFloor(), pifagorKhalifaSignal.getClose()));

            if (strategyProps.isDebugMode()) {
                tradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.mfi.big.guy.debug",
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
                requestSender.openLongPositionMarket(strategyProps.getTicker(), MarginType.ISOLATED, amount, leverage);
                requestSender.cancelOrders(strategyProps.getTicker());

                synchronized (lock) {
                    tradeLogger.logTP_SLOrders(tp_sl = requestSender.postTP_SLOrders(strategyProps.getTicker(),
                            PositionSide.LONG,
                            takeProfit,
                            stopLoss));
                }

                Utils.sleep(1000);
                tradeLogger.logOpenPosition(requestSender.getPosition(strategyProps.getTicker(), PositionSide.LONG));
            } else {
                tradeLogger.logTgBot(I18nSupport.i18n_literals("position.already.opened", PositionSide.LONG, position.getEntryPrice()));
            }
        }
    }

    private void logTP_SL() {
        List<MyTrade> stopLoss, takeProfit;

        synchronized (lock) {
            if (tp_sl.getStopLossOrder() != null && (stopLoss = requestSender.getMyTrades(strategyProps.getTicker(),
                    tp_sl.getStopLossOrder().getOrderId())) != null) {
                tradeLogger.logClosePosition(stopLoss);
                tradeLogger.logCloseLog(Strategy.MFI_BIG_GUY, stopLoss);
            }

            if (tp_sl.getTakeProfitOrder() != null && (takeProfit = requestSender.getMyTrades(strategyProps.getTicker(),
                    tp_sl.getTakeProfitOrder().getOrderId())) != null) {
                tradeLogger.logClosePosition(takeProfit);
                tradeLogger.logCloseLog(Strategy.MFI_BIG_GUY, takeProfit);
            }

            tp_sl = null;
        }
    }

    @Override
    public void close() {
        timer.cancel();
    }
}
