package com.strategies;

import com.binance.client.model.enums.MarginType;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.Order;
import com.futures.Amount;
import com.futures.TP_SL;
import com.futures.dualside.RequestSender;
import com.signal.DEFAULT_STRATEGY_SIGNAL;
import com.signal.Indicator;
import com.signal.StrategyAction;
import com.tgbot.AsyncSender;
import com.utils.Constants;
import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

public class DefaultStrategy extends StrategyHandler {
    private final String ticker;
    private final Amount amount;
    private final int leverage;
    private final boolean debugMode;
    private final boolean testMode;

    private TP_SL longTP_SL;
    private TP_SL shortTP_SL;

    public DefaultStrategy(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender) {
        super(requestSender, strategyProps, asyncSender);

        if (strategyProps.getTickers().size() != 1) {
            throw new IllegalArgumentException(I18nSupport.i18n_literals("tickers.size.must.be.one"));
        }

        ticker = strategyProps.getTickers().get(0);
        amount = new Amount(strategyProps.getProperties().get(Constants.AMOUNT.getKey()));
        leverage = Integer.parseInt(strategyProps.getProperties().get(Constants.LEVERAGE.getKey()));
        debugMode = Boolean.parseBoolean(strategyProps.getProperties().get(Constants.DEBUG_MODE.getKey()));
        testMode = Boolean.parseBoolean(strategyProps.getProperties().get(Constants.TEST_MODE.getKey()));
    }

    @Override
    public void process(Indicator indicator, JSONObject inputRequest) throws JSONException, IllegalArgumentException {
        DEFAULT_STRATEGY_SIGNAL defaultStrategySignal;

        if (indicator.equals(Indicator.DEFAULT_STRATEGY)) {
            defaultStrategySignal = new DEFAULT_STRATEGY_SIGNAL(inputRequest);

            if (debugMode) {
                logger.logTgBot(I18nSupport.i18n_literals("default.strategy.signal",
                        defaultStrategySignal.getTicker(),
                        defaultStrategySignal.getComment(),
                        defaultStrategySignal.getClose()));
            }
        } else {
            throw new JSONException(I18nSupport.i18n_literals("unsupported.signal.exception"));
        }

        StrategyAction strategyAction = new StrategyAction(defaultStrategySignal.getComment());

        if (strategyAction.getType().equals(StrategyAction.Type.OPEN)) {
            if (!testMode) {
                cancelTPOrder(strategyAction.getPositionSide());
                cancelSLOrder(strategyAction.getPositionSide());

                requestSender.openPositionMarket(ticker,
                        PositionSide.LONG,
                        MarginType.ISOLATED,
                        amount,
                        leverage);

                logger.logOpenedPosition(requestSender.getPosition(ticker, strategyAction.getPositionSide()));
            }

            if (debugMode) {
                logger.logTgBot(I18nSupport.i18n_literals("default.strategy.open", ticker, strategyAction.getPositionSide(), defaultStrategySignal.getClose()));
            }
        } else if (strategyAction.getType().equals(StrategyAction.Type.CLOSE)) {
            if (!testMode) {
                cancelTPOrder(strategyAction.getPositionSide());
                cancelSLOrder(strategyAction.getPositionSide());

                Order closePositionOrder = requestSender.closePositionMarket(ticker, strategyAction.getPositionSide(), 100);

                logger.logClosedPosition(closePositionOrder != null ? requestSender.getMyTrades(ticker,
                        closePositionOrder.getOrderId()) : null);
            }

            if (debugMode) {
                logger.logTgBot(I18nSupport.i18n_literals("default.strategy.close", ticker, strategyAction.getPositionSide(), defaultStrategySignal.getClose()));
            }
        } else if (strategyAction.getType().equals(StrategyAction.Type.TAKE_PROFIT)) {
            if (!testMode) {
                cancelTPOrder(strategyAction.getPositionSide());

                TP_SL tp_sl = requestSender.postTP_SLOrdersPrice(ticker, strategyAction.getPositionSide(), strategyAction.getTakeProfit(), null);

                setTPOrder(strategyAction.getPositionSide(), tp_sl);

                logger.logTP_SLOrders(tp_sl);
            }

            if (debugMode) {
                logger.logTgBot(I18nSupport.i18n_literals("default.strategy.tp", ticker, strategyAction.getPositionSide(), strategyAction.getTakeProfit()));
            }
        } else if (strategyAction.getType().equals(StrategyAction.Type.STOP_LOSS)) {
            if (!testMode) {
                cancelSLOrder(strategyAction.getPositionSide());

                TP_SL tp_sl = requestSender.postTP_SLOrdersPrice(ticker, strategyAction.getPositionSide(), null, strategyAction.getStopLoss());

                setSLOrder(strategyAction.getPositionSide(), tp_sl);

                logger.logTP_SLOrders(tp_sl);
            }

            if (debugMode) {
                logger.logTgBot(I18nSupport.i18n_literals("default.strategy.sl", ticker, strategyAction.getPositionSide(), strategyAction.getStopLoss()));
            }
        } else if (strategyAction.getType().equals(StrategyAction.Type.TP_SL)) {
            if (!testMode) {
                cancelTPOrder(strategyAction.getPositionSide());
                cancelSLOrder(strategyAction.getPositionSide());

                TP_SL tp_sl = requestSender.postTP_SLOrdersPrice(ticker, strategyAction.getPositionSide(), strategyAction.getTakeProfit(), strategyAction.getStopLoss());

                setTPOrder(strategyAction.getPositionSide(), tp_sl);
                setSLOrder(strategyAction.getPositionSide(), tp_sl);

                logger.logTP_SLOrders(tp_sl);
            }

            if (debugMode) {
                logger.logTgBot(I18nSupport.i18n_literals("default.strategy.tp.sl", ticker, strategyAction.getPositionSide(), strategyAction.getTakeProfit(), strategyAction.getStopLoss()));
            }
        }
    }

    private void cancelTPOrder(PositionSide positionSide) {
        if (positionSide.equals(PositionSide.LONG) && longTP_SL != null && longTP_SL.getTakeProfitOrder() != null) {
            requestSender.cancelOrder(ticker, longTP_SL.getTakeProfitOrder().getOrderId());
            longTP_SL.setTakeProfitOrder(null);
        } else if (positionSide.equals(PositionSide.SHORT) && shortTP_SL != null && shortTP_SL.getTakeProfitOrder() != null) {
            requestSender.cancelOrder(ticker, shortTP_SL.getTakeProfitOrder().getOrderId());
            shortTP_SL.setTakeProfitOrder(null);
        }
    }

    private void cancelSLOrder(PositionSide positionSide) {
        if (positionSide.equals(PositionSide.LONG) && longTP_SL != null && longTP_SL.getStopLossOrder() != null) {
            requestSender.cancelOrder(ticker, longTP_SL.getStopLossOrder().getOrderId());
            longTP_SL.setStopLossOrder(null);
        } else if (positionSide.equals(PositionSide.SHORT) && shortTP_SL != null && shortTP_SL.getStopLossOrder() != null)  {
            requestSender.cancelOrder(ticker, shortTP_SL.getStopLossOrder().getOrderId());
            shortTP_SL.setStopLossOrder(null);
        }
    }

    private void setTPOrder(PositionSide positionSide, TP_SL tp_sl) {
        if (positionSide.equals(PositionSide.LONG)) {
            if (longTP_SL == null) {
                longTP_SL = tp_sl;
            }

            longTP_SL.setTakeProfitOrder(tp_sl.getTakeProfitOrder());
        } else if (positionSide.equals(PositionSide.SHORT)) {
            if (shortTP_SL == null) {
                shortTP_SL = tp_sl;
            }

            shortTP_SL.setTakeProfitOrder(tp_sl.getTakeProfitOrder());
        }
    }

    private void setSLOrder(PositionSide positionSide, TP_SL tp_sl) {
        if (positionSide.equals(PositionSide.LONG)) {
            if (longTP_SL == null) {
                longTP_SL = tp_sl;
            }

            longTP_SL.setStopLossOrder(tp_sl.getStopLossOrder());
        } else if (positionSide.equals(PositionSide.SHORT)) {
            if (shortTP_SL == null) {
                shortTP_SL = tp_sl;
            }

            shortTP_SL.setStopLossOrder(tp_sl.getStopLossOrder());
        }
    }

    @Override
    public boolean isSupportedSignal(Class<?> signal, String ticker) {
        return this.ticker.equals(ticker) && signal.equals(DEFAULT_STRATEGY_SIGNAL.class);
    }

    @Override
    public void close() {

    }
}
