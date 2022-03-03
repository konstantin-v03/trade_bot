package com.strategies;

import com.binance.client.model.enums.MarginType;
import com.binance.client.model.enums.OrderSide;
import com.binance.client.model.enums.PositionSide;
import com.futures.dualside.RequestSender;
import com.log.TradeLogger;
import com.signal.PIFAGOR_ALTCOINS_SIGNAL;
import com.signal.Signal;
import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import static com.utils.Constants.INTERVAL_1h;
import static com.utils.Constants.INTERVAL_4h;

public class AltcoinsHandler extends StrategyHandler {
    private PIFAGOR_ALTCOINS_SIGNAL pifagorAltcoinsSignal1h;
    private PIFAGOR_ALTCOINS_SIGNAL pifagorAltcoinsSignal4h;

    public AltcoinsHandler(RequestSender requestSender, StrategyProps strategyProps) {
        super(requestSender, strategyProps);
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
                    pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ? 0 : 1,
                    pifagorAltcoinsSignal.getAction(),
                    pifagorAltcoinsSignal.getInterval() == INTERVAL_1h ? "1h" : "4h",
                    pifagorAltcoinsSignal.getClose()));

            if (strategyProps.isDebugMode()) {
                TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.altcoins.debug",
                        pifagorAltcoinsSignal1h,
                        pifagorAltcoinsSignal4h));
            }
        } else {
            throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
        }

        if (pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.SELL) &&
                requestSender.getPosition(strategyProps.getTicker(), PositionSide.LONG) != null) {
            TradeLogger.logClosePosition(requestSender.getMyTrade(strategyProps.getTicker(),
                    requestSender.closePositionMarket(strategyProps.getTicker(), PositionSide.LONG).getOrderId()));
        }

        if (pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) &&
                requestSender.getPosition(strategyProps.getTicker(), PositionSide.SHORT) != null) {
            TradeLogger.logClosePosition(requestSender.getMyTrade(strategyProps.getTicker(),
                    requestSender.closePositionMarket(strategyProps.getTicker(), PositionSide.SHORT).getOrderId()));
        }

        if (pifagorAltcoinsSignal1h != null &&
                pifagorAltcoinsSignal4h != null &&
                pifagorAltcoinsSignal1h.getAction().equals(pifagorAltcoinsSignal4h.getAction())) {
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
