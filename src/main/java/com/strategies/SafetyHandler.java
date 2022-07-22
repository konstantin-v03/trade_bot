package com.strategies;

import com.binance.client.model.enums.MarginType;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.MyTrade;
import com.binance.client.model.trade.Position;
import com.exceptions.IllegalQuantityException;
import com.futures.dualside.RequestSender;
import com.signal.*;
import com.tgbot.AsyncSender;
import com.utils.Constants;
import com.utils.I18nSupport;
import com.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SafetyHandler extends StrategyHandler {
    private final String ticker;
    private final int leverage;
    private Trend trend;
    private final boolean debugMode;
    private final boolean testMode;

    private final int yellowOpenLongPercentage;
    private final int greenOpenLongPercentage;
    private final int orangeCloseLongPercentage;
    private final int redCloseLongPercentage;

    private boolean isOrangeLongClosed;

    private final int blackOpenShortPercentage;
    private final int orangeOpenShortPercentage;
    private final int yellowCloseShortPercentage;
    private final int greenCloseShortPercentage;

    private boolean isYellowShortClosed;

    public SafetyHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender, long exceptionChatId) throws IllegalArgumentException {
        super(requestSender, strategyProps, asyncSender, exceptionChatId);

        if (strategyProps.getTickers().size() != 1) {
            throw new IllegalArgumentException(I18nSupport.i18n_literals("tickers.size.must.be.one"));
        }

        ticker = strategyProps.getTickers().get(0);
        leverage = Integer.parseInt(strategyProps.getProperties().get(Constants.LEVERAGE.getKey()));
        trend = Trend.valueOf(strategyProps.getProperties().get(Constants.TREND.getKey()));
        debugMode = Boolean.parseBoolean(strategyProps.getProperties().get(Constants.DEBUG_MODE.getKey()));
        testMode = Boolean.parseBoolean(strategyProps.getProperties().get(Constants.DEBUG_MODE.getKey()));

        yellowOpenLongPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.YELLOW_OPEN_LONG_PERCENTAGE.getKey()));
        greenOpenLongPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.GREEN_OPEN_LONG_PERCENTAGE.getKey()));
        orangeCloseLongPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.ORANGE_CLOSE_LONG_PERCENTAGE.getKey()));
        redCloseLongPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.RED_CLOSE_LONG_PERCENTAGE.getKey()));

        blackOpenShortPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.BLACK_OPEN_SHORT_PERCENTAGE.getKey()));
        orangeOpenShortPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.ORANGE_OPEN_SHORT_PERCENTAGE.getKey()));
        yellowCloseShortPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.YELLOW_CLOSE_SHORT_PERCENTAGE.getKey()));
        greenCloseShortPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.GREEN_CLOSE_SHORT_PERCENTAGE.getKey()));

        if (greenCloseShortPercentage != 100 || redCloseLongPercentage != 100) {
            throw new IllegalArgumentException();
        }
    }

    public boolean open$logPositionPercentageOfMarginBalance(String ticker, PositionSide positionSide, int percentage) {
        try {
            requestSender.openPositionPercentageOfMarginBalance(ticker, positionSide, MarginType.ISOLATED, percentage, leverage);
        } catch (IllegalQuantityException illegalQuantityException) {
            return false;
        }

        Position position = requestSender.getPosition(ticker, positionSide);

        logger.logOpenedPosition(position);

        return position != null;
    }

    public boolean close$LogPosition(String ticker, PositionSide positionSide, int percentage) {
        List<MyTrade> myTrades;

        try {
            myTrades = requestSender.getMyTrades(ticker,
                    requestSender.closePositionMarket(ticker, positionSide, percentage).getOrderId());
        } catch (IllegalQuantityException illegalQuantityException) {
            return false;
        }

        logger.logClosedPosition(myTrades);

        return myTrades != null && myTrades.get(0) != null;
    }

    @Override
    public void process(Indicator indicator, JSONObject inputRequest) throws JSONException, IllegalArgumentException {
        Trend prevTrend = trend;

        if (indicator.equals(Indicator.PIFAGOR_GLOBAL)) {
            PIFAGOR_GLOBAL_SIGNAL pifagorGlobalSignal = new PIFAGOR_GLOBAL_SIGNAL(inputRequest);

            if (pifagorGlobalSignal.getGlobalAction().equals(PIFAGOR_GLOBAL_SIGNAL.Global_Action.STRONG_LONG)) {
                trend = Trend.BULLISH;
            } else if (pifagorGlobalSignal.getGlobalAction().equals(PIFAGOR_GLOBAL_SIGNAL.Global_Action.STRONG_SHORT)) {
                trend = Trend.BEARISH;
            }

            Indicator indicator1;

            if (debugMode && (indicator1 =
                    pifagorGlobalSignal.getGlobalAction().equals(PIFAGOR_GLOBAL_SIGNAL.Global_Action.STRONG_LONG)
                            ? Indicator.PIFAGOR_STRONG_LONG_ALARM
                            : (pifagorGlobalSignal.getGlobalAction().equals(PIFAGOR_GLOBAL_SIGNAL.Global_Action.STRONG_SHORT)
                            ? Indicator.PIFAGOR_STRONG_SHORT_ALARM
                            : null)) != null) {
                logger.logTgBot(I18nSupport.i18n_literals("alarm.once.per.bar.close",
                        pifagorGlobalSignal.getTicker(),
                        pifagorGlobalSignal.getExchange(),
                        indicator1.getEmoji(),
                        indicator1.alias(),
                        Utils.intToInterval(pifagorGlobalSignal.getInterval())));
            }
        } else if (indicator.equals(Indicator.ADX_DI)) {
            ADX_DI_SIGNAL adxDiSignal =
                    new ADX_DI_SIGNAL(inputRequest);

            if (adxDiSignal.getBackground().equals(ADX_DI_SIGNAL.Background.GREEN)) {
                trend = Trend.BULLISH;
            } else if (adxDiSignal.getBackground().equals(ADX_DI_SIGNAL.Background.RED)) {
                trend = Trend.BEARISH;
            }

            if (debugMode) {
                logger.logTgBot(I18nSupport.i18n_literals("adx.di.signal",
                        ticker,
                        adxDiSignal.getBackground().getEmoji(),
                        adxDiSignal.getClose()));
            }
        } else if (indicator.equals(Indicator.FMA)) {
            FMA_SIGNAL fmaSignal = new FMA_SIGNAL(inputRequest);

            if (!testMode) {
                Position currentLongPosition = null;
                Position currentShortPosition = null;

                for (Position position : requestSender.getOpenedPositions()) {
                    if (position.getSymbol().equals(ticker)) {
                        if (position.getPositionSide().equals(PositionSide.LONG.toString())) {
                            currentLongPosition = position;
                        } else if (position.getPositionSide().equals(PositionSide.SHORT.toString())) {
                            currentShortPosition = position;
                        }
                    }
                }

                switch (fmaSignal.getSmaColor()) {
                    case RED:
                        if (currentLongPosition != null) {
                            close$LogPosition(ticker, PositionSide.LONG, redCloseLongPercentage);
                        }
                        break;
                    case ORANGE:
                        if (!isOrangeLongClosed &&
                                currentLongPosition != null &&
                                close$LogPosition(ticker, PositionSide.LONG, orangeCloseLongPercentage)) {
                            isOrangeLongClosed = true;
                        }

                        if (trend.isBearish() &&
                                open$logPositionPercentageOfMarginBalance(ticker, PositionSide.SHORT, orangeOpenShortPercentage)) {
                            isYellowShortClosed = false;
                        }
                        break;
                    case BLACK:
                        if (trend.isBearish() &&
                                open$logPositionPercentageOfMarginBalance(ticker, PositionSide.SHORT, blackOpenShortPercentage)) {
                            isYellowShortClosed = false;
                        }
                        break;
                    case YELLOW:
                        if (isYellowShortClosed &&
                                currentShortPosition != null &&
                                close$LogPosition(ticker, PositionSide.SHORT, yellowCloseShortPercentage)) {
                            isYellowShortClosed = true;
                        }

                        if (trend.isBullish() &&
                                open$logPositionPercentageOfMarginBalance(ticker, PositionSide.LONG, yellowOpenLongPercentage)) {
                            isOrangeLongClosed = false;
                        }
                        break;
                    case GREEN:
                        if (currentShortPosition != null) {
                            close$LogPosition(ticker, PositionSide.SHORT, greenCloseShortPercentage);
                        }

                        if (trend.isBullish() &&
                                open$logPositionPercentageOfMarginBalance(ticker, PositionSide.LONG, greenOpenLongPercentage)) {
                            isOrangeLongClosed = false;
                        }
                        break;
                }
            }

            if (debugMode) {
                logger.logTgBot(I18nSupport.i18n_literals("fma.signal",
                        ticker,
                        fmaSignal.getSmaColor().getEmoji(),
                        fmaSignal.getClose()));
            }
        }

        if (!prevTrend.equals(trend)) {
            if (!testMode) {
                PositionSide positionSide = prevTrend.equals(Trend.BULLISH) ? PositionSide.LONG : PositionSide.SHORT;

                if (requestSender.getPosition(ticker, positionSide) != null) {
                    close$LogPosition(ticker, positionSide, 100);
                }
            }

            if (debugMode) {
                logger.logTgBot(I18nSupport.i18n_literals("safety.trade.trend.changed",
                        trend.toString(),
                        trend.getEmoji()));
            }
        }
    }

    @Override
    public boolean isSupportedSignal(Class<?> signal, String ticker) {
        return this.ticker.equals(ticker) &&
                (signal.equals(PIFAGOR_GLOBAL_SIGNAL.class)
                        || signal.equals(ADX_DI_SIGNAL.class)
                        || signal.equals(FMA_SIGNAL.class));
    }

    @Override
    public void close() {

    }
}
