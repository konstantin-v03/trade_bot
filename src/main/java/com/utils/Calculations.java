package com.utils;

import com.binance.client.model.trade.MyTrade;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;

public class Calculations {
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    public static BigDecimal percentage(BigDecimal base, BigDecimal percentage){
        return base == null || percentage == null ? null : base.multiply(percentage).divide(ONE_HUNDRED, RoundingMode.FLOOR);
    }

    public static BigDecimal calcTotalRealizedPnl(List<MyTrade> myTrades) {
        BigDecimal realizedPnl = new BigDecimal(BigInteger.ZERO);

        for (MyTrade myTrade : myTrades) {
            realizedPnl = realizedPnl.add(myTrade.getRealizedPnl());
        }

        return realizedPnl;
    }
}
