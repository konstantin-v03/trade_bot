package com.strategies;

import com.binance.client.model.enums.MarginType;
import com.binance.client.model.enums.OrderSide;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.Position;
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
    public void process(JSONObject inputSignal) throws JSONException, IllegalArgumentException {
        Class<?> signalClass = Signal.getSignalClass(inputSignal);

        if (signalClass == PIFAGOR_ALTCOINS_SIGNAL.class) {
            PIFAGOR_ALTCOINS_SIGNAL pifagorAltcoinsSignal = new PIFAGOR_ALTCOINS_SIGNAL(inputSignal);
            String timeFrame;

            if (pifagorAltcoinsSignal.getInterval() == INTERVAL_4h) {
                pifagorAltcoinsSignal4h = pifagorAltcoinsSignal;
                timeFrame = "4h";
            } else if (pifagorAltcoinsSignal.getInterval() == INTERVAL_1h) {
                pifagorAltcoinsSignal1h = pifagorAltcoinsSignal;
                timeFrame = "1h";
            } else {
                throw new JSONException("unsupported.signal.exception");
            }

            TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.altcoins.signal",
                    pifagorAltcoinsSignal.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ? 0 : 1,
                    pifagorAltcoinsSignal.getAction(),
                    timeFrame,
                    pifagorAltcoinsSignal.getClose()));

            if (strategyProps.isDebugMode()) {
                TradeLogger.logTgBot(I18nSupport.i18n_literals("pifagor.altcoins.debug",
                        pifagorAltcoinsSignal1h,
                        pifagorAltcoinsSignal4h));
            }
        } else {
            throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
        }

        if (pifagorAltcoinsSignal1h == null || pifagorAltcoinsSignal4h == null) {
            return;
        }

        Position longPosition = requestSender.getPosition(strategyProps.getTicker(), PositionSide.LONG);
        Position shortPosition = requestSender.getPosition(strategyProps.getTicker(), PositionSide.SHORT);

        if (longPosition != null
                && !pifagorAltcoinsSignal1h.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY)
                && !pifagorAltcoinsSignal4h.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY)) {
            TradeLogger.logClosePosition(requestSender.getMyTrade(strategyProps.getTicker(),
                    requestSender.closePositionMarket(strategyProps.getTicker(), PositionSide.LONG).getOrderId()));
        }

        if (shortPosition != null
                && !pifagorAltcoinsSignal1h.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.SELL)
                && !pifagorAltcoinsSignal4h.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.SELL)) {
            TradeLogger.logClosePosition(requestSender.getMyTrade(strategyProps.getTicker(),
                    requestSender.closePositionMarket(strategyProps.getTicker(), PositionSide.SHORT).getOrderId()));
        }

        if (pifagorAltcoinsSignal4h.getAction().equals(pifagorAltcoinsSignal1h.getAction())) {
            requestSender.openPositionMarket(strategyProps.getTicker(),
                    pifagorAltcoinsSignal1h.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ?
                            OrderSide.BUY : OrderSide.SELL,
                    MarginType.ISOLATED,
                    pifagorAltcoinsSignal1h.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ?
                            PositionSide.LONG : PositionSide.SHORT,
                    strategyProps.getAmount(),
                    strategyProps.getLeverage());

            TradeLogger.logOpenPosition(requestSender.getPosition(strategyProps.getTicker(),
                    pifagorAltcoinsSignal1h.getAction().equals(PIFAGOR_ALTCOINS_SIGNAL.Action.BUY) ?
                            PositionSide.LONG : PositionSide.SHORT));
        }
    }
}
