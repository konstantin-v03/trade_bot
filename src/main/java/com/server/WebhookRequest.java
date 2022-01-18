package com.server;

import com.binance.client.model.enums.PositionSide;
import com.futures.Amount;
import com.futures.Coin;

public class WebhookRequest{
    private final String ticket;
    private final PositionSide positionSide;
    private final Amount amount;
    private final int leverage;

    public WebhookRequest(String ticket, PositionSide positionSide, Amount amount, int leverage) {
        this.ticket = ticket;
        this.positionSide = positionSide;
        this.amount = amount;
        this.leverage = leverage;
    }

    public WebhookRequest(Coin coin, PositionSide positionSide) {
        ticket = coin.getTicket();
        amount = coin.getAmount();
        leverage = coin.getLeverage();
        this.positionSide = positionSide;
    }

    public String getTicket() {
        return ticket;
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
