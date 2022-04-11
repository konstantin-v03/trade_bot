package com.futures;

import java.math.BigDecimal;

import static com.utils.Calculations.ONE_HUNDRED;
import static com.utils.Calculations.percentage;

public class Amount {
    public enum Type {
        PERCENT, USD
    }

    private final BigDecimal amount;
    private final Type type;

    public Amount(String amount) throws IllegalArgumentException {
        String subString = amount.substring(0, amount.length() - 1);
        char lastChr = amount.charAt(amount.length() - 1);

        switch (lastChr) {
            case '%':
                type = Type.PERCENT;
                this.amount = new BigDecimal(subString);

                if (this.amount.compareTo(ONE_HUNDRED) > 0 || this.amount.compareTo(BigDecimal.ONE) < 0) {
                    throw new IllegalArgumentException();
                }
                break;
            case '$':
                type = Type.USD;
                this.amount = new BigDecimal(subString);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public Amount(BigDecimal amount, Type type) {
        this.amount = amount;
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Type getType() {
        return type;
    }

    public static BigDecimal getAmountUSD(BigDecimal amountPercent, BigDecimal availableBalance) {
        return percentage(availableBalance, amountPercent);
    }

    @Override
    public String toString() {
        return amount.toString() + (type.equals(Type.PERCENT) ? "%" : "$");
    }
}
