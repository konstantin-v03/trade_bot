package com.strategies;

import com.binance.client.model.enums.MarginType;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.MyTrade;
import com.binance.client.model.trade.Position;
import com.futures.Amount;
import com.futures.TP_SL;
import com.futures.dualside.RequestSender;
import com.signal.*;
import com.tgbot.AsyncSender;
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
    private PIFAGOR_MFI_SIGNAL pifagorMfiSignal;
    private PIFAGOR_KHALIFA_SIGNAL pifagorKhalifaSignal;

    private final Amount amount;
    private final String ticker;
    private final int leverage;
    private final int takeProfit;
    private final int stopLoss;

    private final Object lock = new Object();
    private TP_SL tp_sl;
    private final Timer timer = new Timer();

    public MFI_BigGuyHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender) throws IllegalArgumentException {
        super(requestSender, strategyProps, asyncSender);

        if (strategyProps.getTickers().size() != 1) {
            throw new IllegalArgumentException(I18nSupport.i18n_literals("tickers.size.must.be.one"));
        }

        ticker = strategyProps.getTickers().get(0);
        amount = new Amount(strategyProps.getProperties().get(Constants.AMOUNT.getKey()));
        leverage = Integer.parseInt(strategyProps.getProperties().get(Constants.LEVERAGE.getKey()));
        takeProfit = Integer.parseInt(strategyProps.getProperties().get(Constants.TAKE_PROFIT.getKey()));
        stopLoss = Integer.parseInt(strategyProps.getProperties().get(Constants.STOP_LOSS.getKey()));

        timer.schedule(new TimerTask() {
            public void run() {
                if (tp_sl != null) {
                    logTP_SL();
                }
            }
        }, 0, 5 * 60 * 1000);
    }

    @Override
    public synchronized void process(Indicator indicator, JSONObject inputSignal) throws JSONException, IllegalArgumentException {
        int currIndex = getCandlestickIndex(new Date(), INTERVAL_5m);

        if (indicator.equals(Indicator.PIFAGOR_MFI)) {
            pifagorMfiSignal = new PIFAGOR_MFI_SIGNAL(inputSignal);
            logger.logTgBot(I18nSupport.i18n_literals("pifagor.mfi.signal", pifagorMfiSignal.getAction().toString(), pifagorMfiSignal.getClose()));

            if (Boolean.parseBoolean(strategyProps.getProperties().get(Constants.DEBUG_MODE.getKey()))) {
                logger.logTgBot(I18nSupport.i18n_literals("pifagor.mfi.big.guy.debug",
                            currIndex,
                            pifagorMfiSignal.toString()));
            }
        } else if (indicator.equals(Indicator.PIFAGOR_KHALIFA)) {
            pifagorKhalifaSignal = new PIFAGOR_KHALIFA_SIGNAL(inputSignal);
            logger.logTgBot(I18nSupport.i18n_literals("pifagor.khalifa.signal.floor", pifagorKhalifaSignal.getFloor(), pifagorKhalifaSignal.getClose()));

            if (Boolean.parseBoolean(strategyProps.getProperties().get(Constants.DEBUG_MODE.getKey()))) {
                logger.logTgBot(I18nSupport.i18n_literals("pifagor.mfi.big.guy.debug",
                            currIndex,
                            pifagorKhalifaSignal.toString()));
            }
        } else {
            throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
        }

        if (pifagorKhalifaSignal != null
                && pifagorKhalifaSignal.getTicker().equals(ticker)
                && pifagorKhalifaSignal.getInterval() == INTERVAL_5m
                && pifagorKhalifaSignal.getFloor() == 3
                && currIndex - 1 == getCandlestickIndex(pifagorKhalifaSignal.getTime(), INTERVAL_5m)
                && pifagorMfiSignal != null
                && pifagorMfiSignal.getTicker().equals(ticker)
                && pifagorMfiSignal.getInterval() == INTERVAL_5m
                && pifagorMfiSignal.getAction().equals(PIFAGOR_MFI_SIGNAL.MFI_Action.STRONG_BUY)
                && currIndex - 1 == getCandlestickIndex(pifagorMfiSignal.getTime(), INTERVAL_5m)) {
            Position position = requestSender.getPosition(ticker, PositionSide.LONG);

            if (position == null) {
                requestSender.openPositionMarket(ticker, PositionSide.LONG, MarginType.ISOLATED, amount, leverage);
                requestSender.cancelOrders(ticker);

                synchronized (lock) {
                    logger.logTP_SLOrders(tp_sl = requestSender.postTP_SLOrders(ticker,
                            PositionSide.LONG,
                            takeProfit,
                            stopLoss));
                }

                Utils.sleep(1000);
                logger.logOpenedPosition(requestSender.getPosition(ticker, PositionSide.LONG));
            } else {
                logger.logTgBot(I18nSupport.i18n_literals("position.already.opened", PositionSide.LONG, position.getEntryPrice()));
            }
        }
    }

    @Override
    public boolean isSupportedSignal(Class<?> signal, String ticker) {
        return this.ticker.equals(ticker) &&
                (signal.equals(PIFAGOR_MFI_SIGNAL.class) || signal.equals(PIFAGOR_KHALIFA_SIGNAL.class));
    }

    private void logTP_SL() {
        List<MyTrade> stopLoss, takeProfit;

        synchronized (lock) {
            if (tp_sl.getStopLossOrder() != null && (stopLoss = requestSender.getMyTrades(ticker,
                    tp_sl.getStopLossOrder().getOrderId())) != null) {
                logger.logClosedPosition(stopLoss);
                logger.logCloseLogToFile(Strategy.MFI_BIG_GUY, stopLoss);
            }

            if (tp_sl.getTakeProfitOrder() != null && (takeProfit = requestSender.getMyTrades(ticker,
                    tp_sl.getTakeProfitOrder().getOrderId())) != null) {
                logger.logClosedPosition(takeProfit);
                logger.logCloseLogToFile(Strategy.MFI_BIG_GUY, takeProfit);
            }

            tp_sl = null;
        }
    }

    @Override
    public void close() {
        timer.cancel();
    }
}
