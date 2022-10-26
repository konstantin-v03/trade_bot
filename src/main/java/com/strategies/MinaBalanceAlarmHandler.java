package com.strategies;

import com.futures.dualside.RequestSender;
import com.tgbot.AsyncSender;
import com.utils.Constants;
import com.utils.I18nSupport;
import okhttp3.HttpUrl;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Objects;

public class MinaBalanceAlarmHandler extends BalanceAlarmHandler {
    private final String alias;

    public MinaBalanceAlarmHandler(RequestSender requestSender, StrategyProps strategyProps, AsyncSender asyncSender) {
        super(requestSender, strategyProps, asyncSender);

        alias = Objects.requireNonNull(strategyProps.getProperties().get(Constants.ALIAS.getKey()));
        httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host("api.minaexplorer.com")
                .addPathSegment("accounts")
                .addPathSegment(Objects.requireNonNull(strategyProps.getProperties().get(Constants.ADDRESS.getKey())))
                .build();

        startScheduler();
    }

    @Override
    public BigDecimal balanceFromJson(JSONObject jsonObject) {
        return jsonObject
                .getJSONObject("account")
                .getJSONObject("balance")
                .getBigDecimal("total");
    }

    @Override
    public void logBalanceIncreased(BigDecimal currBalance, BigDecimal lastBalance) {
        logger.logTgBot(I18nSupport.i18n_literals("mina.balance.changed",
                alias,
                String.format("%.1f", currBalance),
                "+" + String.format("%.1f", currBalance.subtract(lastBalance))));
    }

    @Override
    public void logBalanceDecreased(BigDecimal currBalance, BigDecimal lastBalance) {
        logger.logTgBot(I18nSupport.i18n_literals("mina.balance.changed",
                alias,
                String.format("%.1f", currBalance),
                "-" + String.format("%.1f", lastBalance.subtract(currBalance))));
    }
}
