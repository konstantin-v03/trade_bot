package com.strategies;

import com.futures.dualside.RequestSender;
import com.tgbot.AsyncSender;
import com.utils.Constants;
import com.utils.I18nSupport;
import okhttp3.HttpUrl;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Objects;

public class ChiaBalanceAlarmHandler extends BalanceAlarmHandler {
    private final String alias;

    public ChiaBalanceAlarmHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender) {
        super(requestSender, strategyProps, asyncSender);

        alias = Objects.requireNonNull(strategyProps.getProperties().get(Constants.ALIAS.getKey()));
        httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host("xchscan.com")
                .addPathSegment("api")
                .addPathSegment("account")
                .addPathSegment("balance")
                .addQueryParameter("address",
                        Objects.requireNonNull(strategyProps.getProperties().get(Constants.ADDRESS.getKey())))
                .build();

        startScheduler();
    }

    @Override
    public BigDecimal balanceFromJson(JSONObject jsonObject) {
        return jsonObject.getBigDecimal("xch");
    }

    @Override
    public void logBalanceIncreased(BigDecimal currBalance, BigDecimal lastBalance) {
        logger.logTgBot(I18nSupport.i18n_literals("chia.balance.changed",
                alias,
                String.format("%.1f", currBalance),
                "+" + String.format("%.1f", currBalance.subtract(lastBalance))));
    }

    @Override
    public void logBalanceDecreased(BigDecimal currBalance, BigDecimal lastBalance) {
        logger.logTgBot(I18nSupport.i18n_literals("chia.balance.changed",
                alias,
                String.format("%.1f", currBalance),
                "-" + String.format("%.1f", lastBalance.subtract(currBalance))));
    }
}