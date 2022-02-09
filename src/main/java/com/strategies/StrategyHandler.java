package com.strategies;

import com.futures.dualside.RequestSender;
import org.json.JSONObject;

public abstract class StrategyHandler {
    protected final RequestSender requestSender;
    protected final StrategyProps strategyProps;

    public StrategyHandler(RequestSender requestSender, StrategyProps strategyProps) {
        this.requestSender = requestSender;
        this.strategyProps = strategyProps;
    }

    public abstract void process(JSONObject inputSignal);
}
