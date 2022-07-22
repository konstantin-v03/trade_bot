package com.strategies;

import com.binance.client.model.enums.MarginType;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.MyTrade;
import com.binance.client.model.trade.Order;
import com.binance.client.model.trade.Position;
import com.futures.Amount;
import com.futures.dualside.RequestSender;
import com.signal.ALARM_SIGNAL;
import com.signal.Action;
import com.signal.Indicator;
import com.signal.PIFAGOR_ALTCOINS_SIGNAL;
import com.tgbot.AsyncSender;
import com.utils.Constants;
import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AltcoinsHandler extends StrategyHandler {
    private final String ticker;
    private final Amount amount;
    private final int leverage;

    public AltcoinsHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender) throws IllegalArgumentException {
        super(requestSender, strategyProps, asyncSender);

        if (strategyProps.getTickers().size() != 1) {
            throw new IllegalArgumentException(I18nSupport.i18n_literals("tickers.size.must.be.one"));
        }

        ticker = strategyProps.getTickers().get(0);
        amount = new Amount(strategyProps.getProperties().get(Constants.AMOUNT.getKey()));
        leverage = Integer.parseInt(strategyProps.getProperties().get(Constants.LEVERAGE.getKey()));
    }

    @Override
    public synchronized void process(Indicator indicator, JSONObject inputSignal) throws JSONException, IllegalArgumentException {
        PIFAGOR_ALTCOINS_SIGNAL pifagorAltcoinsSignal;

        if (indicator.equals(Indicator.PIFAGOR_ALTCOINS)) {
            pifagorAltcoinsSignal = new PIFAGOR_ALTCOINS_SIGNAL(inputSignal);

            logger.logTgBot(I18nSupport.i18n_literals("pifagor.altcoins.signal",
                    pifagorAltcoinsSignal.getTicker(),
                    pifagorAltcoinsSignal.getAction().getEmoji(),
                    pifagorAltcoinsSignal.getAction(),
                    pifagorAltcoinsSignal.getClose()));
        } else {
            throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
        }

        Order closePositionOrder = requestSender.closePositionMarket(ticker,
                pifagorAltcoinsSignal.getAction().equals(Action.BUY) ?
                        PositionSide.SHORT : PositionSide.LONG, 100);

        requestSender.openPositionMarket(ticker,
                pifagorAltcoinsSignal.getAction().equals(Action.BUY) ?
                        PositionSide.LONG : PositionSide.SHORT,
                MarginType.ISOLATED,
                amount,
                leverage);

        List<MyTrade> myTrades = closePositionOrder != null ? requestSender.getMyTrades(ticker,
                closePositionOrder.getOrderId()) : null;

        logger.logClosedPosition(myTrades);
        logger.logCloseLogToFile(Strategy.ALTCOINS, myTrades);

        Position position = requestSender.getPosition(ticker,
                pifagorAltcoinsSignal.getAction().equals(Action.BUY) ?
                        PositionSide.LONG : PositionSide.SHORT);

        logger.logOpenedPosition(position);
    }

    @Override
    public boolean isSupportedSignal(Class<?> signal, String ticker) {
        return this.ticker.equals(ticker) && signal.equals(ALARM_SIGNAL.class);
    }

    @Override
    public void close() {

    }
}
