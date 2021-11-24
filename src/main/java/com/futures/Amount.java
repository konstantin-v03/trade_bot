package com.futures;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Amount {
    public enum TYPE {
        PERCENT, USD
    }

    private final BigDecimal amount;
    private final TYPE type;

    public Amount(String amount) throws IllegalArgumentException {
        String subString = amount.substring(0, amount.length() - 1);
        char lastChr = amount.charAt(amount.length() - 1);

        switch (lastChr) {
            case '%':
                type = TYPE.PERCENT;
                this.amount = new BigDecimal(subString);

                if (this.amount.compareTo(new BigDecimal(100)) > 0 || this.amount.compareTo(new BigDecimal(1)) < 0) {
                    throw new IllegalArgumentException();
                }
                break;
            case '$':
                type = TYPE.USD;
                this.amount = new BigDecimal(subString);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public Amount(BigDecimal amount, TYPE type) {
        this.amount = amount;
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TYPE getType() {
        return type;
    }

    public static BigDecimal getAmountUSD(BigDecimal amountPercent, BigDecimal availableBalance) {
        return availableBalance.multiply(amountPercent.setScale(0, RoundingMode.FLOOR).divide(new BigDecimal(100), RoundingMode.FLOOR));
    }

    @Override
    public String toString() {
        return amount.toString() + (type.equals(TYPE.PERCENT) ? "%" : "$");
    }
}
