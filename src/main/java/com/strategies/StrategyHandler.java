package com.strategies;

import com.futures.dualside.RequestSender;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.utils.Utils;

public abstract class StrategyHandler implements HttpHandler {
    protected final RequestSender requestSender;

    protected String inputRequest;

    public StrategyHandler(RequestSender requestSender) {
        this.requestSender = requestSender;
    }

    @Override
    public final void handle(HttpExchange httpExchange) {
        inputRequest = Utils.readAllFromInputStream(httpExchange.getRequestBody());
        process();
        Utils.answerOkToHttpsRequest(httpExchange);
    }

    public abstract void process();
}
