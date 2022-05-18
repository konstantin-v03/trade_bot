package com.strategies;

import com.binance.client.model.trade.MyTrade;
import com.binance.client.model.trade.Position;
import com.futures.TP_SL;
import com.futures.dualside.RequestSender;
import com.tgbot.AsyncSender;
import com.utils.Calculations;
import com.utils.I18nSupport;
import com.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public abstract class StrategyHandler {
    protected final RequestSender requestSender;
    protected final StrategyProps strategyProps;
    public final Logger logger;

    public StrategyHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender) {
        this.requestSender = requestSender;
        this.strategyProps = strategyProps;
        logger = new Logger(asyncSender);
    }

    public abstract void process(JSONObject inputSignal) throws JSONException, IllegalArgumentException;

    public abstract void close();

    public StrategyProps getStrategyProps() {
        return strategyProps;
    }

    class Logger {
        private final AsyncSender asyncSender;

        public Logger(AsyncSender asyncSender) {
            this.asyncSender = asyncSender;
        }

        public void logTgBot(String log) {
            if (asyncSender != null && strategyProps.getLogChatIds() != null) {
                asyncSender.sendTextMsgAsync(log, strategyProps.getLogChatIds());
            }
        }

        public void log$pinTgBot(String log) {
            if (asyncSender != null && strategyProps.getLogChatIds() != null) {
                asyncSender.send$pinTextMsg(log, strategyProps.getLogChatIds());
            }
        }

        public void logOpenPosition(Position position) {
            logTgBot(position != null ? I18nSupport.i18n_literals("position.open",
                    position.getPositionSide().equals("LONG") ? "\uD83D\uDCC8" : "\uD83D\uDCC9",
                    position.getSymbol(),
                    position.getPositionSide(),
                    position.getEntryPrice(),
                    new Date()) : I18nSupport.i18n_literals("position.not.open", "Position = null!"));
        }

        public void logClosePosition(List<MyTrade> myTrades) {
            MyTrade myTrade;

            logTgBot(myTrades != null && (myTrade = myTrades.get(0)) != null ?
                    I18nSupport.i18n_literals("position.close",
                            myTrade.getSymbol(),
                            myTrade.getPositionSide(),
                            myTrade.getPrice(),
                            new Date(myTrade.getTime()),
                            Calculations.calcTotalRealizedPnl(myTrades)) :
                    I18nSupport.i18n_literals("position.not.close", "MyTrade = null!"));
        }

        public void logTP_SLOrders(TP_SL tp_sl) {
            boolean isPost = false;

            if (tp_sl != null) {
                if (tp_sl.getTakeProfitOrder() != null) {
                    logTgBot(I18nSupport.i18n_literals("post.take.profit", tp_sl.getTakeProfitOrder().getStopPrice()));
                    isPost = true;
                }

                if (tp_sl.getStopLossOrder() != null) {
                    logTgBot(I18nSupport.i18n_literals("post.stop.loss", tp_sl.getStopLossOrder().getStopPrice()));
                    isPost = true;
                }
            }

            if (!isPost) {
                logTgBot(I18nSupport.i18n_literals("tp.sl.not.posted"));
            }
        }

        public void logException(Exception exception) {
            logTgBot(I18nSupport.i18n_literals("error.occured", exception));
        }

        public void logCloseLogToFile(Strategy strategy, List<MyTrade> myTrades) {
            try {
                if (myTrades == null || myTrades.size() <= 0) {
                    throw new IllegalArgumentException("List<MyTrade> equals to null or size less than 1!");
                }

                MyTrade myTrade = myTrades.get(0);

                Utils.appendStrToFile(Utils.getLogFileNames(strategy, myTrade.getSymbol()).get(0),
                        I18nSupport.i18n_literals("file.close.log",
                                new Date(myTrade.getTime()),
                                myTrade.getSymbol(),
                                Calculations.calcTotalRealizedPnl(myTrades)) + "\n");
            } catch (IOException |IllegalArgumentException exception) {
                logException(exception);
            }
        }
    }
}
