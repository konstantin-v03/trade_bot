package com.strategies;

import com.futures.dualside.RequestSender;
import com.signal.Indicator;
import com.tgbot.AsyncSender;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class StrategyHandler {
    protected final RequestSender requestSender;
    protected final StrategyProps strategyProps;
    public final Logger logger;

    public StrategyHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender) {
        this.requestSender = requestSender;
        this.strategyProps = strategyProps;
        logger = new Logger(asyncSender, strategyProps.getLogChatIds());
    }

    public StrategyHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender, long exceptionChatId) {
        this.requestSender = requestSender;
        this.strategyProps = strategyProps;
        logger = new Logger(asyncSender, strategyProps.getLogChatIds(), exceptionChatId);
    }

    public abstract void process(Indicator indicator, JSONObject inputRequest) throws JSONException, IllegalArgumentException;

    public abstract void close();

    public StrategyProps getStrategyProps() {
        return strategyProps;
    }

    public abstract boolean isSupportedSignal(Class<?> signal, String ticker);
}
