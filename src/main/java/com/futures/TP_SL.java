package com.futures;

import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.Order;
import com.utils.I18nSupport;

import java.math.BigDecimal;

import static com.utils.Calculations.percentage;

public class TP_SL {
    private final BigDecimal takeProfitPrice;
    private final BigDecimal stopLossPrice;

    private Order takeProfitOrder;
    private Order stopLossOrder;

    public TP_SL(BigDecimal takeProfitPrice, BigDecimal stopLossPrice) {
        this.takeProfitPrice = takeProfitPrice;
        this.stopLossPrice = stopLossPrice;
    }

    public TP_SL(PositionSide positionSide, BigDecimal price, BigDecimal takeProfitPercent, BigDecimal stopLossPercent) throws IllegalArgumentException{
        BigDecimal takeProfitPercentage = percentage(price, takeProfitPercent);
        BigDecimal stopLossPercentage = percentage(price, stopLossPercent);

        if (positionSide.equals(PositionSide.LONG)) {
            takeProfitPrice = price.add(takeProfitPercentage);
            stopLossPrice = price.subtract(stopLossPercentage);
        } else if (positionSide.equals(PositionSide.SHORT)) {
            takeProfitPrice = price.subtract(takeProfitPercentage);
            stopLossPrice = price.add(stopLossPercentage);
        } else {
            throw new IllegalArgumentException(I18nSupport.i18n_literals("illegal.position.side"));
        }
    }

    public BigDecimal getTakeProfitPrice() {
        return takeProfitPrice;
    }

    public BigDecimal getStopLossPrice() {
        return stopLossPrice;
    }

    public Order getTakeProfitOrder() {
        return takeProfitOrder;
    }

    public void setTakeProfitOrder(Order takeProfitOrder) {
        this.takeProfitOrder = takeProfitOrder;
    }

    public Order getStopLossOrder() {
        return stopLossOrder;
    }

    public void setStopLossOrder(Order stopLossOrder) {
        this.stopLossOrder = stopLossOrder;
    }

    @Override
    public String toString() {
        return "TP_SL{" +
                "takeProfitPrice=" + takeProfitPrice +
                ", stopLossPrice=" + stopLossPrice +
                ", takeProfitOrder=" + takeProfitOrder +
                ", stopLossOrder=" + stopLossOrder +
                '}';
    }
}
