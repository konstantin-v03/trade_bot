package com.strategies;

import com.binance.client.model.enums.MarginType;
import com.binance.client.model.enums.OrderSide;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.MyTrade;
import com.binance.client.model.trade.Order;
import com.binance.client.model.trade.Position;
import com.futures.Amount;
import com.futures.dualside.RequestSender;
import com.signal.ALARM_SIGNAL;
import com.signal.Indicator;
import com.signal.PIFAGOR_ALTCOINS_SIGNAL;
import com.tgbot.AsyncSender;
import com.utils.Constants;
import com.utils.I18nSupport;
import com.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import static com.utils.Constants.*;

@Deprecated
public class Altcoins1h4hHandler extends StrategyHandler {
    private PIFAGOR_ALTCOINS_SIGNAL pifagorAltcoinsSignal1h;
    private PIFAGOR_ALTCOINS_SIGNAL pifagorAltcoinsSignal4h;

    private final String ticker;
    private final Amount amount;
    private final int leverage;

    public Altcoins1h4hHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender) throws IllegalArgumentException{
        super(requestSender, strategyProps, asyncSender);

        String action1hStr, action4hStr;

        if (strategyProps.getTickers().size() != 1) {
            throw new IllegalArgumentException(I18nSupport.i18n_literals("tickers.size.must.be.one"));
        }

        ticker = strategyProps.getTickers().get(0);
        amount = new Amount(strategyProps.getProperties().get(Constants.AMOUNT_STR));
        leverage = Integer.parseInt(strategyProps.getProperties().get(Constants.LEVERAGE_STR));

        if ((action1hStr = strategyProps.getProperties().get(Constants.INTERVAL_1H_STR)) != null &&
                (action4hStr = strategyProps.getProperties().get(Constants.INTERVAL_4H_STR)) != null) {
            PIFAGOR_ALTCOINS_SIGNAL.Action action1h = PIFAGOR_ALTCOINS_SIGNAL.Action.valueOf(action1hStr);
            PIFAGOR_ALTCOINS_SIGNAL.Action action4h = PIFAGOR_ALTCOINS_SIGNAL.Action.valueOf(action4hStr);

            pifagorAltcoinsSignal1h = new PIFAGOR_ALTCOINS_SIGNAL(ticker,
                    null,
                    null,
                    null,
                    new Date(),
                    action1h);

            pifagorAltcoinsSignal4h = new PIFAGOR_ALTCOINS_SIGNAL(ticker,
                    null,
                    null,
                    null,
                    new Date(),
                    action4h);
        }
    }

    @Override
    public synchronized void process(Indicator indicator, JSONObject inputSignal) throws JSONException, IllegalArgumentException {
        PIFAGOR_ALTCOINS_SIGNAL pifagorAltcoinsSignal;

        if (indicator.equals(Indicator.PIFAGOR_ALTCOINS)) {
            pifagorAltcoinsSignal = new PIFAGOR_ALTCOINS_SIGNAL(inputSignal);

            if (pifagorAltcoinsSignal.getInterval() == INTERVAL_4h) {
                pifagorAltcoinsSignal4h = pifagorAltcoinsSignal;
            } else if (pifagorAltcoinsSignal.getInterval() == INTERVAL_1h) {
                pifagorAltcoinsSignal1h = pifagorAltcoinsSignal;
            } else {
                throw new JSONException("unsupported.signal.exception");
            }

            logger.logTgBot(I18nSupport.i18n_literals("pifagor.altcoins.1h.4h.signal",
                    pifagorAltcoinsSignal.getTicker(),
                    pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ? 0 : 1,
                    pifagorAltcoinsSignal.getAction(),
                    pifagorAltcoinsSignal.getInterval() == INTERVAL_1h ? Constants.INTERVAL_1H_STR : Constants.INTERVAL_4H_STR,
                    pifagorAltcoinsSignal.getClose()));

            if (Boolean.parseBoolean(strategyProps.getProperties().get(DEBUG_MODE_STR))) {
                logger.logTgBot(I18nSupport.i18n_literals("pifagor.altcoins.1h.4h.debug",
                        pifagorAltcoinsSignal.getTicker(),
                        pifagorAltcoinsSignal1h == null ? 0 : pifagorAltcoinsSignal1h.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ? 1 : 2,
                        pifagorAltcoinsSignal4h == null ? 0 : pifagorAltcoinsSignal4h.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ? 1 : 2));
            }
        } else {
            throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
        }

        Position longPosition = requestSender.getPosition(ticker, PositionSide.LONG);
        Position shortPosition = requestSender.getPosition(ticker, PositionSide.SHORT);

        Order closePositionOrder = null;

        if ((pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.SELL) && longPosition != null) ||
                (pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) && shortPosition != null)) {
            PositionSide positionSide = PIFAGOR_ALTCOINS_SIGNAL.Action.SELL.equals(pifagorAltcoinsSignal.getAction()) ?
                    PositionSide.LONG : PositionSide.SHORT;

            if (Boolean.parseBoolean(strategyProps.getProperties().get(DEBUG_MODE_STR))) {
                logger.logTgBot(I18nSupport.i18n_literals("pifagor.altcoins.1h.4h.try.close.debug",
                        ticker,
                        positionSide));
            }

            closePositionOrder = requestSender.closePositionMarket(ticker, positionSide);
        }

        boolean isOpen = false;

        if (pifagorAltcoinsSignal1h != null &&
                pifagorAltcoinsSignal4h != null &&
                pifagorAltcoinsSignal1h.getAction().equals(pifagorAltcoinsSignal4h.getAction())) {
            if (pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) && longPosition != null) {
                logger.logTgBot(I18nSupport.i18n_literals("position.already.opened", PositionSide.LONG, longPosition.getEntryPrice()));
            } else if (pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.SELL) && shortPosition != null) {
                logger.logTgBot(I18nSupport.i18n_literals("position.already.opened", PositionSide.SHORT, shortPosition.getEntryPrice()));
            } else {
                if (Boolean.parseBoolean(strategyProps.getProperties().get(DEBUG_MODE_STR))) {
                    logger.logTgBot(I18nSupport.i18n_literals("pifagor.altcoins.1h.4h.try.open.debug",
                            ticker,
                            pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ?
                                    PositionSide.LONG : PositionSide.SHORT));
                }

                requestSender.openPositionMarket(ticker,
                        pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ?
                                OrderSide.BUY : OrderSide.SELL,
                        MarginType.ISOLATED,
                        pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ?
                                PositionSide.LONG : PositionSide.SHORT,
                        amount,
                        leverage);

                isOpen = true;
            }
        }

        if (isOpen) {
            Utils.sleep(1000);
            logger.logOpenPosition(requestSender.getPosition(ticker,
                    pifagorAltcoinsSignal1h.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ?
                            PositionSide.LONG : PositionSide.SHORT));
        }

        if (closePositionOrder != null) {
            Utils.sleep(1000);
            List<MyTrade> myTrades = requestSender.getMyTrades(ticker, closePositionOrder.getOrderId());
            logger.logClosePosition(myTrades);
            logger.logCloseLogToFile(Strategy.ALTCOINS_1h_4h, myTrades);
        }
    }

    @Override
    public boolean isSupportedSignal(Class<?> signal, String ticker) {
        return this.ticker.equals(ticker) && signal.equals(ALARM_SIGNAL.class);
    }

    @Override
    public void close() {

    }
}
