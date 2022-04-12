package com.strategies;

import com.futures.dualside.RequestSender;
import com.log.TradeLogger;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class StrategyHandler {
    protected final RequestSender requestSender;
    protected final StrategyProps strategyProps;
    protected TradeLogger tradeLogger;

    public StrategyHandler(RequestSender requestSender, StrategyProps strategyProps, TradeLogger tradeLogger) {
        this.requestSender = requestSender;
        this.strategyProps = strategyProps;
        this.tradeLogger = tradeLogger;
    }

    public abstract void process(JSONObject inputSignal) throws JSONException, IllegalArgumentException;

    public abstract void close();

    public StrategyProps getStrategyProps() {
        return strategyProps;
    }

    public void setLogChatId(Long logChatId) {
        tradeLogger.setLogChatId(logChatId);
    }
}
