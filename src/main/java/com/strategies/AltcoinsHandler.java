package com.strategies;

import com.binance.client.model.enums.MarginType;
import com.binance.client.model.enums.OrderSide;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.MyTrade;
import com.binance.client.model.trade.Position;
import com.futures.dualside.RequestSender;
import com.log.TradeLogger;
import com.signal.PIFAGOR_ALTCOINS_SIGNAL;
import com.signal.Signal;
import com.utils.Constants;
import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Properties;

import static com.utils.Constants.INTERVAL_1h;
import static com.utils.Constants.INTERVAL_4h;

public class AltcoinsHandler extends StrategyHandler {
    private PIFAGOR_ALTCOINS_SIGNAL pifagorAltcoinsSignal1h;
    private PIFAGOR_ALTCOINS_SIGNAL pifagorAltcoinsSignal4h;

    public AltcoinsHandler(RequestSender requestSender, StrategyProps strategyProps) {
        super(requestSender, strategyProps);

        Properties properties = strategyProps.getProperties();
        String action1hStr, action4hStr;

        if (properties != null && (action1hStr = properties.getProperty(Constants.INTERVAL_1H_STR)) != null &&
                (action4hStr = properties.getProperty(Constants.INTERVAL_4H_STR)) != null) {
            PIFAGOR_ALTCOINS_SIGNAL.Action action1h = PIFAGOR_ALTCOINS_SIGNAL.Action.valueOf(action1hStr);
            PIFAGOR_ALTCOINS_SIGNAL.Action action4h = PIFAGOR_ALTCOINS_SIGNAL.Action.valueOf(action4hStr);

            pifagorAltcoinsSignal1h = new PIFAGOR_ALTCOINS_SIGNAL(strategyProps.getTicker(),
                    null,
                    null,
                    null,
                    new Date(),
                    action1h);

            pifagorAltcoinsSignal4h = new PIFAGOR_ALTCOINS_SIGNAL(strategyProps.getTicker(),
                    null,
                    null,
                    null,
                    new Date(),
                    action4h);
        }
    }

    @Override
    public synchronized void process(JSONObject inputSignal) throws JSONException, IllegalArgumentException {
        Class<?> signalClass = Signal.getSignalClass(inputSignal);

        PIFAGOR_ALTCOINS_SIGNAL pifagorAltcoinsSignal;

        if (signalClass == PIFAGOR_ALTCOINS_SIGNAL.class) {
            pifagorAltcoinsSignal = new PIFAGOR_ALTCOINS_SIGNAL(inputSignal);

            if (pifagorAltcoinsSignal.getInterval() == INTERVAL_4h) {
                pifagorAltcoinsSignal4h = pifagorAltcoinsSignal;
            } else if (pifagorAltcoinsSignal.getInterval() == INTERVAL_1h) {
                pifagorAltcoinsSignal1h = pifagorAltcoinsSignal;
            } else {
                throw new JSONException("unsupported.signal.exception");
            }

            TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.altcoins.signal",
                    pifagorAltcoinsSignal.getTicker(),
                    pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ? 0 : 1,
                    pifagorAltcoinsSignal.getAction(),
                    pifagorAltcoinsSignal.getInterval() == INTERVAL_1h ? Constants.INTERVAL_1H_STR : Constants.INTERVAL_4H_STR,
                    pifagorAltcoinsSignal.getClose()));

            if (strategyProps.isDebugMode()) {
                TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.altcoins.debug",
                        pifagorAltcoinsSignal.getTicker(),
                        pifagorAltcoinsSignal1h == null ? 0 : pifagorAltcoinsSignal1h.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ? 1 : 2,
                        pifagorAltcoinsSignal4h == null ? 0 : pifagorAltcoinsSignal4h.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ? 1 : 2));
            }
        } else {
            throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
        }

        Position longPosition = requestSender.getPosition(strategyProps.getTicker(), PositionSide.LONG);
        Position shortPosition = requestSender.getPosition(strategyProps.getTicker(), PositionSide.SHORT);

        if (pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.SELL) && longPosition != null) {
            if (strategyProps.isDebugMode()) {
                TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.altcoins.try.close.debug",
                        strategyProps.getTicker(),
                        PositionSide.LONG));
            }

            MyTrade myTrade = requestSender.getMyTrade(strategyProps.getTicker(),
                    requestSender.closePositionMarket(strategyProps.getTicker(), PositionSide.LONG).getOrderId());

            TradeLogger.logClosePosition(myTrade);
            TradeLogger.logCloseLog(Strategy.ALTCOINS, myTrade);
        }

        if (pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) && shortPosition != null) {
            if (strategyProps.isDebugMode()) {
                TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.altcoins.try.close.debug",
                        strategyProps.getTicker(),
                        PositionSide.SHORT));
            }

            MyTrade myTrade = requestSender.getMyTrade(strategyProps.getTicker(),
                    requestSender.closePositionMarket(strategyProps.getTicker(), PositionSide.SHORT).getOrderId());

            TradeLogger.logClosePosition(myTrade);
            TradeLogger.logCloseLog(Strategy.ALTCOINS, myTrade);

        }

        if (pifagorAltcoinsSignal1h != null &&
                pifagorAltcoinsSignal4h != null &&
                pifagorAltcoinsSignal1h.getAction().equals(pifagorAltcoinsSignal4h.getAction())) {
            if (pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) && longPosition != null) {
                TradeLogger.logTgBot(I18nSupport.i18n_literals("position.already.opened", PositionSide.LONG, longPosition.getEntryPrice()));
            } else if (pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.SELL) && shortPosition != null) {
                TradeLogger.logTgBot(I18nSupport.i18n_literals("position.already.opened", PositionSide.SHORT, shortPosition.getEntryPrice()));
            } else {
                if (strategyProps.isDebugMode()) {
                    TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.altcoins.try.open.debug",
                            strategyProps.getTicker(),
                            pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ?
                                    PositionSide.LONG : PositionSide.SHORT));
                }

                requestSender.openPositionMarket(strategyProps.getTicker(),
                        pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ?
                                OrderSide.BUY : OrderSide.SELL,
                        MarginType.ISOLATED,
                        pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ?
                                PositionSide.LONG : PositionSide.SHORT,
                        strategyProps.getAmount(),
                        strategyProps.getLeverage());

                TradeLogger.logOpenPosition(requestSender.getPosition(strategyProps.getTicker(),
                        pifagorAltcoinsSignal1h.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ?
                                PositionSide.LONG : PositionSide.SHORT));
            }
        }
    }

    @Override
    public void close() {

    }
}
