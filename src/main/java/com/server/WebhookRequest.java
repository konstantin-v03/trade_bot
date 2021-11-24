package com.server;

import com.binance.client.model.enums.PositionSide;
import com.futures.Amount;

public class WebhookRequest{
    private final String symbol;
    private final PositionSide positionSide;
    private final Amount amount;
    private final int leverage;

    public WebhookRequest(String symbol, PositionSide positionSide, Amount amount, int leverage) {
        this.symbol = symbol;
        this.positionSide = positionSide;
        this.amount = amount;
        this.leverage = leverage;
    }

    public String getSymbol() {
        return symbol;
    }

    public PositionSide getPositionSide() {
        return positionSide;
    }

    public Amount getAmount() {
        return amount;
    }

    public int getLeverage() {
        return leverage;
    }
}
