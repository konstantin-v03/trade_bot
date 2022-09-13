package com.signal;

import com.binance.client.model.enums.PositionSide;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class StrategyAction {
    //PositionSide;Action;TP price;SL price
    public enum Type {
        OPEN,
        CLOSE,
        TAKE_PROFIT,
        STOP_LOSS,
        TP_SL,
    }

    private final PositionSide positionSide;
    private final Type type;

    private final BigDecimal takeProfit;
    private final BigDecimal stopLoss;

    public StrategyAction(String comment) throws IllegalArgumentException {
        List<String> splitComment = Arrays.asList(comment.split(";"));

        try {
            positionSide = PositionSide.valueOf(splitComment.get(0));
            type = Type.valueOf(splitComment.get(1));

            if (type.equals(Type.TAKE_PROFIT)) {
                takeProfit = new BigDecimal(splitComment.get(2));
                stopLoss = null;
            } else if (type.equals(Type.STOP_LOSS)) {
                stopLoss = new BigDecimal(splitComment.get(2));
                takeProfit = null;
            } else if (type.equals(Type.TP_SL)) {
                takeProfit = new BigDecimal(splitComment.get(2));
                stopLoss = new BigDecimal(splitComment.get(3));
            } else {
                takeProfit = null;
                stopLoss = null;
            }
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            throw new IllegalArgumentException();
        }
    }

    public PositionSide getPositionSide() {
        return positionSide;
    }

    public Type getType() {
        return type;
    }

    public BigDecimal getTakeProfit() {
        return takeProfit;
    }

    public BigDecimal getStopLoss() {
        return stopLoss;
    }
}
