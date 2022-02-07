package com.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Calculations {
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    public static BigDecimal percentage(BigDecimal base, BigDecimal percentage){
        return base == null || percentage == null ? null : base.multiply(percentage).divide(ONE_HUNDRED, RoundingMode.FLOOR);
    }
}
