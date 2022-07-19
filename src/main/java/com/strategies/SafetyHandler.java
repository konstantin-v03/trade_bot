package com.strategies;

import com.binance.client.model.enums.MarginType;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.MyTrade;
import com.binance.client.model.trade.Position;
import com.futures.Amount;
import com.futures.dualside.RequestSender;
import com.signal.*;
import com.tgbot.AsyncSender;
import com.utils.Calculations;
import com.utils.Constants;
import com.utils.I18nSupport;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.List;

public class SafetyHandler extends StrategyHandler {
    private final String ticker;
    private final int leverage;
    private Trend trend;

    private final int yellowOpenLongPercentage;
    private final int greenOpenLongPercentage;
    private final int orangeCloseLongPercentage;
    private final int redCloseLongPercentage;

    private final int blackOpenShortPercentage;
    private final int orangeOpenShortPercentage;
    private final int yellowCloseShortPercentage;
    private final int greenCloseShortPercentage;

    public SafetyHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender, long exceptionChatId) throws IllegalArgumentException {
        super(requestSender, strategyProps, asyncSender, exceptionChatId);

        if (strategyProps.getTickers().size() != 1) {
            throw new IllegalArgumentException(I18nSupport.i18n_literals("tickers.size.must.be.one"));
        }

        ticker = strategyProps.getTickers().get(0);
        leverage = Integer.parseInt(strategyProps.getProperties().get(Constants.LEVERAGE.getKey()));
        trend = Trend.valueOf(strategyProps.getProperties().get(Constants.TREND.getKey()));

        yellowOpenLongPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.YELLOW_OPEN_LONG_PERCENTAGE.getKey()));
        greenOpenLongPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.GREEN_OPEN_LONG_PERCENTAGE.getKey()));
        orangeCloseLongPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.ORANGE_CLOSE_LONG_PERCENTAGE.getKey()));
        redCloseLongPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.RED_CLOSE_LONG_PERCENTAGE.getKey()));

        blackOpenShortPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.BLACK_OPEN_SHORT_PERCENTAGE.getKey()));
        orangeOpenShortPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.ORANGE_OPEN_SHORT_PERCENTAGE.getKey()));
        yellowCloseShortPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.YELLOW_CLOSE_SHORT_PERCENTAGE.getKey()));
        greenCloseShortPercentage = Integer.parseInt(strategyProps.getProperties().get(Constants.GREEN_CLOSE_SHORT_PERCENTAGE.getKey()));
    }

    public boolean close$LogPosition(String ticker, PositionSide positionSide, int percentage) {
        List<MyTrade> myTrades = requestSender.getMyTrades(ticker,
                requestSender.closePositionMarket(ticker, positionSide, percentage).getOrderId());

        logger.logClosedPosition(myTrades);

        return myTrades != null && myTrades.get(0) != null;
    }

    public boolean open$LogPosition(String ticker, PositionSide positionSide, Amount amount) {
        if (positionSide.equals(PositionSide.LONG)) {
            requestSender.openLongPositionMarket(ticker, MarginType.ISOLATED, amount, leverage);
        } else if (positionSide.equals(PositionSide.SHORT)) {
            requestSender.openShortPositionMarket(ticker, MarginType.ISOLATED, amount, leverage);
        }

        Position position = requestSender.getPosition(ticker, positionSide);

        logger.logOpenedPosition(position);

        return position != null;
    }

    public boolean open$logPositionPercentageOfBalance(String ticker, PositionSide positionSide, int percentage) {
        Position position;

        if (percentage >= 100 || (position = requestSender.getPosition(ticker, positionSide)) == null) {
            return open$LogPosition(ticker, positionSide, new Amount(new BigDecimal(percentage), Amount.Type.PERCENT));
        }

        BigDecimal initialMargin = position.getInitialMargin();

        BigDecimal buyInAdditionMargin =
                Calculations.percentage(requestSender.getAvailableBalance(RequestSender.getAssetBySymbol(ticker)).add(initialMargin), new BigDecimal(percentage))
                        .subtract(initialMargin);

        if (buyInAdditionMargin.compareTo(BigDecimal.ZERO) > 0) {
            return open$LogPosition(ticker,
                    positionSide,
                    new Amount(buyInAdditionMargin, Amount.Type.USD));
        }

        return false;
    }

    @Override
    public void process(Indicator indicator, JSONObject inputRequest) throws JSONException, IllegalArgumentException {
        Trend prevTrend = trend;

        if (indicator.equals(Indicator.PIFAGOR_GLOBAL)) {
            PIFAGOR_GLOBAL_SIGNAL.Global_Action globalAction = new PIFAGOR_GLOBAL_SIGNAL(inputRequest).getGlobalAction();

            if (globalAction.equals(PIFAGOR_GLOBAL_SIGNAL.Global_Action.STRONG_LONG)) {
                trend = Trend.BULLISH;
            } else if (globalAction.equals(PIFAGOR_GLOBAL_SIGNAL.Global_Action.STRONG_SHORT)) {
                trend = Trend.BEARISH;
            }
        } else if (indicator.equals(Indicator.PIFAGOR_SEVEN_DAYS)) {
            PIFAGOR_SEVEN_DAYS_SIGNAL.Background background =
                    new PIFAGOR_SEVEN_DAYS_SIGNAL(inputRequest).getBackground();

            if (background.equals(PIFAGOR_SEVEN_DAYS_SIGNAL.Background.GREEN)) {
                trend = Trend.BULLISH;
            } else if (background.equals(PIFAGOR_SEVEN_DAYS_SIGNAL.Background.RED)) {
                trend = Trend.BEARISH;
            }
        } else if (indicator.equals(Indicator.FMA)) {
            FMA_SIGNAL fmaSignal = new FMA_SIGNAL(inputRequest);
            Position currentPosition = requestSender.getPosition(ticker, trend.equals(Trend.BULLISH) ? PositionSide.LONG : PositionSide.SHORT);

            switch (fmaSignal.getSmaColor()) {
                case RED:
                    if (currentPosition != null && trend.isBullish()) {
                        close$LogPosition(ticker, PositionSide.LONG, redCloseLongPercentage);
                    }
                    break;
                case ORANGE:
                    if (currentPosition != null && trend.isBullish()) {
                        close$LogPosition(ticker, PositionSide.LONG, orangeCloseLongPercentage);
                    } else if (trend.isBearish()) {
                        open$logPositionPercentageOfBalance(ticker, PositionSide.SHORT, orangeOpenShortPercentage);
                    }
                    break;
                case BLACK:
                    if (trend.isBearish()) {
                        open$logPositionPercentageOfBalance(ticker, PositionSide.SHORT, blackOpenShortPercentage);
                    }
                    break;
                case YELLOW:
                    if (currentPosition != null && trend.isBearish()) {
                        close$LogPosition(ticker, PositionSide.SHORT, yellowCloseShortPercentage);
                    } else if (trend.isBullish()) {
                        open$logPositionPercentageOfBalance(ticker, PositionSide.LONG, yellowOpenLongPercentage);
                    }
                    break;
                case GREEN:
                    if (currentPosition != null && trend.isBearish()) {
                        close$LogPosition(ticker, PositionSide.SHORT, greenCloseShortPercentage);
                    } else if (trend.isBullish()) {
                        open$logPositionPercentageOfBalance(ticker, PositionSide.LONG, greenOpenLongPercentage);
                    }
                    break;
            }
        }

        if (!prevTrend.equals(trend)) {
            close$LogPosition(ticker, prevTrend.equals(Trend.BULLISH) ? PositionSide.LONG : PositionSide.SHORT, 100);
        }
    }

    @Override
    public boolean isSupportedSignal(Class<?> signal, String ticker) {
        return this.ticker.equals(ticker) &&
                (signal.equals(PIFAGOR_GLOBAL_SIGNAL.class)
                        || signal.equals(PIFAGOR_SEVEN_DAYS_SIGNAL.class)
                        || signal.equals(FMA_SIGNAL.class));
    }

    @Override
    public void close() {

    }
}
